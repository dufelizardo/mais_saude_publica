package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ManutencaoDtos;
import com.edufelizardo.maissaudepublica.models.enuns.*;
import com.edufelizardo.maissaudepublica.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manutenção, fatia PM2 (ADR-0216): o plano do bem (preventiva, calibração, segurança elétrica) gera a ordem de serviço quando vence, na
 * leitura; o chamado corretivo é aberto por quem usa o bem; a manutenção faz a triagem, executa (equipe interna ou contrato de serviço) e
 * conclui com o serviço, as peças (que saem do Estoque da unidade) e o custo. O bem e o veículo ligado ficam em manutenção na execução.
 */
@Service
public class ManutencaoService {

    public static final String EXECUTAR = "MANUTENCAO.EXECUTAR";
    static final int DIAS_VENCENDO = 30;
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final Set<SituacaoOrdemServico> EM_ABERTO = EnumSet.of(SituacaoOrdemServico.ABERTA, SituacaoOrdemServico.EM_EXECUCAO);
    private static final Set<SituacaoBem> EM_OPERACAO = EnumSet.of(SituacaoBem.EM_USO, SituacaoBem.OCIOSO, SituacaoBem.EM_MANUTENCAO);

    @Autowired private PlanoManutencaoRepository planoRepository;
    @Autowired private OrdemServicoManutencaoRepository ordemRepository;
    @Autowired private PecaOrdemServicoRepository pecaRepository;
    @Autowired private BemPatrimonialRepository bemRepository;
    @Autowired private ResponsavelBemRepository responsavelRepository;
    @Autowired private InstrumentoContratualRepository instrumentoRepository;
    @Autowired private ItemInstrumentoRepository itemInstrumentoRepository;
    @Autowired private MaterialEstoqueRepository materialRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private EstoqueService estoqueService;
    @Autowired private TransporteService transporteService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Planos ─────────────────────────────────────────────────────────────────────────────────────

    /**
     * O plano do bem tombado e em operação (422): preventiva, calibração ou segurança elétrica (a corretiva não tem plano, 400), um ativo
     * por tipo (409). A próxima data conta da última execução informada (não futura, 400) ou de hoje.
     */
    @Transactional
    public ManutencaoDtos.DoBem criarPlano(UUID bemId, PlanoManutencaoRequestDto dto) {
        BemPatrimonial b = bemOu404(bemId);
        controleDeAcesso.exigir(EXECUTAR, b.getUnidade());
        ContextoAuditoria.registro(b.getUuid());
        exigirEmOperacao(b);
        if (dto.getTipo() == TipoManutencao.CORRETIVA) {
            throw new ResourceBadRequestException("A corretiva nasce do chamado; o plano é de preventiva, calibração ou segurança elétrica.");
        }
        LocalDate hoje = hoje();
        if (dto.getUltimaExecucao() != null && dto.getUltimaExecucao().isAfter(hoje)) {
            throw new ResourceBadRequestException("A última execução não pode ser futura.");
        }
        if (planoRepository.findDoBem(b.getUuid()).stream().anyMatch(p -> p.isAtivo() && p.getTipo() == dto.getTipo())) {
            throw new ResourceConflictException("O bem já tem um plano ativo de " + rotulo(dto.getTipo()) + ".");
        }
        PlanoManutencao p = new PlanoManutencao();
        p.setBem(b);
        p.setTipo(dto.getTipo());
        p.setPeriodicidadeDias(dto.getPeriodicidadeDias());
        p.setUltimaExecucao(dto.getUltimaExecucao());
        p.setProximaEm((dto.getUltimaExecucao() != null ? dto.getUltimaExecucao() : hoje).plusDays(dto.getPeriodicidadeDias()));
        p.setDescricao(limpo(dto.getDescricao()));
        p.setAtivo(true);
        p.setRegistradoEm(Instant.now());
        p.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        planoRepository.save(p);
        ContextoAuditoria.detalhe("Plano de " + rotulo(p.getTipo()) + " a cada " + p.getPeriodicidadeDias() + " dias");
        return doBem(b);
    }

    /** Desativa o plano (422 se já inativo); as ordens já abertas continuam. */
    @Transactional
    public ManutencaoDtos.DoBem desativarPlano(UUID planoId) {
        PlanoManutencao p = planoRepository.findById(planoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o plano " + planoId + " em nossos registros."));
        controleDeAcesso.exigir(EXECUTAR, p.getBem().getUnidade());
        ContextoAuditoria.registro(p.getBem().getUuid());
        if (!p.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O plano já está inativo.");
        }
        p.setAtivo(false);
        planoRepository.save(p);
        return doBem(p.getBem());
    }

    // ── Chamado e consulta ─────────────────────────────────────────────────────────────────────────

    /** O chamado corretivo, por quem está logado, do bem tombado e em operação (422); o bem parado começa a contar o tempo parado. */
    @Transactional
    public ManutencaoDtos.Ordem abrirChamado(UUID bemId, ChamadoManutencaoRequestDto dto) {
        BemPatrimonial b = bemOu404(bemId);
        ContextoAuditoria.registro(b.getUuid());
        ContextoAuditoria.unidade(b.getUnidade());
        exigirEmOperacao(b);
        LocalDate hoje = hoje();
        OrdemServicoManutencao o = nova(b, TipoManutencao.CORRETIVA, null, hoje);
        o.setDefeito(dto.getDefeito().trim());
        o.setBemParado(Boolean.TRUE.equals(dto.getBemParado()));
        o.setParadoDesde(o.isBemParado() ? hoje : null);
        ordemRepository.save(o);
        ContextoAuditoria.detalhe("Chamado " + o.getNumero() + " do bem " + b.getNumeroTombamento());
        return dto(o, List.of());
    }

    /** Os chamados abertos por quem está logado ("Meus chamados"). */
    @Transactional(readOnly = true)
    public List<ManutencaoDtos.Ordem> meusChamados() {
        String cpf = UsuarioAutenticado.cpf();
        return cpf == null ? List.of() : dtos(ordemRepository.findAbertasPor(cpf));
    }

    /** As ordens das unidades visíveis, com as do plano vencido já geradas; filtra por situação e unidade. */
    @Transactional
    public List<ManutencaoDtos.Ordem> ordens(SituacaoOrdemServico situacao, UUID unidadeId) {
        controleDeAcesso.exigirAlguma(EXECUTAR);
        gerarVencidas();
        return dtos(visiveis(ordemRepository.findTodas().stream())
                .filter(o -> situacao == null || o.getSituacao() == situacao)
                .filter(o -> unidadeId == null || o.getBem().getUnidade().getUuid().equals(unidadeId))
                .toList());
    }

    /** A ordem: para a manutenção da unidade ou para quem abriu o chamado (403 aos outros). */
    @Transactional(readOnly = true)
    public ManutencaoDtos.Ordem ordem(UUID id) {
        OrdemServicoManutencao o = ordemOu404(id);
        String cpf = UsuarioAutenticado.cpf();
        if (cpf == null || !cpf.equals(o.getAbertaPorCpf())) {
            controleDeAcesso.exigirAlguma(EXECUTAR);
            controleDeAcesso.exigirVisivel(o.getBem().getUnidade(), EXECUTAR);
        }
        return dtos(List.of(o)).get(0);
    }

    /** A manutenção na ficha do bem: para quem vê o bem (Patrimônio ou Manutenção da unidade) ou para o responsável. */
    @Transactional
    public ManutencaoDtos.DoBem doBemConsultado(UUID bemId) {
        BemPatrimonial b = bemOu404(bemId);
        String cpf = UsuarioAutenticado.cpf();
        boolean responsavel = cpf != null && responsavelRepository.findAtuais(List.of(b.getUuid())).stream().anyMatch(r -> cpf.equals(r.getProfissional().getCpf()));
        if (!responsavel) {
            controleDeAcesso.exigirAlguma(EXECUTAR, PatrimonioService.CONSULTAR, PatrimonioService.GERENCIAR);
            controleDeAcesso.exigirVisivel(b.getUnidade(), EXECUTAR, PatrimonioService.CONSULTAR, PatrimonioService.GERENCIAR);
        }
        gerarVencidas();
        return doBem(b);
    }

    /** O resumo da manutenção nas unidades visíveis: ordens abertas, em execução e com bem parado; planos vencendo em 30 dias; calibrações vencidas. */
    @Transactional
    public ManutencaoDtos.Resumo resumo() {
        controleDeAcesso.exigirAlguma(EXECUTAR);
        gerarVencidas();
        LocalDate hoje = hoje();
        List<OrdemServicoManutencao> ordens = visiveis(ordemRepository.findTodas().stream()).toList();
        List<PlanoManutencao> planos = controleDeAcesso.filtrar(planoRepository.findAtivos().stream(), p -> Stream.of(p.getBem().getUnidade()), EXECUTAR)
                .filter(p -> p.getBem().getSituacao() != SituacaoBem.BAIXADO).toList();
        Map<UUID, LocalDate> validade = validadesDaCalibracao(ordens);
        List<ManutencaoDtos.Plano> vencendo = planos.stream().filter(p -> !p.getProximaEm().isAfter(hoje.plusDays(DIAS_VENCENDO)))
                .map(p -> dto(p, hoje)).toList();
        List<ManutencaoDtos.Plano> calibracoes = planos.stream().filter(p -> p.getTipo() == TipoManutencao.CALIBRACAO)
                .filter(p -> p.getProximaEm().isBefore(hoje) || Optional.ofNullable(validade.get(p.getBem().getUuid())).map(v -> v.isBefore(hoje)).orElse(false))
                .map(p -> dto(p, hoje)).toList();
        return new ManutencaoDtos.Resumo(ordens.stream().filter(o -> o.getSituacao() == SituacaoOrdemServico.ABERTA).count(),
                ordens.stream().filter(o -> o.getSituacao() == SituacaoOrdemServico.EM_EXECUCAO).count(),
                ordens.stream().filter(o -> EM_ABERTO.contains(o.getSituacao()) && o.getParadoDesde() != null).count(), vencendo, calibracoes);
    }

    /** As atas e os contratos vigentes com item de serviço, para quem executa escolher a execução por contrato sem a permissão de Compras. */
    @Transactional(readOnly = true)
    public List<ManutencaoDtos.ContratoServico> contratosDeServico() {
        controleDeAcesso.exigirAlguma(EXECUTAR);
        LocalDate hoje = hoje();
        List<InstrumentoContratual> vigentes = instrumentoRepository.findTodos().stream()
                .filter(i -> !i.getInicio().isAfter(hoje) && !i.getFim().isBefore(hoje)).toList();
        if (vigentes.isEmpty()) {
            return List.of();
        }
        Set<UUID> comServico = itemInstrumentoRepository.findDosInstrumentos(vigentes.stream().map(InstrumentoContratual::getUuid).toList()).stream()
                .filter(i -> i.getItemContratacao().getTipo() == TipoItemCompra.SERVICO).map(i -> i.getInstrumento().getUuid()).collect(Collectors.toSet());
        return vigentes.stream().filter(i -> comServico.contains(i.getUuid()))
                .map(i -> new ManutencaoDtos.ContratoServico(i.getUuid(), i.getNumero(), i.getFornecedor().getRazaoSocial(), i.getFim())).toList();
    }

    // ── Ordem de serviço ───────────────────────────────────────────────────────────────────────────

    /** A prioridade, só da ordem aberta (422). */
    @Transactional
    public ManutencaoDtos.Ordem triar(UUID id, TriagemOrdemServicoRequestDto dto) {
        OrdemServicoManutencao o = ordemOu404(id);
        controleDeAcesso.exigir(EXECUTAR, o.getBem().getUnidade());
        ContextoAuditoria.registro(o.getBem().getUuid());
        if (o.getSituacao() != SituacaoOrdemServico.ABERTA) {
            throw new ResourceUnprocessableEntityException("A ordem " + o.getNumero() + " não está aberta.");
        }
        o.setPrioridade(dto.getPrioridade());
        ordemRepository.save(o);
        return dtos(List.of(o)).get(0);
    }

    /**
     * Inicia a ordem aberta (422): a equipe interna pede o técnico (400); o contrato pede a ata ou o contrato (400) vigente (422) com item
     * de serviço (422). O bem fica em manutenção e o veículo ligado também (o veículo em serviço não entra, 422).
     */
    @Transactional
    public ManutencaoDtos.Ordem iniciar(UUID id, InicioOrdemServicoRequestDto dto) {
        OrdemServicoManutencao o = ordemOu404(id);
        BemPatrimonial b = o.getBem();
        controleDeAcesso.exigir(EXECUTAR, b.getUnidade());
        ContextoAuditoria.registro(b.getUuid());
        if (o.getSituacao() != SituacaoOrdemServico.ABERTA) {
            throw new ResourceUnprocessableEntityException("A ordem " + o.getNumero() + " não está aberta.");
        }
        exigirEmOperacao(b);
        LocalDate hoje = hoje();
        if (dto.getExecucao() == ExecucaoManutencao.INTERNA) {
            if (limpo(dto.getTecnico()) == null) {
                throw new ResourceBadRequestException("Informe o técnico da equipe interna.");
            }
            o.setInstrumento(null);
        } else {
            if (dto.getInstrumentoId() == null) {
                throw new ResourceBadRequestException("Informe a ata ou o contrato de serviço.");
            }
            InstrumentoContratual ins = instrumentoRepository.findById(dto.getInstrumentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a ata ou o contrato " + dto.getInstrumentoId() + " em nossos registros."));
            if (ins.getInicio().isAfter(hoje) || ins.getFim().isBefore(hoje)) {
                throw new ResourceUnprocessableEntityException("A ata ou o contrato " + ins.getNumero() + " está fora da vigência.");
            }
            boolean temServico = itemInstrumentoRepository.findDosInstrumentos(List.of(ins.getUuid())).stream()
                    .anyMatch(i -> i.getItemContratacao().getTipo() == TipoItemCompra.SERVICO);
            if (!temServico) {
                throw new ResourceUnprocessableEntityException("A ata ou o contrato " + ins.getNumero() + " não tem item de serviço.");
            }
            o.setInstrumento(ins);
        }
        if (b.getVeiculo() != null) {
            transporteService.entrarEmManutencao(b.getVeiculo(), "Ordem de serviço " + o.getNumero());
        }
        o.setExecucao(dto.getExecucao());
        o.setTecnico(limpo(dto.getTecnico()));
        o.setSituacaoBemAnterior(b.getSituacao() == SituacaoBem.EM_MANUTENCAO ? null : b.getSituacao());
        o.setSituacao(SituacaoOrdemServico.EM_EXECUCAO);
        o.setIniciadaEm(hoje);
        if (o.getPrioridade() == null) {
            o.setPrioridade(PrioridadeManutencao.NORMAL);
        }
        if (o.getParadoDesde() == null) {
            o.setParadoDesde(hoje);
        }
        ordemRepository.save(o);
        b.setSituacao(SituacaoBem.EM_MANUTENCAO);
        bemRepository.save(b);
        ContextoAuditoria.detalhe("Ordem " + o.getNumero() + " em execução");
        return dtos(List.of(o)).get(0);
    }

    /**
     * Conclui a ordem em execução (422): o serviço; na calibração, o certificado e a validade futura (400). Cada peça é do Estoque (o
     * material, 404) ou descrita (400), e a do Estoque sai da unidade do bem (sem saldo, 422). O bem volta à situação de antes, o veículo
     * volta a disponível e o plano conta a próxima a partir de hoje.
     */
    @Transactional
    public ManutencaoDtos.Ordem concluir(UUID id, ConclusaoOrdemServicoRequestDto dto) {
        OrdemServicoManutencao o = ordemOu404(id);
        BemPatrimonial b = o.getBem();
        controleDeAcesso.exigir(EXECUTAR, b.getUnidade());
        ContextoAuditoria.registro(b.getUuid());
        if (o.getSituacao() != SituacaoOrdemServico.EM_EXECUCAO) {
            throw new ResourceUnprocessableEntityException("A ordem " + o.getNumero() + " não está em execução.");
        }
        LocalDate hoje = hoje();
        if (o.getTipo() == TipoManutencao.CALIBRACAO) {
            if (limpo(dto.getCertificado()) == null || dto.getCalibracaoValidaAte() == null || !dto.getCalibracaoValidaAte().isAfter(hoje)) {
                throw new ResourceBadRequestException("A calibração pede o número do certificado e a validade futura.");
            }
            o.setCertificado(dto.getCertificado().trim());
            o.setCalibracaoValidaAte(dto.getCalibracaoValidaAte());
        }
        Profissional quem = UsuarioAutenticado.cpf() == null ? null : profissionalRepository.findByCpfAndAtivoTrue(UsuarioAutenticado.cpf()).orElse(null);
        List<PecaOrdemServico> pecas = new ArrayList<>();
        int n = 0;
        for (ConclusaoOrdemServicoRequestDto.Peca d : dto.getPecas() == null ? List.<ConclusaoOrdemServicoRequestDto.Peca>of() : dto.getPecas()) {
            n++;
            PecaOrdemServico p = new PecaOrdemServico();
            p.setOrdem(o);
            p.setQuantidade(d.getQuantidade());
            p.setCustoUnitario(d.getCustoUnitario());
            if (d.getMaterialId() != null) {
                MaterialEstoque m = materialRepository.findById(d.getMaterialId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar o material " + d.getMaterialId() + " em nossos registros."));
                estoqueService.saidaPelaManutencao(b.getUnidade(), m, d.getQuantidade(), quem, "Ordem de serviço " + o.getNumero() + " do bem " + b.getNumeroTombamento());
                p.setMaterial(m);
                p.setDescricao(m.getNome().length() > 300 ? m.getNome().substring(0, 300) : m.getNome());
            } else if (limpo(d.getDescricao()) == null) {
                throw new ResourceBadRequestException("Peça " + n + ": escolha o material do Estoque ou descreva a peça.");
            } else {
                p.setDescricao(d.getDescricao().trim());
            }
            pecas.add(p);
        }
        pecaRepository.saveAll(pecas);
        o.setServicoRealizado(dto.getServicoRealizado().trim());
        o.setCustoMaoDeObra(dto.getCustoMaoDeObra());
        o.setSituacao(SituacaoOrdemServico.CONCLUIDA);
        o.setConcluidaEm(hoje);
        ordemRepository.save(o);
        devolverBem(o);
        if (o.getPlano() != null) {
            PlanoManutencao plano = o.getPlano();
            plano.setUltimaExecucao(hoje);
            plano.setProximaEm(hoje.plusDays(plano.getPeriodicidadeDias()));
            planoRepository.save(plano);
        }
        ContextoAuditoria.detalhe("Ordem " + o.getNumero() + " concluída");
        return dtos(List.of(o)).get(0);
    }

    /**
     * Cancela a ordem aberta ou em execução (422), com o motivo; na execução, o bem e o veículo voltam ao que eram. A ordem do plano
     * cancelada passa o plano para o próximo ciclo.
     */
    @Transactional
    public ManutencaoDtos.Ordem cancelar(UUID id, MotivoRequestDto dto) {
        OrdemServicoManutencao o = ordemOu404(id);
        controleDeAcesso.exigir(EXECUTAR, o.getBem().getUnidade());
        ContextoAuditoria.registro(o.getBem().getUuid());
        if (!EM_ABERTO.contains(o.getSituacao())) {
            throw new ResourceUnprocessableEntityException("A ordem " + o.getNumero() + " já foi " + (o.getSituacao() == SituacaoOrdemServico.CONCLUIDA ? "concluída." : "cancelada."));
        }
        boolean emExecucao = o.getSituacao() == SituacaoOrdemServico.EM_EXECUCAO;
        o.setSituacao(SituacaoOrdemServico.CANCELADA);
        o.setMotivoCancelamento(dto.getMotivo().trim());
        o.setConcluidaEm(hoje());
        ordemRepository.save(o);
        if (emExecucao) {
            devolverBem(o);
        }
        if (o.getPlano() != null && o.getPlano().isAtivo()) {
            // Sem isso o plano ficaria vencido sem ordem: a do ciclo foi cancelada, e a próxima conta a partir de hoje.
            o.getPlano().setProximaEm(hoje().plusDays(o.getPlano().getPeriodicidadeDias()));
            planoRepository.save(o.getPlano());
        }
        return dtos(List.of(o)).get(0);
    }

    // ── Auxiliares ─────────────────────────────────────────────────────────────────────────────────

    /**
     * A ordem do plano vencido nasce na leitura (sem rotina agendada, ADR-0209): uma por vencimento, ou seja, só se não houver ordem do
     * plano aberta desde a data prevista.
     */
    void gerarVencidas() {
        LocalDate hoje = hoje();
        List<PlanoManutencao> vencidos = planoRepository.findAtivos().stream()
                .filter(p -> !p.getProximaEm().isAfter(hoje) && EnumSet.of(SituacaoBem.EM_USO, SituacaoBem.OCIOSO, SituacaoBem.EM_MANUTENCAO).contains(p.getBem().getSituacao()))
                .toList();
        if (vencidos.isEmpty()) {
            return;
        }
        Set<UUID> comOrdem = ordemRepository.findTodas().stream().filter(o -> o.getPlano() != null)
                .filter(o -> !o.getAbertaEm().isBefore(o.getPlano().getProximaEm()) || EM_ABERTO.contains(o.getSituacao()))
                .map(o -> o.getPlano().getUuid()).collect(Collectors.toSet());
        for (PlanoManutencao p : vencidos) {
            if (!comOrdem.contains(p.getUuid())) {
                OrdemServicoManutencao o = nova(p.getBem(), p.getTipo(), p, hoje);
                o.setPrioridade(PrioridadeManutencao.NORMAL);
                ordemRepository.save(o);
            }
        }
    }

    private OrdemServicoManutencao nova(BemPatrimonial b, TipoManutencao tipo, PlanoManutencao plano, LocalDate hoje) {
        OrdemServicoManutencao o = new OrdemServicoManutencao();
        String prefixo = hoje.getYear() + "/";
        String maior = ordemRepository.findMaiorNumero(prefixo);
        o.setNumero(prefixo + String.format("%06d", maior == null ? 1 : Long.parseLong(maior.substring(prefixo.length())) + 1));
        o.setBem(b);
        o.setPlano(plano);
        o.setTipo(tipo);
        o.setSituacao(SituacaoOrdemServico.ABERTA);
        o.setAbertaEm(hoje);
        o.setAbertaPorCpf(plano == null ? UsuarioAutenticado.cpf() : null);
        o.setRegistradoEm(Instant.now());
        return o;
    }

    /** Ao concluir ou cancelar a execução: sem outra ordem em execução no bem, ele volta à situação de antes e o veículo sai da manutenção. */
    private void devolverBem(OrdemServicoManutencao o) {
        BemPatrimonial b = o.getBem();
        boolean outra = ordemRepository.findDoBem(b.getUuid()).stream()
                .anyMatch(x -> !x.getUuid().equals(o.getUuid()) && x.getSituacao() == SituacaoOrdemServico.EM_EXECUCAO);
        if (outra || b.getSituacao() != SituacaoBem.EM_MANUTENCAO) {
            return;
        }
        b.setSituacao(o.getSituacaoBemAnterior() != null ? o.getSituacaoBemAnterior() : SituacaoBem.EM_USO);
        bemRepository.save(b);
        if (b.getVeiculo() != null) {
            transporteService.sairDaManutencao(b.getVeiculo(), "Fim da ordem de serviço " + o.getNumero());
        }
    }

    private ManutencaoDtos.DoBem doBem(BemPatrimonial b) {
        LocalDate hoje = hoje();
        List<ManutencaoDtos.Plano> planos = planoRepository.findDoBem(b.getUuid()).stream().map(p -> dto(p, hoje)).toList();
        List<OrdemServicoManutencao> lista = ordemRepository.findDoBem(b.getUuid());
        List<ManutencaoDtos.Ordem> ordens = dtos(lista);
        BigDecimal custo = ordens.stream().filter(o -> o.situacao() == SituacaoOrdemServico.CONCLUIDA).map(ManutencaoDtos.Ordem::custoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long parado = ordens.stream().filter(o -> o.situacao() != SituacaoOrdemServico.CANCELADA).map(ManutencaoDtos.Ordem::diasParado)
                .filter(Objects::nonNull).mapToLong(Long::longValue).sum();
        LocalDate validade = validadesDaCalibracao(lista).get(b.getUuid());
        boolean vencida = (validade != null && validade.isBefore(hoje))
                || planos.stream().anyMatch(p -> p.ativo() && p.tipo() == TipoManutencao.CALIBRACAO && p.vencido());
        return new ManutencaoDtos.DoBem(planos, ordens, custo, parado, validade, vencida);
    }

    /** A validade do último certificado de calibração de cada bem. */
    private static Map<UUID, LocalDate> validadesDaCalibracao(List<OrdemServicoManutencao> ordens) {
        Map<UUID, OrdemServicoManutencao> ultima = new HashMap<>();
        for (OrdemServicoManutencao o : ordens) {
            if (o.getTipo() == TipoManutencao.CALIBRACAO && o.getSituacao() == SituacaoOrdemServico.CONCLUIDA && o.getCalibracaoValidaAte() != null) {
                ultima.merge(o.getBem().getUuid(), o, (a, c) -> c.getConcluidaEm().isAfter(a.getConcluidaEm()) ? c : a);
            }
        }
        return ultima.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getCalibracaoValidaAte()));
    }

    private Stream<OrdemServicoManutencao> visiveis(Stream<OrdemServicoManutencao> ordens) {
        return controleDeAcesso.filtrar(ordens, o -> Stream.of(o.getBem().getUnidade()), EXECUTAR);
    }

    private List<ManutencaoDtos.Ordem> dtos(List<OrdemServicoManutencao> ordens) {
        if (ordens.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<PecaOrdemServico>> pecas = pecaRepository.findDasOrdens(ordens.stream().map(OrdemServicoManutencao::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(p -> p.getOrdem().getUuid()));
        return ordens.stream().map(o -> dto(o, pecas.getOrDefault(o.getUuid(), List.of()))).toList();
    }

    private ManutencaoDtos.Ordem dto(OrdemServicoManutencao o, List<PecaOrdemServico> pecas) {
        BemPatrimonial b = o.getBem();
        BigDecimal custoPecas = pecas.stream().filter(p -> p.getCustoUnitario() != null)
                .map(p -> p.getCustoUnitario().multiply(BigDecimal.valueOf(p.getQuantidade()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = custoPecas.add(o.getCustoMaoDeObra() != null ? o.getCustoMaoDeObra() : BigDecimal.ZERO);
        Long dias = o.getParadoDesde() == null ? null : ChronoUnit.DAYS.between(o.getParadoDesde(), o.getConcluidaEm() != null ? o.getConcluidaEm() : hoje());
        return new ManutencaoDtos.Ordem(o.getUuid(), o.getNumero(), b.getUuid(), b.getNumeroTombamento(), b.getDescricao(), b.getUnidade().getUuid(),
                b.getUnidade().getNome(), o.getTipo(), o.getPlano() != null ? o.getPlano().getUuid() : null, o.getDefeito(), o.isBemParado(), o.getParadoDesde(),
                o.getAbertaEm(), o.getPrioridade(), o.getSituacao(), o.getExecucao(), o.getInstrumento() != null ? o.getInstrumento().getUuid() : null,
                o.getInstrumento() != null ? o.getInstrumento().getNumero() : null, o.getTecnico(), o.getIniciadaEm(), o.getConcluidaEm(),
                o.getServicoRealizado(), o.getCustoMaoDeObra(), custoPecas, total, dias, o.getCertificado(), o.getCalibracaoValidaAte(),
                o.getMotivoCancelamento(), pecas.stream().map(p -> new ManutencaoDtos.Peca(p.getUuid(), p.getMaterial() != null ? p.getMaterial().getUuid() : null,
                p.getDescricao(), p.getQuantidade(), p.getCustoUnitario())).toList());
    }

    private static ManutencaoDtos.Plano dto(PlanoManutencao p, LocalDate hoje) {
        BemPatrimonial b = p.getBem();
        return new ManutencaoDtos.Plano(p.getUuid(), b.getUuid(), b.getNumeroTombamento(), b.getDescricao(), b.getUnidade().getNome(), p.getTipo(),
                p.getPeriodicidadeDias(), p.getUltimaExecucao(), p.getProximaEm(), p.getDescricao(), p.isAtivo(), p.isAtivo() && p.getProximaEm().isBefore(hoje));
    }

    private static void exigirEmOperacao(BemPatrimonial b) {
        if (!EM_OPERACAO.contains(b.getSituacao())) {
            throw new ResourceUnprocessableEntityException(b.getSituacao() == SituacaoBem.A_TOMBAR ? "O bem ainda não foi tombado." : "O bem está baixado.");
        }
    }

    private static String rotulo(TipoManutencao t) {
        return switch (t) {
            case CORRETIVA -> "corretiva";
            case PREVENTIVA -> "preventiva";
            case CALIBRACAO -> "calibração";
            case SEGURANCA_ELETRICA -> "segurança elétrica";
        };
    }

    private BemPatrimonial bemOu404(UUID id) {
        return bemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o bem " + id + " em nossos registros."));
    }

    private OrdemServicoManutencao ordemOu404(UUID id) {
        return ordemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a ordem de serviço " + id + " em nossos registros."));
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }
}
