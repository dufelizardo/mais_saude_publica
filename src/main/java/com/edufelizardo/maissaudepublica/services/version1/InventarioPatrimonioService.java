package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.InventarioDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ManutencaoDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PatrimonioDtos;
import com.edufelizardo.maissaudepublica.models.enuns.*;
import com.edufelizardo.maissaudepublica.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Patrimônio, fatia PM3 (ADR-0217): o inventário anual da unidade (comissão de três, conferência item a item, achados a mais e o
 * fechamento que trata as divergências), a baixa do bem com a confirmação da gestão acima do valor de alçada, e o painel com o valor
 * contábil estimado, os inventários, as baixas e a manutenção.
 */
@Service
public class InventarioPatrimonioService {

    public static final String CONFIRMAR_BAIXA = "PATRIMONIO.CONFIRMAR_BAIXA";
    /** Acima deste valor de aquisição, a baixa espera a confirmação da gestão. */
    static final BigDecimal ALCADA = new BigDecimal("10000");
    static final int MEMBROS = 3;
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final Set<SituacaoBem> TOMBADOS = EnumSet.of(SituacaoBem.EM_USO, SituacaoBem.OCIOSO, SituacaoBem.EM_MANUTENCAO);
    private static final String CONSULTAR = PatrimonioService.CONSULTAR;
    private static final String GERENCIAR = PatrimonioService.GERENCIAR;

    @Autowired private InventarioPatrimonioRepository inventarioRepository;
    @Autowired private MembroInventarioRepository membroRepository;
    @Autowired private ItemInventarioRepository itemRepository;
    @Autowired private AchadoInventarioRepository achadoRepository;
    @Autowired private BaixaBemRepository baixaRepository;
    @Autowired private BemPatrimonialRepository bemRepository;
    @Autowired private ResponsavelBemRepository responsavelRepository;
    @Autowired private TransferenciaBemRepository transferenciaRepository;
    @Autowired private PlanoManutencaoRepository planoRepository;
    @Autowired private OrdemServicoManutencaoRepository ordemServicoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private SetorRepository setorRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ManutencaoService manutencaoService;
    @Autowired private TransporteService transporteService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Inventário ─────────────────────────────────────────────────────────────────────────────────

    /**
     * Abre o inventário da unidade com três profissionais diferentes (400), ativos (404/422); um aberto por unidade (409). Os bens tombados
     * e não baixados da unidade ficam na lista, com o setor de agora.
     */
    @Transactional
    public InventarioDtos.Inventario abrir(InventarioPatrimonioRequestDto dto) {
        UnidadeDeSaude u = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, u);
        Set<String> matriculas = dto.getMembrosMatriculas().stream().filter(Objects::nonNull).map(String::trim).filter(m -> !m.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (matriculas.size() != MEMBROS) {
            throw new ResourceBadRequestException("A comissão tem três profissionais diferentes.");
        }
        List<Profissional> membros = new ArrayList<>();
        for (String m : matriculas) {
            Profissional p = profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o profissional de matrícula " + m + " em nossos registros."));
            if (!p.isAtivo()) {
                throw new ResourceUnprocessableEntityException("O profissional " + p.getNome() + " está inativo.");
            }
            membros.add(p);
        }
        if (inventarioRepository.existsByUnidade_UuidAndSituacao(u.getUuid(), SituacaoInventarioPatrimonio.ABERTO)) {
            throw new ResourceConflictException("A unidade " + u.getNome() + " já tem um inventário aberto.");
        }
        LocalDate hoje = hoje();
        InventarioPatrimonio inv = new InventarioPatrimonio();
        String prefixo = hoje.getYear() + "/";
        String maior = inventarioRepository.findMaiorNumero(prefixo);
        inv.setNumero(prefixo + String.format("%06d", maior == null ? 1 : Long.parseLong(maior.substring(prefixo.length())) + 1));
        inv.setUnidade(u);
        inv.setSituacao(SituacaoInventarioPatrimonio.ABERTO);
        inv.setAbertoEm(hoje);
        inv.setAbertoPorCpf(UsuarioAutenticado.cpf());
        inv.setObservacao(limpo(dto.getObservacao()));
        inv.setRegistradoEm(Instant.now());
        inventarioRepository.save(inv);
        ContextoAuditoria.registro(inv.getUuid());
        for (Profissional p : membros) {
            MembroInventario m = new MembroInventario();
            m.setInventario(inv);
            m.setProfissional(p);
            membroRepository.save(m);
        }
        List<ItemInventario> itens = bemRepository.findTodos().stream()
                .filter(b -> b.getUnidade().getUuid().equals(u.getUuid()) && TOMBADOS.contains(b.getSituacao()))
                .map(b -> {
                    ItemInventario i = new ItemInventario();
                    i.setInventario(inv);
                    i.setBem(b);
                    i.setSetorEsperado(b.getSetor());
                    return i;
                }).toList();
        itemRepository.saveAll(itens);
        ContextoAuditoria.detalhe("Inventário " + inv.getNumero() + " aberto com " + itens.size() + " bem(ns)");
        return dto(inv);
    }

    /** Os inventários das unidades visíveis; {@code unidadeId} filtra. */
    @Transactional(readOnly = true)
    public List<InventarioDtos.Inventario> inventarios(UUID unidadeId) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        return controleDeAcesso.filtrar(inventarioRepository.findTodos().stream(), i -> Stream.of(i.getUnidade()), CONSULTAR, GERENCIAR)
                .filter(i -> unidadeId == null || i.getUnidade().getUuid().equals(unidadeId))
                .map(this::dto).toList();
    }

    /** Os inventários em que quem está logado é da comissão ("Meus inventários"). */
    @Transactional(readOnly = true)
    public List<InventarioDtos.Inventario> meus() {
        String cpf = UsuarioAutenticado.cpf();
        if (cpf == null) {
            return List.of();
        }
        Set<UUID> ids = new HashSet<>(membroRepository.findInventariosDoCpf(cpf));
        return inventarioRepository.findTodos().stream().filter(i -> ids.contains(i.getUuid())).map(this::dto).toList();
    }

    /** O inventário com os itens e os achados: para a comissão ou para quem vê a unidade (403 aos outros). */
    @Transactional(readOnly = true)
    public InventarioDtos.Inventario inventario(UUID id) {
        InventarioPatrimonio inv = inventarioOu404(id);
        if (!membro(inv)) {
            controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
            controleDeAcesso.exigirVisivel(inv.getUnidade(), CONSULTAR, GERENCIAR);
        }
        return dto(inv);
    }

    /**
     * A conferência do bem, pela comissão ou por quem gerencia a unidade, no inventário aberto (422). O bem visto pede a conservação
     * (400); o outro local pede o setor ou a unidade (400), e o setor tem de ser da unidade informada (422). Pode ser refeita até o fechamento.
     */
    @Transactional
    public InventarioDtos.Inventario conferir(UUID itemId, ConferenciaInventarioRequestDto dto) {
        ItemInventario item = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o item " + itemId + " em nossos registros."));
        InventarioPatrimonio inv = item.getInventario();
        exigirComissaoOuGestao(inv);
        exigirAberto(inv);
        ContextoAuditoria.registro(inv.getUuid());
        boolean visto = dto.getResultado() != ResultadoConferencia.NAO_ENCONTRADO;
        if (visto && dto.getConservacao() == null) {
            throw new ResourceBadRequestException("Informe o estado de conservação do bem.");
        }
        Setor setor = null;
        UnidadeDeSaude outra = null;
        if (dto.getResultado() == ResultadoConferencia.OUTRO_LOCAL) {
            if (dto.getSetorId() == null && dto.getUnidadeId() == null) {
                throw new ResourceBadRequestException("Informe o setor ou a unidade onde o bem estava.");
            }
            UnidadeDeSaude onde = inv.getUnidade();
            if (dto.getUnidadeId() != null && !dto.getUnidadeId().equals(inv.getUnidade().getUuid())) {
                outra = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
                onde = outra;
            } else if (dto.getSetorId() == null) {
                throw new ResourceBadRequestException("Na própria unidade, informe o setor onde o bem estava.");
            }
            if (dto.getSetorId() != null) {
                setor = setorRepository.findById(dto.getSetorId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar o setor " + dto.getSetorId() + " em nossos registros."));
                if (setor.getUnidade() == null || !setor.getUnidade().getUuid().equals(onde.getUuid())) {
                    throw new ResourceUnprocessableEntityException("O setor " + setor.getNome() + " não é da unidade " + onde.getNome() + ".");
                }
            }
        }
        item.setResultado(dto.getResultado());
        item.setConservacao(visto ? dto.getConservacao() : null);
        item.setSetorEncontrado(setor);
        item.setUnidadeEncontrada(outra);
        item.setObservacao(limpo(dto.getObservacao()));
        item.setConferidoEm(hoje());
        item.setConferidoPorCpf(UsuarioAutenticado.cpf());
        itemRepository.save(item);
        return dto(inv);
    }

    /** O bem achado na unidade e fora da lista, pela comissão ou pela gestão, no inventário aberto (422). */
    @Transactional
    public InventarioDtos.Inventario registrarAchado(UUID inventarioId, AchadoInventarioRequestDto dto) {
        InventarioPatrimonio inv = inventarioOu404(inventarioId);
        exigirComissaoOuGestao(inv);
        exigirAberto(inv);
        ContextoAuditoria.registro(inv.getUuid());
        AchadoInventario a = new AchadoInventario();
        a.setInventario(inv);
        a.setDescricao(dto.getDescricao().trim());
        a.setLocal(limpo(dto.getLocal()));
        a.setRegistradoEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        achadoRepository.save(a);
        return dto(inv);
    }

    /**
     * Fecha o inventário aberto (422) com tudo conferido (422), por quem gerencia a unidade. O bem em outro setor tem o setor corrigido; o
     * em outra unidade ganha a transferência pendente; o não encontrado e o inservível ficam com a baixa sugerida.
     */
    @Transactional
    public InventarioDtos.Inventario fechar(UUID inventarioId) {
        InventarioPatrimonio inv = inventarioOu404(inventarioId);
        controleDeAcesso.exigir(GERENCIAR, inv.getUnidade());
        ContextoAuditoria.registro(inv.getUuid());
        exigirAberto(inv);
        List<ItemInventario> itens = itemRepository.findDosInventarios(List.of(inv.getUuid()));
        long faltam = itens.stream().filter(i -> i.getResultado() == null).count();
        if (faltam > 0) {
            throw new ResourceUnprocessableEntityException("Faltam " + faltam + " bem(ns) a conferir.");
        }
        LocalDate hoje = hoje();
        inv.setSituacao(SituacaoInventarioPatrimonio.FECHADO);
        inv.setFechadoEm(hoje);
        inv.setFechadoPorCpf(UsuarioAutenticado.cpf());
        inventarioRepository.save(inv);
        Set<UUID> comPendente = transferenciaRepository.findDosBens(itens.stream().map(i -> i.getBem().getUuid()).toList()).stream()
                .filter(t -> t.getSituacao() == SituacaoTransferenciaBem.PENDENTE).map(t -> t.getBem().getUuid()).collect(Collectors.toSet());
        for (ItemInventario i : itens) {
            BemPatrimonial b = i.getBem();
            AcaoInventario acao = null;
            if (i.getResultado() == ResultadoConferencia.OUTRO_LOCAL) {
                if (i.getUnidadeEncontrada() == null) {
                    b.setSetor(i.getSetorEncontrado());
                    bemRepository.save(b);
                    acao = AcaoInventario.SETOR_CORRIGIDO;
                } else if (!comPendente.contains(b.getUuid()) && EnumSet.of(SituacaoBem.EM_USO, SituacaoBem.OCIOSO).contains(b.getSituacao())) {
                    TransferenciaBem t = new TransferenciaBem();
                    t.setBem(b);
                    t.setOrigem(inv.getUnidade());
                    t.setDestino(i.getUnidadeEncontrada());
                    t.setMotivo("Inventário " + inv.getNumero() + ": o bem estava em " + i.getUnidadeEncontrada().getNome() + ".");
                    t.setEnviadaEm(hoje);
                    t.setEnviadaPorCpf(UsuarioAutenticado.cpf());
                    t.setSituacao(SituacaoTransferenciaBem.PENDENTE);
                    t.setRegistradoEm(Instant.now());
                    transferenciaRepository.save(t);
                    acao = AcaoInventario.TRANSFERENCIA;
                }
            }
            if (i.getResultado() == ResultadoConferencia.NAO_ENCONTRADO || i.getConservacao() == ConservacaoBem.INSERVIVEL) {
                acao = AcaoInventario.BAIXA_SUGERIDA;
            }
            if (acao != null) {
                i.setAcao(acao);
                itemRepository.save(i);
            }
        }
        ContextoAuditoria.detalhe("Inventário " + inv.getNumero() + " fechado");
        return dto(inv);
    }

    // ── Baixa ──────────────────────────────────────────────────────────────────────────────────────

    /**
     * Pede a baixa do bem tombado e não baixado (422), sem ordem de serviço aberta nem transferência pendente (422), uma baixa à espera por
     * vez (409). Data não futura e o boletim no furto ou roubo (400); a devolução só do bem cedido (422). Até o valor de alçada, a baixa vale
     * na hora; acima dele, espera a confirmação da gestão.
     */
    @Transactional
    public PatrimonioDtos.Baixa pedirBaixa(UUID bemId, BaixaBemRequestDto dto) {
        BemPatrimonial b = bemOu404(bemId);
        controleDeAcesso.exigir(GERENCIAR, b.getUnidade());
        ContextoAuditoria.registro(b.getUuid());
        LocalDate hoje = hoje();
        if (dto.getDataBaixa().isAfter(hoje)) {
            throw new ResourceBadRequestException("A data da baixa não pode ser futura.");
        }
        if (dto.getMotivo() == MotivoBaixaBem.FURTO_ROUBO && limpo(dto.getBoletimOcorrencia()) == null) {
            throw new ResourceBadRequestException("No furto ou roubo, informe o número do boletim de ocorrência.");
        }
        if (dto.getMotivo() == MotivoBaixaBem.DEVOLUCAO_CESSAO && b.getOrigem() != OrigemBem.CESSAO_COMODATO) {
            throw new ResourceUnprocessableEntityException("Só o bem cedido ou em comodato é devolvido.");
        }
        if (baixaRepository.findDoBem(b.getUuid()).stream().anyMatch(x -> x.getSituacao() == SituacaoBaixaBem.PENDENTE)) {
            throw new ResourceConflictException("O bem já tem uma baixa à espera da confirmação.");
        }
        exigirBaixavel(b);
        BaixaBem x = new BaixaBem();
        x.setBem(b);
        x.setMotivo(dto.getMotivo());
        x.setProcesso(dto.getProcesso().trim());
        x.setBoletimOcorrencia(limpo(dto.getBoletimOcorrencia()));
        x.setDataBaixa(dto.getDataBaixa());
        x.setObservacao(limpo(dto.getObservacao()));
        x.setValorContabil(PatrimonioService.valorContabil(b, hoje));
        x.setRegistradoEm(Instant.now());
        x.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        boolean alcada = b.getValorAquisicao() != null && b.getValorAquisicao().compareTo(ALCADA) > 0;
        x.setSituacao(alcada ? SituacaoBaixaBem.PENDENTE : SituacaoBaixaBem.CONFIRMADA);
        if (!alcada) {
            x.setResolvidaEm(hoje);
            x.setResolvidaPorCpf(UsuarioAutenticado.cpf());
        }
        baixaRepository.save(x);
        if (!alcada) {
            efetivar(b, x);
        }
        ContextoAuditoria.detalhe("Baixa do bem " + b.getNumeroTombamento() + (alcada ? " à espera da confirmação" : ""));
        return PatrimonioService.dto(x);
    }

    /** A gestão confirma a baixa à espera (422); quem pediu não confirma (422). As condições do bem são conferidas de novo. */
    @Transactional
    public PatrimonioDtos.Baixa confirmarBaixa(UUID baixaId) {
        BaixaBem x = baixaPendenteOu422(baixaId);
        BemPatrimonial b = x.getBem();
        controleDeAcesso.exigir(CONFIRMAR_BAIXA, b.getUnidade());
        ContextoAuditoria.registro(b.getUuid());
        String cpf = UsuarioAutenticado.cpf();
        if (cpf != null && cpf.equals(x.getRegistradoPorCpf())) {
            throw new ResourceUnprocessableEntityException("Quem pediu a baixa não a confirma (segregação de funções).");
        }
        exigirBaixavel(b);
        x.setSituacao(SituacaoBaixaBem.CONFIRMADA);
        x.setResolvidaEm(hoje());
        x.setResolvidaPorCpf(cpf);
        baixaRepository.save(x);
        efetivar(b, x);
        ContextoAuditoria.detalhe("Baixa do bem " + b.getNumeroTombamento() + " confirmada");
        return PatrimonioService.dto(x);
    }

    /** A gestão recusa a baixa à espera (422), com o motivo; o bem continua. */
    @Transactional
    public PatrimonioDtos.Baixa recusarBaixa(UUID baixaId, MotivoRequestDto dto) {
        BaixaBem x = baixaPendenteOu422(baixaId);
        controleDeAcesso.exigir(CONFIRMAR_BAIXA, x.getBem().getUnidade());
        ContextoAuditoria.registro(x.getBem().getUuid());
        x.setSituacao(SituacaoBaixaBem.RECUSADA);
        x.setResolvidaEm(hoje());
        x.setResolvidaPorCpf(UsuarioAutenticado.cpf());
        x.setMotivoRecusa(dto.getMotivo().trim());
        baixaRepository.save(x);
        return PatrimonioService.dto(x);
    }

    /** As baixas das unidades visíveis; {@code pendentes} deixa só as que esperam a confirmação. */
    @Transactional(readOnly = true)
    public List<PatrimonioDtos.Baixa> baixas(Boolean pendentes) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR, CONFIRMAR_BAIXA);
        return controleDeAcesso.filtrar(baixaRepository.findTodas().stream(), x -> Stream.of(x.getBem().getUnidade()), CONSULTAR, GERENCIAR, CONFIRMAR_BAIXA)
                .filter(x -> !Boolean.TRUE.equals(pendentes) || x.getSituacao() == SituacaoBaixaBem.PENDENTE)
                .map(PatrimonioService::dto).toList();
    }

    // ── Painel ─────────────────────────────────────────────────────────────────────────────────────

    /** O painel das unidades visíveis: os bens e os valores, os inventários, as baixas e a manutenção dos 10 bens mais caros. */
    @Transactional(readOnly = true)
    public InventarioDtos.Painel painel() {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        LocalDate hoje = hoje();
        List<BemPatrimonial> todos = controleDeAcesso.filtrar(bemRepository.findTodos().stream(), b -> Stream.of(b.getUnidade()), CONSULTAR, GERENCIAR).toList();
        List<BemPatrimonial> tombados = todos.stream().filter(b -> TOMBADOS.contains(b.getSituacao())).toList();
        Map<UUID, BigDecimal> contabil = new HashMap<>();
        tombados.forEach(b -> contabil.put(b.getUuid(), Optional.ofNullable(PatrimonioService.valorContabil(b, hoje)).orElse(BigDecimal.ZERO)));
        Function<List<BemPatrimonial>, BigDecimal> aquisicao = l -> l.stream().map(b -> Optional.ofNullable(b.getValorAquisicao()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Function<List<BemPatrimonial>, BigDecimal> contabilDe = l -> l.stream().map(b -> contabil.get(b.getUuid())).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<InventarioPatrimonio> inventarios = controleDeAcesso.filtrar(inventarioRepository.findTodos().stream(), i -> Stream.of(i.getUnidade()), CONSULTAR, GERENCIAR).toList();
        Map<UUID, LocalDate> ultimo = inventarios.stream().filter(i -> i.getFechadoEm() != null)
                .collect(Collectors.toMap(i -> i.getUnidade().getUuid(), InventarioPatrimonio::getFechadoEm, (a, c) -> a.isAfter(c) ? a : c));
        Map<UUID, List<BemPatrimonial>> porUnidade = tombados.stream().collect(Collectors.groupingBy(b -> b.getUnidade().getUuid(), LinkedHashMap::new, Collectors.toList()));
        List<InventarioDtos.PorUnidade> unidades = porUnidade.values().stream().map(l -> {
            UnidadeDeSaude u = l.get(0).getUnidade();
            LocalDate ult = ultimo.get(u.getUuid());
            return new InventarioDtos.PorUnidade(u.getUuid(), u.getNome(), l.size(), aquisicao.apply(l), contabilDe.apply(l), ult,
                    ult == null || ult.isBefore(hoje.minusMonths(12)));
        }).sorted(Comparator.comparing(InventarioDtos.PorUnidade::unidadeNome)).toList();
        List<InventarioDtos.PorClasse> classes = tombados.stream().filter(b -> b.getClasse() != null)
                .collect(Collectors.groupingBy(BemPatrimonial::getClasse, () -> new EnumMap<>(ClasseBem.class), Collectors.toList()))
                .entrySet().stream().map(e -> new InventarioDtos.PorClasse(e.getKey(), e.getValue().size(), aquisicao.apply(e.getValue()), contabilDe.apply(e.getValue())))
                .toList();

        List<BaixaBem> baixas = controleDeAcesso.filtrar(baixaRepository.findTodas().stream(), x -> Stream.of(x.getBem().getUnidade()), CONSULTAR, GERENCIAR).toList();
        long baixasNoAno = baixas.stream().filter(x -> x.getSituacao() == SituacaoBaixaBem.CONFIRMADA && x.getDataBaixa().getYear() == hoje.getYear()).count();
        List<PatrimonioDtos.Baixa> pendentes = baixas.stream().filter(x -> x.getSituacao() == SituacaoBaixaBem.PENDENTE).map(PatrimonioService::dto).toList();

        Set<UUID> ids = todos.stream().map(BemPatrimonial::getUuid).collect(Collectors.toSet());
        Map<UUID, List<ManutencaoDtos.Ordem>> ordens = manutencaoService.ordensDosBens(ids).stream().collect(Collectors.groupingBy(ManutencaoDtos.Ordem::bemId));
        List<InventarioDtos.CustoBem> manutencao = ordens.values().stream().map(l -> {
            ManutencaoDtos.Ordem o = l.get(0);
            BigDecimal custo = l.stream().filter(x -> x.situacao() == SituacaoOrdemServico.CONCLUIDA).map(ManutencaoDtos.Ordem::custoTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            long dias = l.stream().filter(x -> x.situacao() != SituacaoOrdemServico.CANCELADA && x.diasParado() != null).mapToLong(ManutencaoDtos.Ordem::diasParado).sum();
            return new InventarioDtos.CustoBem(o.bemId(), o.numeroTombamento(), o.bemDescricao(), o.unidadeNome(), custo, dias);
        }).sorted(Comparator.comparing(InventarioDtos.CustoBem::custo).reversed().thenComparing(InventarioDtos.CustoBem::diasParado, Comparator.reverseOrder()))
                .limit(10).toList();

        return new InventarioDtos.Painel(tombados.size(), aquisicao.apply(tombados), contabilDe.apply(tombados),
                tombados.stream().filter(b -> b.getSituacao() == SituacaoBem.OCIOSO).count(),
                todos.stream().filter(b -> b.getSituacao() == SituacaoBem.A_TOMBAR).count(),
                inventarios.stream().filter(i -> i.getSituacao() == SituacaoInventarioPatrimonio.ABERTO).count(),
                baixasNoAno, pendentes, unidades, classes, manutencao);
    }

    // ── Auxiliares ─────────────────────────────────────────────────────────────────────────────────

    /** O bem baixado: o termo se encerra, os planos se desativam e o veículo ligado fica inativo. O histórico fica. */
    private void efetivar(BemPatrimonial b, BaixaBem x) {
        LocalDate hoje = hoje();
        if (b.getVeiculo() != null) {
            transporteService.inativarPelaBaixa(b.getVeiculo(), "Baixa do bem " + b.getNumeroTombamento() + " (processo " + x.getProcesso() + ")");
        }
        b.setSituacao(SituacaoBem.BAIXADO);
        bemRepository.save(b);
        for (ResponsavelBem r : responsavelRepository.findAtuais(List.of(b.getUuid()))) {
            r.setAte(hoje);
            responsavelRepository.save(r);
        }
        for (PlanoManutencao p : planoRepository.findDoBem(b.getUuid())) {
            if (p.isAtivo()) {
                p.setAtivo(false);
                planoRepository.save(p);
            }
        }
    }

    private void exigirBaixavel(BemPatrimonial b) {
        if (b.getSituacao() == SituacaoBem.A_TOMBAR) {
            throw new ResourceUnprocessableEntityException("O bem ainda não foi tombado.");
        }
        if (b.getSituacao() == SituacaoBem.BAIXADO) {
            throw new ResourceUnprocessableEntityException("O bem já está baixado.");
        }
        boolean ordemAberta = ordemServicoRepository.findDoBem(b.getUuid()).stream()
                .anyMatch(o -> o.getSituacao() == SituacaoOrdemServico.ABERTA || o.getSituacao() == SituacaoOrdemServico.EM_EXECUCAO);
        if (ordemAberta) {
            throw new ResourceUnprocessableEntityException("O bem tem ordem de serviço aberta; conclua ou cancele antes da baixa.");
        }
        boolean transferencia = transferenciaRepository.findDosBens(List.of(b.getUuid())).stream().anyMatch(t -> t.getSituacao() == SituacaoTransferenciaBem.PENDENTE);
        if (transferencia) {
            throw new ResourceUnprocessableEntityException("O bem tem transferência à espera do aceite.");
        }
    }

    private BaixaBem baixaPendenteOu422(UUID id) {
        BaixaBem x = baixaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a baixa " + id + " em nossos registros."));
        if (x.getSituacao() != SituacaoBaixaBem.PENDENTE) {
            throw new ResourceUnprocessableEntityException("A baixa já foi " + (x.getSituacao() == SituacaoBaixaBem.CONFIRMADA ? "confirmada." : "recusada."));
        }
        return x;
    }

    private boolean membro(InventarioPatrimonio inv) {
        String cpf = UsuarioAutenticado.cpf();
        return cpf != null && membroRepository.findDosInventarios(List.of(inv.getUuid())).stream().anyMatch(m -> cpf.equals(m.getProfissional().getCpf()));
    }

    private void exigirComissaoOuGestao(InventarioPatrimonio inv) {
        if (!membro(inv)) {
            controleDeAcesso.exigir(GERENCIAR, inv.getUnidade());
        } else {
            ContextoAuditoria.unidade(inv.getUnidade());
        }
    }

    private static void exigirAberto(InventarioPatrimonio inv) {
        if (inv.getSituacao() != SituacaoInventarioPatrimonio.ABERTO) {
            throw new ResourceUnprocessableEntityException("O inventário " + inv.getNumero() + " já foi fechado.");
        }
    }

    private InventarioDtos.Inventario dto(InventarioPatrimonio inv) {
        List<InventarioDtos.Membro> membros = membroRepository.findDosInventarios(List.of(inv.getUuid())).stream()
                .map(m -> new InventarioDtos.Membro(m.getProfissional().getMatricula(), m.getProfissional().getNome())).toList();
        List<ItemInventario> itens = itemRepository.findDosInventarios(List.of(inv.getUuid()));
        List<InventarioDtos.Item> itensDto = itens.stream().map(i -> new InventarioDtos.Item(i.getUuid(), i.getBem().getUuid(), i.getBem().getNumeroTombamento(),
                i.getBem().getDescricao(), i.getSetorEsperado() != null ? i.getSetorEsperado().getNome() : null, i.getResultado(), i.getConservacao(),
                i.getSetorEncontrado() != null ? i.getSetorEncontrado().getNome() : null,
                i.getUnidadeEncontrada() != null ? i.getUnidadeEncontrada().getNome() : null, i.getObservacao(), i.getConferidoEm(), i.getAcao())).toList();
        List<InventarioDtos.Achado> achados = achadoRepository.findDosInventarios(List.of(inv.getUuid())).stream()
                .map(a -> new InventarioDtos.Achado(a.getUuid(), a.getDescricao(), a.getLocal())).toList();
        return new InventarioDtos.Inventario(inv.getUuid(), inv.getNumero(), inv.getUnidade().getUuid(), inv.getUnidade().getNome(), inv.getSituacao(),
                inv.getAbertoEm(), inv.getFechadoEm(), inv.getObservacao(), membros, itens.size(),
                (int) itens.stream().filter(i -> i.getResultado() != null).count(),
                (int) itens.stream().filter(i -> i.getResultado() == ResultadoConferencia.NAO_ENCONTRADO).count(),
                (int) itens.stream().filter(i -> i.getResultado() == ResultadoConferencia.OUTRO_LOCAL).count(),
                (int) itens.stream().filter(i -> i.getConservacao() == ConservacaoBem.INSERVIVEL).count(), itensDto, achados);
    }

    private InventarioPatrimonio inventarioOu404(UUID id) {
        return inventarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o inventário " + id + " em nossos registros."));
    }

    private BemPatrimonial bemOu404(UUID id) {
        return bemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o bem " + id + " em nossos registros."));
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }
}
