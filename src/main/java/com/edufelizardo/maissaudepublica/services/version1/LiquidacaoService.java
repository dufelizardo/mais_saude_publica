package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DiariaTfdRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LiquidacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PagamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FinanceiroDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LiquidacaoDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ManutencaoDtos;
import com.edufelizardo.maissaudepublica.models.enuns.*;
import com.edufelizardo.maissaudepublica.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Financeiro, fatia F2 (ADR-0219): a fila a liquidar a partir dos atestos da plataforma (recebimento definitivo, ordem de serviço por contrato,
 * diária do TFD), a liquidação com as retenções, o pagamento com a ordem bancária e a ordem cronológica, as diárias do TFD, os restos a
 * pagar e o painel da execução.
 */
@Service
public class LiquidacaoService {

    static final int VENCIMENTO_PADRAO = 30;
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final Set<SituacaoSolicitacaoTransporte> TFD_CONCEDIVEL = EnumSet.of(SituacaoSolicitacaoTransporte.AUTORIZADA,
            SituacaoSolicitacaoTransporte.PROGRAMADA, SituacaoSolicitacaoTransporte.REALIZADA);

    @Autowired private LiquidacaoRepository liquidacaoRepository;
    @Autowired private DiariaTfdRepository diariaRepository;
    @Autowired private EmpenhoRepository empenhoRepository;
    @Autowired private RecebimentoCompraRepository recebimentoRepository;
    @Autowired private ItemRecebimentoCompraRepository itemRecebimentoRepository;
    @Autowired private OrdemServicoManutencaoRepository ordemServicoRepository;
    @Autowired private SolicitacaoTransporteRepository solicitacaoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private FinanceiroService financeiroService;
    @Autowired private ManutencaoService manutencaoService;
    @Autowired private CustosService custosService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── A liquidar ─────────────────────────────────────────────────────────────────────────────────

    /** Os atestos ainda sem liquidação: os recebimentos definitivos com aceito, as ordens de serviço por contrato concluídas e as diárias. */
    @Transactional(readOnly = true)
    public List<LiquidacaoDtos.Pendencia> pendencias() {
        controleDeAcesso.exigirAlguma(FinanceiroService.CONSULTAR, FinanceiroService.GERENCIAR);
        List<Liquidacao> liquidadas = liquidacaoRepository.findTodas();
        Set<UUID> recebimentos = liquidadas.stream().filter(l -> l.getRecebimento() != null).map(l -> l.getRecebimento().getUuid()).collect(Collectors.toSet());
        Set<UUID> ordens = liquidadas.stream().filter(l -> l.getOrdemServico() != null).map(l -> l.getOrdemServico().getUuid()).collect(Collectors.toSet());
        Set<UUID> diarias = liquidadas.stream().filter(l -> l.getDiaria() != null).map(l -> l.getDiaria().getUuid()).collect(Collectors.toSet());
        List<LiquidacaoDtos.Pendencia> lista = new ArrayList<>();

        List<RecebimentoCompra> abertos = recebimentoRepository.findDefinitivos().stream().filter(r -> !recebimentos.contains(r.getUuid())).toList();
        Map<UUID, BigDecimal> valores = valoresDosRecebimentos(abertos);
        for (RecebimentoCompra r : abertos) {
            BigDecimal valor = valores.getOrDefault(r.getUuid(), BigDecimal.ZERO);
            if (valor.signum() <= 0) {
                continue;
            }
            OrdemFornecimento o = r.getOrdem();
            InstrumentoContratual ins = o.getInstrumento();
            lista.add(new LiquidacaoDtos.Pendencia(OrigemLiquidacao.RECEBIMENTO, r.getUuid(), "Ordem " + o.getNumero() + " · nota " + r.getNotaFiscal(),
                    ins.getFornecedor().getUuid(), ins.getFornecedor().getRazaoSocial(), ins.getUuid(), ins.getNumero(),
                    o.getEmpenhoRef() != null ? o.getEmpenhoRef().getUuid() : null, o.getEmpenhoRef() != null ? o.getEmpenhoRef().getNumero() : null,
                    valor, r.getDefinitivoEm(), o.getUnidade().getNome()));
        }
        List<OrdemServicoManutencao> concluidas = ordemServicoRepository.findTodas().stream()
                .filter(o -> o.getSituacao() == SituacaoOrdemServico.CONCLUIDA && o.getExecucao() == ExecucaoManutencao.CONTRATO && o.getInstrumento() != null)
                .filter(o -> !ordens.contains(o.getUuid())).toList();
        Map<UUID, ManutencaoDtos.Ordem> custos = manutencaoService.dtosDe(concluidas).stream().collect(Collectors.toMap(ManutencaoDtos.Ordem::uuid, x -> x));
        for (OrdemServicoManutencao o : concluidas) {
            BigDecimal valor = custos.get(o.getUuid()).custoTotal();
            if (valor.signum() <= 0) {
                continue;
            }
            InstrumentoContratual ins = o.getInstrumento();
            lista.add(new LiquidacaoDtos.Pendencia(OrigemLiquidacao.ORDEM_SERVICO, o.getUuid(), "Ordem de serviço " + o.getNumero() + " · "
                    + o.getBem().getDescricao(), ins.getFornecedor().getUuid(), ins.getFornecedor().getRazaoSocial(), ins.getUuid(), ins.getNumero(), null, null,
                    valor, o.getConcluidaEm(), o.getBem().getUnidade().getNome()));
        }
        for (DiariaTfd d : diariaRepository.findTodas()) {
            if (!diarias.contains(d.getUuid())) {
                lista.add(new LiquidacaoDtos.Pendencia(OrigemLiquidacao.DIARIA_TFD, d.getUuid(), "Diária do TFD · " + d.getSolicitacao().getPaciente().getNome()
                        + " (" + d.getDias() + " dia(s))", d.getCredor().getUuid(), d.getCredor().getRazaoSocial(), null, null, null, null, d.getValorTotal(),
                        d.getConcedidaEm(), d.getSolicitacao().getUnidade() != null ? d.getSolicitacao().getUnidade().getNome() : null));
            }
        }
        lista.sort(Comparator.comparing(LiquidacaoDtos.Pendencia::atestadoEm, Comparator.nullsLast(Comparator.naturalOrder())));
        return lista;
    }

    // ── Liquidação e pagamento ─────────────────────────────────────────────────────────────────────

    /**
     * Liquida o atesto (ou a despesa avulsa, com a justificativa e quem atestou, 400) contra o empenho:
     * <ul>
     *   <li>o atesto existe (404) e ainda não foi liquidado (409);</li>
     *   <li>o empenho é o da ordem, quando ela tem um; senão, do mesmo fornecedor e sem outro contrato (422);</li>
     *   <li>o bruto vai até o valor do atesto e até o saldo a liquidar do empenho (422); as retenções não passam do bruto (400).</li>
     * </ul>
     */
    @Transactional
    public LiquidacaoDtos.Liquidacao liquidar(LiquidacaoRequestDto dto) {
        controleDeAcesso.exigirAlguma(FinanceiroService.GERENCIAR);
        Empenho e = empenhoRepository.findById(dto.getEmpenhoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o empenho " + dto.getEmpenhoId() + " em nossos registros."));
        LocalDate hoje = hoje();
        if (dto.getNotaEmitidaEm() != null && dto.getNotaEmitidaEm().isAfter(hoje)) {
            throw new ResourceBadRequestException("A nota não pode ter data futura.");
        }
        Liquidacao l = new Liquidacao();
        l.setOrigem(dto.getOrigem());
        BigDecimal teto = null;
        Fornecedor credor;
        InstrumentoContratual instrumento = null;
        if (dto.getOrigem() != OrigemLiquidacao.AVULSA && dto.getOrigemId() == null) {
            throw new ResourceBadRequestException("Informe o atesto de origem.");
        }
        Set<UUID> jaLiquidados = liquidacaoRepository.findTodas().stream().map(x -> switch (x.getOrigem()) {
            case RECEBIMENTO -> x.getRecebimento().getUuid();
            case ORDEM_SERVICO -> x.getOrdemServico().getUuid();
            case DIARIA_TFD -> x.getDiaria().getUuid();
            case AVULSA -> x.getUuid();
        }).collect(Collectors.toSet());
        if (dto.getOrigemId() != null && jaLiquidados.contains(dto.getOrigemId())) {
            throw new ResourceConflictException("Este atesto já foi liquidado.");
        }
        switch (dto.getOrigem()) {
            case RECEBIMENTO -> {
                RecebimentoCompra r = recebimentoRepository.findDefinitivos().stream().filter(x -> x.getUuid().equals(dto.getOrigemId())).findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o recebimento definitivo " + dto.getOrigemId() + "."));
                OrdemFornecimento o = r.getOrdem();
                if (o.getEmpenhoRef() != null && !o.getEmpenhoRef().getUuid().equals(e.getUuid())) {
                    throw new ResourceUnprocessableEntityException("A ordem " + o.getNumero() + " é do empenho " + o.getEmpenhoRef().getNumero() + ".");
                }
                instrumento = o.getInstrumento();
                credor = instrumento.getFornecedor();
                teto = valoresDosRecebimentos(List.of(r)).getOrDefault(r.getUuid(), BigDecimal.ZERO);
                l.setRecebimento(r);
                if (dto.getNotaFiscal() == null || dto.getNotaFiscal().isBlank()) {
                    dto.setNotaFiscal(r.getNotaFiscal());
                    dto.setNotaEmitidaEm(r.getNotaEmitidaEm());
                }
            }
            case ORDEM_SERVICO -> {
                OrdemServicoManutencao o = ordemServicoRepository.findById(dto.getOrigemId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar a ordem de serviço " + dto.getOrigemId() + " em nossos registros."));
                if (o.getSituacao() != SituacaoOrdemServico.CONCLUIDA || o.getExecucao() != ExecucaoManutencao.CONTRATO || o.getInstrumento() == null) {
                    throw new ResourceUnprocessableEntityException("Só a ordem de serviço por contrato concluída é liquidada.");
                }
                instrumento = o.getInstrumento();
                credor = instrumento.getFornecedor();
                teto = manutencaoService.dtosDe(List.of(o)).get(0).custoTotal();
                l.setOrdemServico(o);
            }
            case DIARIA_TFD -> {
                DiariaTfd d = diariaRepository.findById(dto.getOrigemId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar a diária " + dto.getOrigemId() + " em nossos registros."));
                credor = d.getCredor();
                teto = d.getValorTotal();
                l.setDiaria(d);
            }
            default -> {
                if (limpo(dto.getJustificativa()) == null || limpo(dto.getAtestadoPorMatricula()) == null) {
                    throw new ResourceBadRequestException("Sem atesto na plataforma, informe a justificativa e a matrícula de quem atestou.");
                }
                if (dto.getCentroCustoId() == null) {
                    throw new ResourceBadRequestException("Na liquidação avulsa, informe o centro de custo (ADR-0220).");
                }
                l.setCentroCusto(custosService.ativo(dto.getCentroCustoId()));
                String m = dto.getAtestadoPorMatricula().trim();
                Profissional p = profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar o profissional de matrícula " + m + " em nossos registros."));
                l.setAtestadoPorCpf(p.getCpf());
                credor = e.getFornecedor();
            }
        }
        if (!e.getFornecedor().getUuid().equals(credor.getUuid())) {
            throw new ResourceUnprocessableEntityException("O empenho " + e.getNumero() + " é de outro credor.");
        }
        if (instrumento != null && e.getInstrumento() != null && !e.getInstrumento().getUuid().equals(instrumento.getUuid())) {
            throw new ResourceUnprocessableEntityException("O empenho " + e.getNumero() + " é de outra ata ou contrato.");
        }
        BigDecimal bruto = dto.getValorBruto();
        if (teto != null && bruto.compareTo(teto) > 0) {
            throw new ResourceUnprocessableEntityException("O atesto vale " + moeda(teto) + "; a liquidação passa disso.");
        }
        BigDecimal aLiquidar = saldoALiquidar(e);
        if (bruto.compareTo(aLiquidar) > 0) {
            throw new ResourceUnprocessableEntityException("O empenho " + e.getNumero() + " tem " + moeda(aLiquidar) + " a liquidar.");
        }
        BigDecimal iss = zero(dto.getIss()), irrf = zero(dto.getIrrf()), inss = zero(dto.getInss()), outras = zero(dto.getOutrasRetencoes());
        BigDecimal retencoes = iss.add(irrf).add(inss).add(outras);
        if (retencoes.compareTo(bruto) > 0) {
            throw new ResourceBadRequestException("As retenções passam do valor bruto.");
        }
        l.setEmpenho(e);
        l.setNotaFiscal(limpo(dto.getNotaFiscal()));
        l.setNotaEmitidaEm(dto.getNotaEmitidaEm());
        l.setValorBruto(bruto);
        l.setIss(iss);
        l.setIrrf(irrf);
        l.setInss(inss);
        l.setOutrasRetencoes(outras);
        l.setValorLiquido(bruto.subtract(retencoes));
        l.setLiquidadaEm(hoje);
        l.setVencimento(dto.getVencimento() != null ? dto.getVencimento() : hoje.plusDays(VENCIMENTO_PADRAO));
        l.setJustificativa(limpo(dto.getJustificativa()));
        l.setRegistradoEm(Instant.now());
        l.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        liquidacaoRepository.save(l);
        ContextoAuditoria.registro(l.getUuid());
        ContextoAuditoria.detalhe("Liquidação de " + moeda(bruto) + " no empenho " + e.getNumero());
        return dto(l, hoje);
    }

    /**
     * Paga a liquidação ainda não paga (422), com a ordem bancária e a data entre a liquidação e hoje (400). Pagar antes de outra liquidação
     * vencida da mesma fonte pede a justificativa (422, Lei 14.133/2021, art. 141).
     */
    @Transactional
    public LiquidacaoDtos.Liquidacao pagar(UUID id, PagamentoRequestDto dto) {
        controleDeAcesso.exigirAlguma(FinanceiroService.GERENCIAR);
        Liquidacao l = liquidacaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a liquidação " + id + " em nossos registros."));
        ContextoAuditoria.registro(l.getUuid());
        if (l.getPagaEm() != null) {
            throw new ResourceUnprocessableEntityException("A liquidação já foi paga em " + l.getPagaEm() + ".");
        }
        LocalDate hoje = hoje();
        if (dto.getData().isAfter(hoje) || dto.getData().isBefore(l.getLiquidadaEm())) {
            throw new ResourceBadRequestException("O pagamento é entre a liquidação e hoje.");
        }
        FonteRecursoSaude fonte = l.getEmpenho().getDotacao().getFonte();
        long antes = liquidacaoRepository.findTodas().stream()
                .filter(x -> x.getPagaEm() == null && !x.getUuid().equals(l.getUuid()) && x.getEmpenho().getDotacao().getFonte() == fonte)
                .filter(x -> x.getVencimento().isBefore(l.getVencimento()) && x.getVencimento().isBefore(hoje.plusDays(1)))
                .count();
        String justificativa = limpo(dto.getJustificativaForaDeOrdem());
        if (antes > 0 && justificativa == null) {
            throw new ResourceUnprocessableEntityException("Há " + antes + " liquidação(ões) da mesma fonte vencida(s) antes desta; pague na ordem ou justifique.");
        }
        l.setPagaEm(dto.getData());
        l.setOrdemBancaria(dto.getOrdemBancaria().trim());
        l.setJustificativaForaDeOrdem(antes > 0 ? justificativa : null);
        l.setPagoPorCpf(UsuarioAutenticado.cpf());
        liquidacaoRepository.save(l);
        return dto(l, hoje);
    }

    /** As liquidações; {@code aPagar} deixa só as não pagas, na ordem do vencimento. */
    @Transactional(readOnly = true)
    public List<LiquidacaoDtos.Liquidacao> liquidacoes(Boolean aPagar) {
        controleDeAcesso.exigirAlguma(FinanceiroService.CONSULTAR, FinanceiroService.GERENCIAR);
        LocalDate hoje = hoje();
        return liquidacaoRepository.findTodas().stream().filter(l -> !Boolean.TRUE.equals(aPagar) || l.getPagaEm() == null).map(l -> dto(l, hoje)).toList();
    }

    // ── Diárias do TFD ─────────────────────────────────────────────────────────────────────────────

    /**
     * A diária do TFD: a solicitação é de TFD (422) autorizada, programada ou realizada (422); o acompanhante precisa estar na solicitação
     * (422); o credor é pessoa física (422); uma por solicitação e beneficiário (409).
     */
    @Transactional
    public LiquidacaoDtos.Diaria conceder(DiariaTfdRequestDto dto) {
        controleDeAcesso.exigirAlguma(TransporteEletivoService.TFD_AUTORIZAR, FinanceiroService.GERENCIAR);
        SolicitacaoTransporte s = solicitacaoRepository.findById(dto.getSolicitacaoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a solicitação " + dto.getSolicitacaoId() + " em nossos registros."));
        ContextoAuditoria.registro(s.getUuid());
        if (s.getMotivo() != MotivoTransporte.TFD) {
            throw new ResourceUnprocessableEntityException("A diária é do Tratamento Fora de Domicílio.");
        }
        if (!TFD_CONCEDIVEL.contains(s.getSituacao())) {
            throw new ResourceUnprocessableEntityException("O TFD ainda não foi autorizado (está " + s.getSituacao() + ").");
        }
        if (dto.getBeneficiario() == BeneficiarioDiariaTfd.ACOMPANHANTE && limpo(s.getAcompanhanteNome()) == null) {
            throw new ResourceUnprocessableEntityException("A solicitação não tem acompanhante.");
        }
        Fornecedor credor = credor(dto, s);
        if (credor.getDocumento() == null || credor.getDocumento().length() != 11) {
            throw new ResourceUnprocessableEntityException("O credor da diária é a pessoa física (CPF).");
        }
        if (diariaRepository.existsBySolicitacao_UuidAndBeneficiario(s.getUuid(), dto.getBeneficiario())) {
            throw new ResourceConflictException("A diária deste beneficiário já foi concedida.");
        }
        DiariaTfd d = new DiariaTfd();
        d.setSolicitacao(s);
        d.setBeneficiario(dto.getBeneficiario());
        d.setCredor(credor);
        d.setDias(dto.getDias());
        d.setValorDiaria(dto.getValorDiaria());
        d.setValorTotal(dto.getValorDiaria().multiply(BigDecimal.valueOf(dto.getDias())));
        d.setObservacao(limpo(dto.getObservacao()));
        d.setConcedidaEm(hoje());
        d.setConcedidaPorCpf(UsuarioAutenticado.cpf());
        d.setRegistradoEm(Instant.now());
        diariaRepository.save(d);
        return dto(d, false);
    }

    /**
     * O credor da diária: o cadastrado ({@code credorId}, 404) ou, pelo CPF do beneficiário (400 se inválido), o encontrado ou cadastrado com o
     * nome dele. Sem nenhum dos dois, 400.
     */
    private Fornecedor credor(DiariaTfdRequestDto dto, SolicitacaoTransporte s) {
        if (dto.getCredorId() != null) {
            return fornecedorRepository.findById(dto.getCredorId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o credor " + dto.getCredorId() + " em nossos registros."));
        }
        if (limpo(dto.getCredorCpf()) == null) {
            throw new ResourceBadRequestException("Informe o credor ou o CPF do beneficiário.");
        }
        String documento = VigilanciaSanitariaService.documento(dto.getCredorCpf());
        return fornecedorRepository.findByDocumento(documento).orElseGet(() -> {
            Fornecedor f = new Fornecedor();
            f.setDocumento(documento);
            f.setPessoaJuridica(documento.length() == 14);
            String nome = dto.getBeneficiario() == BeneficiarioDiariaTfd.PACIENTE ? s.getPaciente().getNome() : s.getAcompanhanteNome();
            f.setRazaoSocial(nome.length() > 200 ? nome.substring(0, 200) : nome);
            f.setAtivo(true);
            f.setRegistradoEm(Instant.now());
            f.setRegistradoPorCpf(UsuarioAutenticado.cpf());
            return fornecedorRepository.save(f);
        });
    }

    /** As diárias, da solicitação pedida ou todas. */
    @Transactional(readOnly = true)
    public List<LiquidacaoDtos.Diaria> diarias(UUID solicitacaoId) {
        controleDeAcesso.exigirAlguma(TransporteEletivoService.TFD_AUTORIZAR, FinanceiroService.CONSULTAR, FinanceiroService.GERENCIAR);
        Set<UUID> liquidadas = liquidacaoRepository.findTodas().stream().filter(l -> l.getDiaria() != null).map(l -> l.getDiaria().getUuid()).collect(Collectors.toSet());
        return diariaRepository.findTodas().stream().filter(d -> solicitacaoId == null || d.getSolicitacao().getUuid().equals(solicitacaoId))
                .map(d -> dto(d, liquidadas.contains(d.getUuid()))).toList();
    }

    // ── Restos a pagar e execução ──────────────────────────────────────────────────────────────────

    /** Os restos a pagar: os empenhos de exercícios anteriores com o liquidado não pago (processados) ou o empenhado não liquidado. */
    @Transactional(readOnly = true)
    public List<LiquidacaoDtos.RestoAPagar> restosAPagar() {
        controleDeAcesso.exigirAlguma(FinanceiroService.CONSULTAR, FinanceiroService.GERENCIAR);
        int ano = hoje().getYear();
        List<Empenho> anteriores = empenhoRepository.findTodos().stream().filter(e -> e.getExercicio() < ano).toList();
        if (anteriores.isEmpty()) {
            return List.of();
        }
        Map<UUID, BigDecimal> atual = financeiroService.valoresAtuais(anteriores);
        Map<UUID, List<Liquidacao>> porEmpenho = liquidacaoRepository.findDosEmpenhos(atual.keySet()).stream().collect(Collectors.groupingBy(l -> l.getEmpenho().getUuid()));
        List<LiquidacaoDtos.RestoAPagar> lista = new ArrayList<>();
        for (Empenho e : anteriores) {
            List<Liquidacao> l = porEmpenho.getOrDefault(e.getUuid(), List.of());
            BigDecimal liquidado = soma(l.stream().map(Liquidacao::getValorBruto).toList());
            BigDecimal pago = soma(l.stream().filter(x -> x.getPagaEm() != null).map(Liquidacao::getValorBruto).toList());
            BigDecimal processado = liquidado.subtract(pago);
            BigDecimal naoProcessado = atual.get(e.getUuid()).subtract(liquidado);
            if (processado.signum() > 0 || naoProcessado.signum() > 0) {
                lista.add(new LiquidacaoDtos.RestoAPagar(e.getUuid(), e.getNumero(), e.getExercicio(), e.getFornecedor().getRazaoSocial(), processado, naoProcessado));
            }
        }
        return lista;
    }

    /** A execução do exercício por dotação e por fonte: autorizado, empenhado, liquidado e pago; a fila a liquidar, as vencidas e o prazo médio. */
    @Transactional(readOnly = true)
    public LiquidacaoDtos.Execucao execucao(Integer exercicio) {
        controleDeAcesso.exigirAlguma(FinanceiroService.CONSULTAR, FinanceiroService.GERENCIAR);
        LocalDate hoje = hoje();
        int ano = exercicio != null ? exercicio : hoje.getYear();
        List<FinanceiroDtos.Dotacao> dotacoes = financeiroService.dotacoes(ano);
        List<Liquidacao> todas = liquidacaoRepository.findTodas();
        List<Liquidacao> doExercicio = todas.stream().filter(l -> l.getEmpenho().getExercicio() == ano).toList();
        Map<UUID, List<Liquidacao>> porDotacao = doExercicio.stream().collect(Collectors.groupingBy(l -> l.getEmpenho().getDotacao().getUuid()));
        List<LiquidacaoDtos.ExecucaoDotacao> linhas = dotacoes.stream().map(d -> {
            List<Liquidacao> l = porDotacao.getOrDefault(d.uuid(), List.of());
            return new LiquidacaoDtos.ExecucaoDotacao(d.uuid(), d.programaTrabalho() + " · " + d.naturezaDespesa(), d.fonte(), d.autorizado(), d.empenhado(),
                    soma(l.stream().map(Liquidacao::getValorBruto).toList()), soma(l.stream().filter(x -> x.getPagaEm() != null).map(Liquidacao::getValorBruto).toList()));
        }).toList();
        List<LiquidacaoDtos.ExecucaoFonte> fontes = linhas.stream().collect(Collectors.groupingBy(LiquidacaoDtos.ExecucaoDotacao::fonte, () -> new EnumMap<>(FonteRecursoSaude.class), Collectors.toList()))
                .entrySet().stream().map(x -> new LiquidacaoDtos.ExecucaoFonte(x.getKey(),
                        soma(x.getValue().stream().map(LiquidacaoDtos.ExecucaoDotacao::autorizado).toList()),
                        soma(x.getValue().stream().map(LiquidacaoDtos.ExecucaoDotacao::empenhado).toList()),
                        soma(x.getValue().stream().map(LiquidacaoDtos.ExecucaoDotacao::liquidado).toList()),
                        soma(x.getValue().stream().map(LiquidacaoDtos.ExecucaoDotacao::pago).toList()))).toList();
        List<LiquidacaoDtos.Pendencia> pendencias = pendencias();
        List<Liquidacao> vencidas = todas.stream().filter(l -> l.getPagaEm() == null && l.getVencimento().isBefore(hoje)).toList();
        OptionalDouble prazo = doExercicio.stream().filter(l -> l.getPagaEm() != null && atesto(l) != null)
                .mapToLong(l -> ChronoUnit.DAYS.between(atesto(l), l.getPagaEm())).average();
        return new LiquidacaoDtos.Execucao(ano, linhas, fontes, pendencias.size(), soma(pendencias.stream().map(LiquidacaoDtos.Pendencia::valor).toList()),
                vencidas.size(), soma(vencidas.stream().map(Liquidacao::getValorLiquido).toList()),
                prazo.isPresent() ? BigDecimal.valueOf(prazo.getAsDouble()).setScale(1, RoundingMode.HALF_UP).doubleValue() : null);
    }

    // ── Auxiliares ─────────────────────────────────────────────────────────────────────────────────

    /** O que o empenho ainda pode liquidar: o valor atual menos o já liquidado. */
    BigDecimal saldoALiquidar(Empenho e) {
        BigDecimal atual = financeiroService.valoresAtuais(List.of(e)).get(e.getUuid());
        return atual.subtract(soma(liquidacaoRepository.findDosEmpenhos(List.of(e.getUuid())).stream().map(Liquidacao::getValorBruto).toList()));
    }

    private Map<UUID, BigDecimal> valoresDosRecebimentos(List<RecebimentoCompra> recebimentos) {
        if (recebimentos.isEmpty()) {
            return Map.of();
        }
        Map<UUID, BigDecimal> valores = new HashMap<>();
        for (ItemRecebimentoCompra i : itemRecebimentoRepository.findDosRecebimentos(recebimentos.stream().map(RecebimentoCompra::getUuid).toList())) {
            int aceita = i.getQuantidadeAceita() != null ? i.getQuantidadeAceita() : 0;
            valores.merge(i.getRecebimento().getUuid(), i.getItemOrdem().getItemInstrumento().getPrecoUnitario().multiply(BigDecimal.valueOf(aceita)), BigDecimal::add);
        }
        return valores;
    }

    private static LocalDate atesto(Liquidacao l) {
        return switch (l.getOrigem()) {
            case RECEBIMENTO -> l.getRecebimento().getDefinitivoEm();
            case ORDEM_SERVICO -> l.getOrdemServico().getConcluidaEm();
            case DIARIA_TFD -> l.getDiaria().getConcedidaEm();
            case AVULSA -> null;
        };
    }

    private LiquidacaoDtos.Liquidacao dto(Liquidacao l, LocalDate hoje) {
        Empenho e = l.getEmpenho();
        UUID origemId = switch (l.getOrigem()) {
            case RECEBIMENTO -> l.getRecebimento().getUuid();
            case ORDEM_SERVICO -> l.getOrdemServico().getUuid();
            case DIARIA_TFD -> l.getDiaria().getUuid();
            case AVULSA -> null;
        };
        String descricao = switch (l.getOrigem()) {
            case RECEBIMENTO -> "Ordem " + l.getRecebimento().getOrdem().getNumero() + " · nota " + l.getRecebimento().getNotaFiscal();
            case ORDEM_SERVICO -> "Ordem de serviço " + l.getOrdemServico().getNumero();
            case DIARIA_TFD -> "Diária do TFD";
            case AVULSA -> "Avulsa · " + l.getJustificativa();
        };
        return new LiquidacaoDtos.Liquidacao(l.getUuid(), l.getOrigem(), origemId, descricao, e.getUuid(), e.getNumero(), e.getFornecedor().getRazaoSocial(),
                e.getDotacao().getFonte(), l.getNotaFiscal(), l.getNotaEmitidaEm(), l.getValorBruto(), l.getIss(), l.getIrrf(), l.getInss(), l.getOutrasRetencoes(),
                l.getValorLiquido(), l.getLiquidadaEm(), l.getVencimento(), l.getPagaEm() == null && l.getVencimento().isBefore(hoje), l.getPagaEm() != null,
                l.getPagaEm(), l.getOrdemBancaria(), l.getJustificativa(), l.getJustificativaForaDeOrdem(),
                l.getCentroCusto() == null ? null : l.getCentroCusto().getCodigo() + " · " + l.getCentroCusto().getNome());
    }

    private static LiquidacaoDtos.Diaria dto(DiariaTfd d, boolean liquidada) {
        SolicitacaoTransporte s = d.getSolicitacao();
        String beneficiario = d.getBeneficiario() == BeneficiarioDiariaTfd.PACIENTE ? s.getPaciente().getNome() : s.getAcompanhanteNome();
        String destino = s.getDestinoMunicipio() != null ? s.getDestinoMunicipio() : s.getDestino();
        return new LiquidacaoDtos.Diaria(d.getUuid(), s.getUuid(), s.getPaciente().getNome(), destino, d.getBeneficiario(), beneficiario,
                d.getCredor().getUuid(), d.getCredor().getRazaoSocial(), d.getDias(), d.getValorDiaria(), d.getValorTotal(), d.getConcedidaEm(), liquidada);
    }

    private static BigDecimal zero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static BigDecimal soma(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String moeda(BigDecimal v) {
        return java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(v);
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }
}
