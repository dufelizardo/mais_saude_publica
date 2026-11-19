package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Fornecedor;
import com.edufelizardo.maissaudepublica.models.InstrumentoContratual;
import com.edufelizardo.maissaudepublica.models.ItemContratacao;
import com.edufelizardo.maissaudepublica.models.ItemInstrumento;
import com.edufelizardo.maissaudepublica.models.ItemOrdemFornecimento;
import com.edufelizardo.maissaudepublica.models.ItemRecebimentoCompra;
import com.edufelizardo.maissaudepublica.models.OcorrenciaFornecedor;
import com.edufelizardo.maissaudepublica.models.OrdemFornecimento;
import com.edufelizardo.maissaudepublica.models.RecebimentoCompra;
import com.edufelizardo.maissaudepublica.models.SancaoFornecedor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EntradaEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.OcorrenciaFornecedorRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.OrdemFornecimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RecebimentoCompraRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RecebimentoDefinitivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SancaoFornecedorRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EntregasDtos;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemEntradaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaFornecedor;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOrdemFornecimento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRecebimentoCompra;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoCompra;
import com.edufelizardo.maissaudepublica.models.enuns.TipoItemCompra;
import com.edufelizardo.maissaudepublica.models.enuns.TipoOcorrenciaFornecedor;
import com.edufelizardo.maissaudepublica.repositories.InstrumentoContratualRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemInstrumentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemOrdemFornecimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemRecebimentoCompraRepository;
import com.edufelizardo.maissaudepublica.repositories.OcorrenciaFornecedorRepository;
import com.edufelizardo.maissaudepublica.repositories.OrdemFornecimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.RecebimentoCompraRepository;
import com.edufelizardo.maissaudepublica.repositories.SancaoFornecedorRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoCompraRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Compras, fatia CP3 (ADR-0214): a ordem de fornecimento reserva o saldo da ata ou do contrato; o fiscal (ou quem compra) recebe a entrega
 * em duas fases (art. 140) e o aceito entra no Estoque ou na Farmácia da unidade; as ocorrências com o fornecedor encerram com a resposta
 * ou viram sanção; o painel mostra o andamento e o desempenho dos fornecedores.
 */
@Service
public class EntregasService {

    static final int PRAZO_PADRAO = 30;
    static final int DIAS_ALERTA_SALA = 10;
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final Set<SituacaoOrdemFornecimento> ABERTAS = EnumSet.of(SituacaoOrdemFornecimento.EMITIDA, SituacaoOrdemFornecimento.PARCIAL);
    private static final Set<SituacaoSolicitacaoCompra> EM_ANDAMENTO = EnumSet.of(SituacaoSolicitacaoCompra.RASCUNHO, SituacaoSolicitacaoCompra.ENVIADA,
            SituacaoSolicitacaoCompra.DEVOLVIDA, SituacaoSolicitacaoCompra.APROVADA, SituacaoSolicitacaoCompra.EM_COTACAO, SituacaoSolicitacaoCompra.COTADA);

    @Autowired private OrdemFornecimentoRepository ordemRepository;
    @Autowired private ItemOrdemFornecimentoRepository itemOrdemRepository;
    @Autowired private RecebimentoCompraRepository recebimentoRepository;
    @Autowired private ItemRecebimentoCompraRepository itemRecebimentoRepository;
    @Autowired private OcorrenciaFornecedorRepository ocorrenciaRepository;
    @Autowired private InstrumentoContratualRepository instrumentoRepository;
    @Autowired private ItemInstrumentoRepository itemInstrumentoRepository;
    @Autowired private SancaoFornecedorRepository sancaoRepository;
    @Autowired private SolicitacaoCompraRepository solicitacaoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private EstoqueService estoqueService;
    @Autowired private LoteService loteService;
    @Autowired private ComprasService comprasService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Ordem de fornecimento ──────────────────────────────────────────────────────────────────────

    /**
     * Emite a ordem contra a ata ou o contrato vigente (422): itens do instrumento (422), sem repetir (400), dentro do saldo (422), que fica
     * reservado. Prazo de 1 a 365 dias, 30 sem informar.
     */
    @Transactional
    public EntregasDtos.Ordem emitir(UUID instrumentoId, OrdemFornecimentoRequestDto dto) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        InstrumentoContratual ins = instrumentoOu404(instrumentoId);
        ContextoAuditoria.registro(ins.getUuid());
        LocalDate hoje = hoje();
        if (ins.getInicio().isAfter(hoje) || ins.getFim().isBefore(hoje)) {
            throw new ResourceUnprocessableEntityException("A ata ou o contrato está fora da vigência.");
        }
        UnidadeDeSaude unidade = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        Map<UUID, ItemInstrumento> doInstrumento = itemInstrumentoRepository.findDosInstrumentos(List.of(ins.getUuid())).stream()
                .collect(Collectors.toMap(ItemInstrumento::getUuid, i -> i));
        Set<UUID> vistos = new HashSet<>();
        for (OrdemFornecimentoRequestDto.Item i : dto.getItens()) {
            if (!vistos.add(i.getItemInstrumentoId())) {
                throw new ResourceBadRequestException("O mesmo item está duas vezes na ordem.");
            }
            ItemInstrumento item = doInstrumento.get(i.getItemInstrumentoId());
            if (item == null) {
                throw new ResourceUnprocessableEntityException("O item " + i.getItemInstrumentoId() + " não é desta ata ou contrato.");
            }
            if (i.getQuantidade() > item.getSaldo()) {
                throw new ResourceUnprocessableEntityException("O item " + item.getItemContratacao().getDescricao() + " tem saldo de " + item.getSaldo() + ".");
            }
        }
        int prazo = dto.getPrazoDias() != null ? dto.getPrazoDias() : PRAZO_PADRAO;
        OrdemFornecimento o = new OrdemFornecimento();
        o.setNumero(numero(hoje));
        o.setInstrumento(ins);
        o.setUnidade(unidade);
        o.setEmpenho(limpo(dto.getEmpenho()));
        o.setEmitidaEm(hoje);
        o.setPrazoDias(prazo);
        o.setPrazoEm(hoje.plusDays(prazo));
        o.setSituacao(SituacaoOrdemFornecimento.EMITIDA);
        o.setRegistradoEm(Instant.now());
        o.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        ordemRepository.save(o);
        for (OrdemFornecimentoRequestDto.Item i : dto.getItens()) {
            ItemInstrumento item = doInstrumento.get(i.getItemInstrumentoId());
            item.setSaldo(item.getSaldo() - i.getQuantidade());
            itemInstrumentoRepository.save(item);
            itemOrdemRepository.save(new ItemOrdemFornecimento(null, o, item, i.getQuantidade(), 0));
        }
        ContextoAuditoria.detalhe("Ordem " + o.getNumero());
        return dto(o);
    }

    /** As ordens da ata ou do contrato, para quem compra e para o gestor e o fiscal (403 aos outros). */
    @Transactional(readOnly = true)
    public List<EntregasDtos.Ordem> ordensDo(UUID instrumentoId) {
        InstrumentoContratual ins = instrumentoOu404(instrumentoId);
        exigirAcompanhar(ins);
        return dtos(ordemRepository.findTodas().stream().filter(o -> o.getInstrumento().getUuid().equals(ins.getUuid())).toList());
    }

    @Transactional(readOnly = true)
    public EntregasDtos.Ordem ordem(UUID id) {
        OrdemFornecimento o = ordemOu404(id);
        exigirAcompanhar(o.getInstrumento());
        return dto(o);
    }

    /** Cancela a ordem aberta (422), sem recebimento provisório pendente (422); o não aceito volta ao saldo. */
    @Transactional
    public EntregasDtos.Ordem cancelar(UUID id, String motivo) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        OrdemFornecimento o = ordemOu404(id);
        ContextoAuditoria.registro(o.getUuid());
        aberta(o);
        if (recebimentoRepository.findDasOrdens(List.of(o.getUuid())).stream().anyMatch(r -> r.getSituacao() == SituacaoRecebimentoCompra.PROVISORIO)) {
            throw new ResourceUnprocessableEntityException("Há recebimento provisório pendente; conclua o definitivo antes.");
        }
        for (ItemOrdemFornecimento i : itemOrdemRepository.findDasOrdens(List.of(o.getUuid()))) {
            ItemInstrumento item = i.getItemInstrumento();
            item.setSaldo(item.getSaldo() + i.getQuantidade() - i.getQuantidadeAceita());
            itemInstrumentoRepository.save(item);
        }
        o.setSituacao(SituacaoOrdemFornecimento.CANCELADA);
        o.setMotivoCancelamento(motivo.trim());
        ordemRepository.save(o);
        return dto(o);
    }

    // ── Recebimento ────────────────────────────────────────────────────────────────────────────────

    /**
     * O recebimento provisório (art. 140, I, a), pelo fiscal ou por quem compra (403 aos outros), da ordem aberta (422): nota fiscal com a
     * data até hoje (400); itens da ordem (422), sem repetir (400), até o que falta entregar (422); medicamento e material com lote pedem o
     * lote e a validade, os outros não (400); lote vencido não entra (422).
     */
    @Transactional
    public EntregasDtos.Ordem receber(UUID ordemId, RecebimentoCompraRequestDto dto) {
        OrdemFornecimento o = ordemOu404(ordemId);
        exigirReceber(o.getInstrumento());
        ContextoAuditoria.registro(o.getUuid());
        aberta(o);
        LocalDate hoje = hoje();
        if (dto.getNotaEmitidaEm().isAfter(hoje)) {
            throw new ResourceBadRequestException("A nota fiscal não pode ser do futuro.");
        }
        Map<UUID, ItemOrdemFornecimento> itens = itemOrdemRepository.findDasOrdens(List.of(o.getUuid())).stream()
                .collect(Collectors.toMap(ItemOrdemFornecimento::getUuid, i -> i));
        Map<UUID, Integer> emRecebimento = emRecebimento(List.of(o.getUuid()));
        RecebimentoCompra r = new RecebimentoCompra(null, o, dto.getNotaFiscal().trim(), dto.getNotaEmitidaEm(), hoje, SituacaoRecebimentoCompra.PROVISORIO,
                null, null, limpo(dto.getObservacao()), Instant.now(), UsuarioAutenticado.cpf());
        List<ItemRecebimentoCompra> novos = new ArrayList<>();
        Set<UUID> vistos = new HashSet<>();
        for (RecebimentoCompraRequestDto.Item i : dto.getItens()) {
            if (!vistos.add(i.getItemOrdemId())) {
                throw new ResourceBadRequestException("O mesmo item está duas vezes no recebimento.");
            }
            ItemOrdemFornecimento item = itens.get(i.getItemOrdemId());
            if (item == null) {
                throw new ResourceUnprocessableEntityException("O item " + i.getItemOrdemId() + " não é desta ordem.");
            }
            ItemContratacao ic = item.getItemInstrumento().getItemContratacao();
            int pendente = item.getQuantidade() - item.getQuantidadeAceita() - emRecebimento.getOrDefault(item.getUuid(), 0);
            if (i.getQuantidade() > pendente) {
                throw new ResourceUnprocessableEntityException("Falta entregar " + pendente + " de " + ic.getDescricao() + ".");
            }
            String lote = limpo(i.getNumeroLote());
            boolean pedeLote = pedeLote(ic);
            if (pedeLote != (lote != null && i.getValidade() != null) || (!pedeLote && (lote != null || i.getValidade() != null))) {
                throw new ResourceBadRequestException(ic.getDescricao() + (pedeLote ? ": informe o lote e a validade." : ": não leva lote nem validade."));
            }
            if (i.getValidade() != null && i.getValidade().isBefore(hoje)) {
                throw new ResourceUnprocessableEntityException("O lote " + lote + " de " + ic.getDescricao() + " está vencido.");
            }
            novos.add(new ItemRecebimentoCompra(null, r, item, i.getQuantidade(), lote, i.getValidade(), null, null));
        }
        recebimentoRepository.save(r);
        itemRecebimentoRepository.saveAll(novos);
        ContextoAuditoria.detalhe("Recebimento provisório da nota " + r.getNotaFiscal());
        return dto(o);
    }

    /**
     * O recebimento definitivo (art. 140, I, b) do provisório (422): o aceito de cada item entregue, todos e só eles (400), até o entregue
     * (400); o recusado pede o motivo (400) e vira ocorrência de item fora da especificação. O aceito entra no Estoque (material, com o
     * fornecedor e o preço do contrato) ou na Farmácia (medicamento) da unidade da ordem; o serviço fica atestado.
     */
    @Transactional
    public EntregasDtos.Ordem definitivo(UUID recebimentoId, RecebimentoDefinitivoRequestDto dto) {
        RecebimentoCompra r = recebimentoRepository.findById(recebimentoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o recebimento " + recebimentoId + " em nossos registros."));
        OrdemFornecimento o = r.getOrdem();
        exigirReceber(o.getInstrumento());
        ContextoAuditoria.registro(o.getUuid());
        if (r.getSituacao() != SituacaoRecebimentoCompra.PROVISORIO) {
            throw new ResourceUnprocessableEntityException("O recebimento já é definitivo.");
        }
        Map<UUID, ItemRecebimentoCompra> itens = itemRecebimentoRepository.findDosRecebimentos(List.of(r.getUuid())).stream()
                .collect(Collectors.toMap(ItemRecebimentoCompra::getUuid, i -> i));
        Set<UUID> informados = dto.getItens().stream().map(RecebimentoDefinitivoRequestDto.Item::getItemRecebimentoId).collect(Collectors.toSet());
        if (informados.size() != dto.getItens().size() || !informados.equals(itens.keySet())) {
            throw new ResourceBadRequestException("Informe o aceite de cada item entregue, e só deles.");
        }
        Fornecedor fornecedor = o.getInstrumento().getFornecedor();
        List<String> recusas = new ArrayList<>();
        for (RecebimentoDefinitivoRequestDto.Item d : dto.getItens()) {
            ItemRecebimentoCompra i = itens.get(d.getItemRecebimentoId());
            ItemOrdemFornecimento item = i.getItemOrdem();
            ItemContratacao ic = item.getItemInstrumento().getItemContratacao();
            if (d.getQuantidadeAceita() > i.getQuantidadeEntregue()) {
                throw new ResourceBadRequestException(ic.getDescricao() + ": o aceito passa do entregue (" + i.getQuantidadeEntregue() + ").");
            }
            int recusada = i.getQuantidadeEntregue() - d.getQuantidadeAceita();
            String motivo = limpo(d.getMotivoRecusa());
            if (recusada > 0 && motivo == null) {
                throw new ResourceBadRequestException(ic.getDescricao() + ": informe o motivo da recusa.");
            }
            i.setQuantidadeAceita(d.getQuantidadeAceita());
            i.setMotivoRecusa(recusada > 0 ? motivo : null);
            itemRecebimentoRepository.save(i);
            item.setQuantidadeAceita(item.getQuantidadeAceita() + d.getQuantidadeAceita());
            itemOrdemRepository.save(item);
            if (recusada > 0) {
                recusas.add(recusada + " " + ic.getUnidadeMedida().toLowerCase(Locale.ROOT) + " de " + ic.getDescricao() + ": " + motivo);
            }
            if (d.getQuantidadeAceita() > 0) {
                darEntrada(o, r, i, ic, item.getItemInstrumento(), fornecedor, d.getQuantidadeAceita());
            }
        }
        r.setSituacao(SituacaoRecebimentoCompra.DEFINITIVO);
        r.setDefinitivoEm(hoje());
        r.setAtestadoPorCpf(UsuarioAutenticado.cpf());
        recebimentoRepository.save(r);
        boolean completa = itemOrdemRepository.findDasOrdens(List.of(o.getUuid())).stream().allMatch(i -> i.getQuantidadeAceita() >= i.getQuantidade());
        o.setSituacao(completa ? SituacaoOrdemFornecimento.RECEBIDA : SituacaoOrdemFornecimento.PARCIAL);
        ordemRepository.save(o);
        if (!recusas.isEmpty()) {
            ocorrenciaRepository.save(ocorrencia(fornecedor, o, TipoOcorrenciaFornecedor.FORA_ESPECIFICACAO,
                    "Recusado no recebimento da nota " + r.getNotaFiscal() + ": " + String.join("; ", recusas)));
        }
        ContextoAuditoria.detalhe("Recebimento definitivo da nota " + r.getNotaFiscal());
        return dto(o);
    }

    private void darEntrada(OrdemFornecimento o, RecebimentoCompra r, ItemRecebimentoCompra i, ItemContratacao ic, ItemInstrumento ii, Fornecedor f, int quantidade) {
        if (ic.getTipo() == TipoItemCompra.MATERIAL) {
            EntradaEstoqueRequestDto e = new EntradaEstoqueRequestDto();
            e.setUnidadeId(o.getUnidade().getUuid());
            e.setMaterialId(ic.getMaterial().getUuid());
            e.setQuantidade(quantidade);
            e.setOrigem(OrigemEntradaEstoque.NOTA_FISCAL);
            e.setNumeroLote(i.getNumeroLote());
            e.setValidade(i.getValidade());
            e.setCustoUnitario(ii.getPrecoUnitario());
            e.setDocumento(r.getNotaFiscal());
            e.setFornecedor(f.getRazaoSocial().length() > 150 ? f.getRazaoSocial().substring(0, 150) : f.getRazaoSocial());
            estoqueService.entradaConferida(e, f);
        } else if (ic.getTipo() == TipoItemCompra.MEDICAMENTO) {
            LoteRequestDto l = new LoteRequestDto();
            l.setMedicamentoId(ic.getMedicamento().getUuid());
            l.setUnidadeId(o.getUnidade().getUuid());
            l.setNumeroLote(i.getNumeroLote());
            l.setValidade(i.getValidade());
            l.setQuantidade(quantidade);
            loteService.criarPeloRecebimento(l);
        }
    }

    // ── Ocorrências ────────────────────────────────────────────────────────────────────────────────

    /** Ocorrência na ordem, pelo fiscal ou por quem compra; o atraso não se registra à mão (400), nasce sozinho. */
    @Transactional
    public EntregasDtos.Ordem registrarOcorrencia(UUID ordemId, OcorrenciaFornecedorRequestDto dto) {
        OrdemFornecimento o = ordemOu404(ordemId);
        exigirReceber(o.getInstrumento());
        ContextoAuditoria.registro(o.getUuid());
        if (dto.getTipo() == TipoOcorrenciaFornecedor.ATRASO) {
            throw new ResourceBadRequestException("O atraso é registrado sozinho quando a ordem passa do prazo.");
        }
        ocorrenciaRepository.save(ocorrencia(o.getInstrumento().getFornecedor(), o, dto.getTipo(), dto.getDescricao().trim()));
        return dto(o);
    }

    /** As ocorrências, com os atrasos de hoje já registrados; {@code situacao} filtra. */
    @Transactional
    public List<EntregasDtos.Ocorrencia> ocorrencias(SituacaoOcorrenciaFornecedor situacao) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        registrarAtrasos();
        return ocorrenciaRepository.findTodas().stream().filter(x -> situacao == null || x.getSituacao() == situacao).map(EntregasService::ocorrenciaDto).toList();
    }

    /** Encerra a ocorrência aberta (422) com a resposta do fornecedor. */
    @Transactional
    public EntregasDtos.Ocorrencia encerrar(UUID id, String resposta) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        OcorrenciaFornecedor x = ocorrenciaAberta(id);
        x.setSituacao(SituacaoOcorrenciaFornecedor.ENCERRADA);
        x.setResposta(resposta.trim());
        x.setEncerradaEm(hoje());
        return ocorrenciaDto(ocorrenciaRepository.save(x));
    }

    /** Converte a ocorrência aberta (422) em sanção ao fornecedor, com as regras do art. 156 (ADR-0212). */
    @Transactional
    public EntregasDtos.Ocorrencia sancionar(UUID id, SancaoFornecedorRequestDto dto) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        OcorrenciaFornecedor x = ocorrenciaAberta(id);
        UUID fornecedorId = x.getFornecedor().getUuid();
        comprasService.registrarSancao(fornecedorId, dto);
        SancaoFornecedor s = sancaoRepository.findByFornecedor_UuidOrderByInicioDesc(fornecedorId).stream()
                .max(Comparator.comparing(SancaoFornecedor::getRegistradoEm)).orElseThrow();
        x.setSituacao(SituacaoOcorrenciaFornecedor.SANCIONADA);
        x.setSancao(s);
        x.setEncerradaEm(hoje());
        ContextoAuditoria.registro(x.getUuid());
        return ocorrenciaDto(ocorrenciaRepository.save(x));
    }

    // ── Painel ─────────────────────────────────────────────────────────────────────────────────────

    /**
     * O painel de Compras: as solicitações em andamento, o valor contratado no período (pela homologação), as ordens abertas e atrasadas,
     * os contratos vencendo em 90 dias, os itens com saldo baixo, as ocorrências abertas e o desempenho dos fornecedores nas ordens do
     * período. Sem datas, os últimos 12 meses; início depois do fim ou mais de 366 dias, 400.
     */
    @Transactional
    public EntregasDtos.Painel painel(LocalDate inicio, LocalDate fim) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        LocalDate hoje = hoje();
        LocalDate ate = fim != null ? fim : hoje;
        LocalDate de = inicio != null ? inicio : ate.minusMonths(12).plusDays(1);
        if (de.isAfter(ate)) {
            throw new ResourceBadRequestException("O início é antes do fim.");
        }
        if (ChronoUnit.DAYS.between(de, ate) > 366) {
            throw new ResourceBadRequestException("O período vai até um ano.");
        }
        registrarAtrasos();
        Map<SituacaoSolicitacaoCompra, Long> porSituacao = new EnumMap<>(SituacaoSolicitacaoCompra.class);
        EM_ANDAMENTO.forEach(s -> porSituacao.put(s, 0L));
        solicitacaoRepository.findTodas().stream().filter(s -> EM_ANDAMENTO.contains(s.getSituacao())).forEach(s -> porSituacao.merge(s.getSituacao(), 1L, Long::sum));

        List<InstrumentoContratual> instrumentos = instrumentoRepository.findTodos();
        BigDecimal contratado = instrumentos.stream().filter(i -> dentro(i.getContratacao().getHomologadaEm(), de, ate)).map(InstrumentoContratual::getValorInicial)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<InstrumentoContratual> vigentes = instrumentos.stream().filter(i -> !i.getInicio().isAfter(hoje) && !i.getFim().isBefore(hoje)).toList();
        long vencendo = vigentes.stream().filter(i -> ChronoUnit.DAYS.between(hoje, i.getFim()) <= ContratosService.DIAS_ALERTA_VIGENCIA).count();
        long saldoBaixo = vigentes.isEmpty() ? 0 : itemInstrumentoRepository.findDosInstrumentos(vigentes.stream().map(InstrumentoContratual::getUuid).toList())
                .stream().filter(i -> i.getQuantidade() > 0 && i.getSaldo() * 5 < i.getQuantidade()).count();

        List<OrdemFornecimento> ordens = ordemRepository.findTodas();
        List<OrdemFornecimento> abertas = ordens.stream().filter(o -> ABERTAS.contains(o.getSituacao())).toList();
        List<EntregasDtos.OrdemAtrasada> atrasadas = abertas.stream().filter(o -> o.getPrazoEm().isBefore(hoje))
                .map(o -> new EntregasDtos.OrdemAtrasada(o.getUuid(), o.getNumero(), o.getInstrumento().getFornecedor().getRazaoSocial(), o.getUnidade().getNome(),
                        o.getPrazoEm(), ChronoUnit.DAYS.between(o.getPrazoEm(), hoje)))
                .sorted(Comparator.comparingLong(EntregasDtos.OrdemAtrasada::diasDeAtraso).reversed()).toList();
        List<OcorrenciaFornecedor> ocorrencias = ocorrenciaRepository.findTodas();
        long ocorrenciasAbertas = ocorrencias.stream().filter(x -> x.getSituacao() == SituacaoOcorrenciaFornecedor.ABERTA).count();

        // Desempenho: as ordens emitidas no período e os recebimentos definitivos delas.
        List<OrdemFornecimento> doPeriodo = ordens.stream().filter(o -> dentro(o.getEmitidaEm(), de, ate)).toList();
        Map<UUID, List<RecebimentoCompra>> definitivos = doPeriodo.isEmpty() ? Map.of() : recebimentoRepository.findDasOrdens(doPeriodo.stream()
                .map(OrdemFornecimento::getUuid).toList()).stream().filter(r -> r.getSituacao() == SituacaoRecebimentoCompra.DEFINITIVO)
                .collect(Collectors.groupingBy(r -> r.getOrdem().getUuid()));
        Map<UUID, Long> recusada = new HashMap<>();
        List<UUID> idsRec = definitivos.values().stream().flatMap(List::stream).map(RecebimentoCompra::getUuid).toList();
        if (!idsRec.isEmpty()) {
            itemRecebimentoRepository.findDosRecebimentos(idsRec).forEach(i -> recusada.merge(i.getRecebimento().getUuid(),
                    (long) (i.getQuantidadeEntregue() - (i.getQuantidadeAceita() == null ? 0 : i.getQuantidadeAceita())), Long::sum));
        }
        Map<UUID, List<OrdemFornecimento>> porFornecedor = doPeriodo.stream().collect(Collectors.groupingBy(o -> o.getInstrumento().getFornecedor().getUuid()));
        List<EntregasDtos.DesempenhoFornecedor> fornecedores = porFornecedor.values().stream().map(lista -> {
            Fornecedor f = lista.get(0).getInstrumento().getFornecedor();
            List<RecebimentoCompra> recs = lista.stream().flatMap(o -> definitivos.getOrDefault(o.getUuid(), List.of()).stream()).toList();
            long noPrazo = recs.stream().filter(r -> !r.getRecebidoEm().isAfter(r.getOrdem().getPrazoEm())).count();
            Double prazoMedio = recs.isEmpty() ? null
                    : Math.round(recs.stream().mapToLong(r -> ChronoUnit.DAYS.between(r.getOrdem().getEmitidaEm(), r.getRecebidoEm())).average().orElse(0) * 10) / 10.0;
            long ocorr = ocorrencias.stream().filter(x -> x.getFornecedor().getUuid().equals(f.getUuid()) && dentro(x.getRegistradaEm(), de, ate)).count();
            return new EntregasDtos.DesempenhoFornecedor(f.getUuid(), f.getRazaoSocial(), lista.size(), recs.size(),
                    recs.isEmpty() ? null : Math.round(noPrazo * 1000.0 / recs.size()) / 10.0, prazoMedio,
                    recs.stream().mapToLong(r -> recusada.getOrDefault(r.getUuid(), 0L)).sum(), ocorr);
        }).sorted(Comparator.comparingLong(EntregasDtos.DesempenhoFornecedor::ocorrencias).reversed()
                .thenComparing(EntregasDtos.DesempenhoFornecedor::fornecedorNome)).toList();
        return new EntregasDtos.Painel(de, ate, porSituacao, contratado.setScale(2, RoundingMode.HALF_UP), abertas.size(), atrasadas.size(), vencendo, saldoBaixo,
                ocorrenciasAbertas, atrasadas, fornecedores);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** A ordem aberta e vencida ganha a ocorrência de atraso, uma vez só. */
    private void registrarAtrasos() {
        LocalDate hoje = hoje();
        Set<UUID> comAtraso = ocorrenciaRepository.findTodas().stream().filter(x -> x.getTipo() == TipoOcorrenciaFornecedor.ATRASO && x.getOrdem() != null)
                .map(x -> x.getOrdem().getUuid()).collect(Collectors.toSet());
        for (OrdemFornecimento o : ordemRepository.findTodas()) {
            if (ABERTAS.contains(o.getSituacao()) && o.getPrazoEm().isBefore(hoje) && !comAtraso.contains(o.getUuid())) {
                OcorrenciaFornecedor x = ocorrencia(o.getInstrumento().getFornecedor(), o, TipoOcorrenciaFornecedor.ATRASO,
                        "A ordem " + o.getNumero() + " passou do prazo de entrega (" + o.getPrazoEm() + ").");
                x.setRegistradoPorCpf(null);
                ocorrenciaRepository.save(x);
            }
        }
    }

    private OcorrenciaFornecedor ocorrencia(Fornecedor f, OrdemFornecimento o, TipoOcorrenciaFornecedor tipo, String descricao) {
        OcorrenciaFornecedor x = new OcorrenciaFornecedor();
        x.setFornecedor(f);
        x.setOrdem(o);
        x.setTipo(tipo);
        x.setDescricao(descricao.length() > 1000 ? descricao.substring(0, 1000) : descricao);
        x.setRegistradaEm(hoje());
        x.setSituacao(SituacaoOcorrenciaFornecedor.ABERTA);
        x.setRegistradoEm(Instant.now());
        x.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        return x;
    }

    private OcorrenciaFornecedor ocorrenciaAberta(UUID id) {
        OcorrenciaFornecedor x = ocorrenciaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a ocorrência " + id + " em nossos registros."));
        if (x.getSituacao() != SituacaoOcorrenciaFornecedor.ABERTA) {
            throw new ResourceUnprocessableEntityException("A ocorrência já foi " + (x.getSituacao() == SituacaoOcorrenciaFornecedor.ENCERRADA ? "encerrada." : "sancionada."));
        }
        return x;
    }

    private static boolean pedeLote(ItemContratacao ic) {
        return ic.getTipo() == TipoItemCompra.MEDICAMENTO || (ic.getTipo() == TipoItemCompra.MATERIAL && ic.getMaterial().isControlaLote());
    }

    /** O entregue em recebimentos ainda provisórios, por item da ordem. */
    private Map<UUID, Integer> emRecebimento(List<UUID> ordens) {
        List<UUID> provisorios = recebimentoRepository.findDasOrdens(ordens).stream().filter(r -> r.getSituacao() == SituacaoRecebimentoCompra.PROVISORIO)
                .map(RecebimentoCompra::getUuid).toList();
        return provisorios.isEmpty() ? Map.of() : itemRecebimentoRepository.findDosRecebimentos(provisorios).stream()
                .collect(Collectors.groupingBy(i -> i.getItemOrdem().getUuid(), Collectors.summingInt(ItemRecebimentoCompra::getQuantidadeEntregue)));
    }

    private void exigirAcompanhar(InstrumentoContratual ins) {
        String cpf = UsuarioAutenticado.cpf();
        if (!controleDeAcesso.temAlguma(ComprasService.GERENCIAR) && (cpf == null || !(cpf.equals(ins.getFiscal().getCpf()) || cpf.equals(ins.getGestor().getCpf())))) {
            throw new ResourceForbiddenException("Sem acesso a esta ata ou contrato.");
        }
    }

    /** Recebe e registra ocorrência quem compra ou o fiscal designado. */
    private void exigirReceber(InstrumentoContratual ins) {
        String cpf = UsuarioAutenticado.cpf();
        if (!controleDeAcesso.temAlguma(ComprasService.GERENCIAR) && (cpf == null || !cpf.equals(ins.getFiscal().getCpf()))) {
            throw new ResourceForbiddenException("Recebe quem compra ou o fiscal do contrato.");
        }
    }

    private static void aberta(OrdemFornecimento o) {
        if (!ABERTAS.contains(o.getSituacao())) {
            throw new ResourceUnprocessableEntityException("A ordem já está " + (o.getSituacao() == SituacaoOrdemFornecimento.RECEBIDA ? "recebida." : "cancelada."));
        }
    }

    private static boolean dentro(LocalDate d, LocalDate de, LocalDate ate) {
        return d != null && !d.isBefore(de) && !d.isAfter(ate);
    }

    private InstrumentoContratual instrumentoOu404(UUID id) {
        return instrumentoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a ata ou o contrato " + id + " em nossos registros."));
    }

    private OrdemFornecimento ordemOu404(UUID id) {
        return ordemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a ordem de fornecimento " + id + " em nossos registros."));
    }

    private String numero(LocalDate hoje) {
        String prefixo = hoje.getYear() + "/";
        return prefixo + String.format(Locale.ROOT, "%06d", ordemRepository.countByNumeroStartingWith(prefixo) + 1);
    }

    private EntregasDtos.Ordem dto(OrdemFornecimento o) {
        return dtos(List.of(o)).get(0);
    }

    private List<EntregasDtos.Ordem> dtos(List<OrdemFornecimento> ordens) {
        if (ordens.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = ordens.stream().map(OrdemFornecimento::getUuid).toList();
        Map<UUID, List<ItemOrdemFornecimento>> itens = itemOrdemRepository.findDasOrdens(ids).stream().collect(Collectors.groupingBy(i -> i.getOrdem().getUuid()));
        List<RecebimentoCompra> recebimentos = recebimentoRepository.findDasOrdens(ids);
        Map<UUID, List<ItemRecebimentoCompra>> itensRec = recebimentos.isEmpty() ? Map.of() : itemRecebimentoRepository.findDosRecebimentos(recebimentos.stream()
                .map(RecebimentoCompra::getUuid).toList()).stream().collect(Collectors.groupingBy(i -> i.getRecebimento().getUuid()));
        Map<UUID, Integer> emRecebimento = new HashMap<>();
        recebimentos.stream().filter(r -> r.getSituacao() == SituacaoRecebimentoCompra.PROVISORIO)
                .flatMap(r -> itensRec.getOrDefault(r.getUuid(), List.of()).stream())
                .forEach(i -> emRecebimento.merge(i.getItemOrdem().getUuid(), i.getQuantidadeEntregue(), Integer::sum));
        Map<UUID, List<RecebimentoCompra>> recPorOrdem = recebimentos.stream().collect(Collectors.groupingBy(r -> r.getOrdem().getUuid()));
        Map<UUID, List<OcorrenciaFornecedor>> ocorrencias = ocorrenciaRepository.findTodas().stream().filter(x -> x.getOrdem() != null)
                .collect(Collectors.groupingBy(x -> x.getOrdem().getUuid()));
        LocalDate hoje = hoje();
        return ordens.stream().map(o -> {
            List<ItemOrdemFornecimento> doPedido = itens.getOrDefault(o.getUuid(), List.of()).stream()
                    .sorted(Comparator.comparingInt(i -> i.getItemInstrumento().getItemContratacao().getOrdem())).toList();
            Map<UUID, String> descricoes = new HashMap<>();
            List<EntregasDtos.ItemOrdem> itensDto = doPedido.stream().map(i -> {
                ItemContratacao ic = i.getItemInstrumento().getItemContratacao();
                descricoes.put(i.getUuid(), ic.getDescricao());
                int em = emRecebimento.getOrDefault(i.getUuid(), 0);
                return new EntregasDtos.ItemOrdem(i.getUuid(), i.getItemInstrumento().getUuid(), ic.getTipo(), ic.getDescricao(), ic.getUnidadeMedida(),
                        i.getQuantidade(), i.getQuantidadeAceita(), em, Math.max(0, i.getQuantidade() - i.getQuantidadeAceita() - em),
                        i.getItemInstrumento().getPrecoUnitario(), pedeLote(ic));
            }).toList();
            BigDecimal valor = doPedido.stream().map(i -> i.getItemInstrumento().getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            List<EntregasDtos.Recebimento> recs = recPorOrdem.getOrDefault(o.getUuid(), List.of()).stream().map(r -> new EntregasDtos.Recebimento(r.getUuid(),
                    r.getNotaFiscal(), r.getNotaEmitidaEm(), r.getRecebidoEm(), r.getSituacao(), r.getDefinitivoEm(), r.getObservacao(),
                    itensRec.getOrDefault(r.getUuid(), List.of()).stream().map(i -> new EntregasDtos.ItemRecebimento(i.getUuid(), i.getItemOrdem().getUuid(),
                            descricoes.get(i.getItemOrdem().getUuid()), i.getQuantidadeEntregue(), i.getQuantidadeAceita(),
                            i.getQuantidadeAceita() == null ? null : i.getQuantidadeEntregue() - i.getQuantidadeAceita(), i.getMotivoRecusa(), i.getNumeroLote(),
                            i.getValidade())).toList())).toList();
            boolean atrasada = ABERTAS.contains(o.getSituacao()) && o.getPrazoEm().isBefore(hoje);
            Fornecedor f = o.getInstrumento().getFornecedor();
            return new EntregasDtos.Ordem(o.getUuid(), o.getNumero(), o.getInstrumento().getUuid(), o.getInstrumento().getNumero(), f.getUuid(), f.getRazaoSocial(),
                    o.getUnidade().getUuid(), o.getUnidade().getNome(), o.getEmpenho(), o.getEmitidaEm(), o.getPrazoEm(), atrasada,
                    atrasada ? ChronoUnit.DAYS.between(o.getPrazoEm(), hoje) : 0, o.getSituacao(), o.getMotivoCancelamento(), valor, itensDto, recs,
                    ocorrencias.getOrDefault(o.getUuid(), List.of()).stream().map(EntregasService::ocorrenciaDto).toList());
        }).toList();
    }

    private static EntregasDtos.Ocorrencia ocorrenciaDto(OcorrenciaFornecedor x) {
        return new EntregasDtos.Ocorrencia(x.getUuid(), x.getFornecedor().getUuid(), x.getFornecedor().getRazaoSocial(), x.getOrdem() != null ? x.getOrdem().getUuid() : null,
                x.getOrdem() != null ? x.getOrdem().getNumero() : null, x.getTipo(), x.getDescricao(), x.getRegistradaEm(), x.getSituacao(), x.getResposta(),
                x.getEncerradaEm(), x.getSancao() != null ? x.getSancao().getUuid() : null);
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }
}
