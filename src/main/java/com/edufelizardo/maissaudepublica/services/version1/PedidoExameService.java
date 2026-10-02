package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AmostraExame;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.EventoExame;
import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.PedidoExame;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.ResultadoExame;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CancelamentoItemExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ColetaExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PedidoExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RejeicaoAmostraRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResultadoExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoResultadoExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ItemTrabalhoExameDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PedidoExameResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PedidoExameResumoDto;
import com.edufelizardo.maissaudepublica.models.enuns.InterpretacaoResultado;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoRejeicaoAmostra;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeExame;
import com.edufelizardo.maissaudepublica.models.enuns.StatusItemExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import com.edufelizardo.maissaudepublica.repositories.AmostraExameRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemPedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ResultadoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Laboratório assistencial (ADR-0093): pedido de exames, coleta em amostras (uma por material), resultado, liberação
 * e retificação. A unidade solicitante responde pelo pedido; o laboratório escolhido na coleta responde pela análise
 * e pela liberação. Resultado liberado não muda: correção é retificação com motivo. Cada passo grava um
 * {@link EventoExame}.
 */
@Service
public class PedidoExameService {

    public static final String SOLICITAR = "EXAME.SOLICITAR";
    public static final String COLETAR = "LABORATORIO.COLETAR";
    public static final String ANALISAR = "LABORATORIO.ANALISAR";
    public static final String LIBERAR = "LABORATORIO.LIBERAR";
    static final String[] VER = {SOLICITAR, COLETAR, ANALISAR, LIBERAR};

    private static final Comparator<ItemPedidoExame> ORDEM_DE_TRABALHO = Comparator
            .comparing((ItemPedidoExame i) -> i.getPedido().getPrioridade() == PrioridadeExame.URGENTE ? 0 : 1)
            .thenComparing(i -> i.getPedido().getSolicitadoEm());
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("yyMMdd", Locale.ROOT);
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom SORTEIO = new SecureRandom();

    @Autowired
    private PedidoExameRepository pedidoRepository;

    @Autowired
    private ItemPedidoExameRepository itemRepository;

    @Autowired
    private AmostraExameRepository amostraRepository;

    @Autowired
    private ResultadoExameRepository resultadoRepository;

    @Autowired
    private EventoExameRepository eventoRepository;

    @Autowired
    private ExameLaboratorialService exameService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Pedido ─────────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public PedidoExameResponseDto solicitar(PedidoExameRequestDto dto) {
        UnidadeDeSaude unidade = buscarUnidade(dto.getUnidadeSolicitanteId());
        controleDeAcesso.exigir(SOLICITAR, unidade);
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + dto.getPacienteId() + " em nossos registros."));
        ContextoAuditoria.paciente(paciente.getUuid());
        if (!paciente.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O paciente está inativo; reative o cadastro antes de pedir exames.");
        }
        Atendimento atendimento = null;
        if (dto.getAtendimentoId() != null) {
            atendimento = atendimentoRepository.findById(dto.getAtendimentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar um atendimento com o id " + dto.getAtendimentoId() + " em nossos registros."));
            if (!atendimento.getPaciente().getUuid().equals(paciente.getUuid())) {
                throw new ResourceBadRequestException("O atendimento informado é de outro paciente.");
            }
        }
        Set<UUID> distintos = new HashSet<>(dto.getExameIds());
        if (distintos.size() != dto.getExameIds().size()) {
            throw new ResourceBadRequestException("O mesmo exame aparece mais de uma vez no pedido.");
        }
        List<ExameLaboratorial> exames = dto.getExameIds().stream().map(exameService::buscar).toList();
        exames.stream().filter(e -> !e.isAtivo()).findFirst().ifPresent(e -> {
            throw new ResourceUnprocessableEntityException("O exame " + e.getNome() + " está fora de uso no catálogo.");
        });
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());

        PedidoExame pedido = new PedidoExame();
        pedido.setPaciente(paciente);
        pedido.setAtendimento(atendimento);
        pedido.setUnidadeSolicitante(unidade);
        pedido.setProfissionalSolicitante(profissional);
        pedido.setIndicacaoClinica(dto.getIndicacaoClinica().trim());
        pedido.setCid(textoOuNulo(dto.getCid()) == null ? null : dto.getCid().trim().toUpperCase(Locale.ROOT).replace(".", ""));
        pedido.setPrioridade(dto.getPrioridade());
        pedido.setSolicitadoEm(Instant.now());
        pedido.setSolicitadoPorCpf(UsuarioAutenticado.cpf());
        pedido = pedidoRepository.save(pedido);
        for (ExameLaboratorial exame : exames) {
            itemRepository.save(new ItemPedidoExame(pedido, exame));
        }
        registrar(pedido, null, TipoEventoExame.SOLICITACAO, exames.size() + " exame(s)", profissional);
        return detalhe(pedido);
    }

    /** Coleta: uma amostra por material entre os exames coletados; cada exame passa a COLETADO. */
    @Transactional
    public PedidoExameResponseDto coletar(UUID pedidoId, ColetaExameRequestDto dto) {
        PedidoExame pedido = buscarPedido(pedidoId);
        UnidadeDeSaude unidadeColeta = buscarUnidade(dto.getUnidadeColetaId());
        controleDeAcesso.exigir(COLETAR, unidadeColeta);
        UnidadeDeSaude laboratorio = dto.getLaboratorioId() != null ? buscarUnidade(dto.getLaboratorioId()) : unidadeColeta;
        Profissional coletor = buscarProfissional(dto.getProfissionalMatricula());
        List<ItemPedidoExame> aguardando = itemRepository.findByPedido_Uuid(pedidoId).stream()
                .filter(i -> i.getStatus() == StatusItemExame.SOLICITADO).toList();
        List<ItemPedidoExame> coletar = dto.getItemIds() == null || dto.getItemIds().isEmpty() ? aguardando
                : aguardando.stream().filter(i -> dto.getItemIds().contains(i.getUuid())).toList();
        if (coletar.isEmpty() || (dto.getItemIds() != null && !dto.getItemIds().isEmpty() && coletar.size() != dto.getItemIds().size())) {
            throw new ResourceUnprocessableEntityException("Não há exame aguardando coleta entre os informados.");
        }
        LocalDateTime quando = dto.getColetadaEm() != null ? dto.getColetadaEm() : LocalDateTime.now().withNano(0);
        if (quando.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new ResourceUnprocessableEntityException("A coleta não pode estar no futuro.");
        }
        Map<MaterialExame, List<ItemPedidoExame>> porMaterial = coletar.stream()
                .collect(Collectors.groupingBy(i -> i.getExame().getMaterial(), LinkedHashMap::new, Collectors.toList()));
        List<String> codigos = new ArrayList<>();
        for (Map.Entry<MaterialExame, List<ItemPedidoExame>> e : porMaterial.entrySet()) {
            AmostraExame amostra = new AmostraExame();
            amostra.setPedido(pedido);
            amostra.setCodigo(novoCodigo());
            amostra.setMaterial(e.getKey());
            amostra.setUnidadeColeta(unidadeColeta);
            amostra.setLaboratorio(laboratorio);
            amostra.setColetadaEm(quando);
            amostra.setColetadaPor(coletor);
            amostra.setRegistradoPorCpf(UsuarioAutenticado.cpf());
            amostra = amostraRepository.save(amostra);
            codigos.add(amostra.getCodigo());
            for (ItemPedidoExame item : e.getValue()) {
                item.setAmostra(amostra);
                item.setStatus(StatusItemExame.COLETADO);
                itemRepository.save(item);
            }
        }
        registrar(pedido, null, TipoEventoExame.COLETA, "Amostra(s) " + String.join(", ", codigos) + " para " + laboratorio.getNome(), coletor);
        return detalhe(pedido);
    }

    /** Amostra que não serve: os exames ainda não liberados voltam a aguardar coleta. */
    @Transactional
    public PedidoExameResponseDto rejeitarAmostra(UUID amostraId, RejeicaoAmostraRequestDto dto) {
        AmostraExame amostra = amostraRepository.findById(amostraId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma amostra com o id " + amostraId + " em nossos registros."));
        ContextoAuditoria.paciente(amostra.getPedido().getPaciente().getUuid());
        controleDeAcesso.exigir(ANALISAR, amostra.getLaboratorio());
        if (amostra.isRejeitada()) {
            throw new ResourceUnprocessableEntityException("A amostra " + amostra.getCodigo() + " já foi rejeitada.");
        }
        List<ItemPedidoExame> itens = itemRepository.findByAmostra_Uuid(amostraId);
        if (itens.stream().anyMatch(i -> i.getStatus() == StatusItemExame.LIBERADO)) {
            throw new ResourceUnprocessableEntityException("A amostra tem resultado liberado; corrija com retificação.");
        }
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        amostra.setMotivoRejeicao(dto.getMotivo());
        amostra.setObservacaoRejeicao(textoOuNulo(dto.getObservacao()));
        amostra.setRejeitadaEm(Instant.now());
        amostra.setRejeitadaPor(profissional);
        amostraRepository.save(amostra);
        for (ItemPedidoExame item : itens) {
            if (item.getStatus() == StatusItemExame.CANCELADO) {
                continue;
            }
            item.setAmostra(null);
            item.setResultadoAtual(null);
            item.setStatus(StatusItemExame.SOLICITADO);
            itemRepository.save(item);
        }
        registrar(amostra.getPedido(), null, TipoEventoExame.REJEICAO_AMOSTRA,
                "Amostra " + amostra.getCodigo() + ": " + dto.getMotivo() + (dto.getObservacao() != null && !dto.getObservacao().isBlank()
                        ? " (" + dto.getObservacao().trim() + ")" : "") + ". Exames voltam para recoleta.", profissional);
        return detalhe(amostra.getPedido());
    }

    // ── Item ───────────────────────────────────────────────────────────────────────────────────────

    /** Registra (ou registra de novo, antes da liberação) o resultado de um exame coletado. */
    @Transactional
    public PedidoExameResponseDto registrarResultado(UUID itemId, ResultadoExameRequestDto dto) {
        ItemPedidoExame item = travarItem(itemId);
        controleDeAcesso.exigir(ANALISAR, item.getAmostra() != null ? item.getAmostra().getLaboratorio() : null);
        exigirStatus(item, "registrar resultado de", StatusItemExame.COLETADO, StatusItemExame.RESULTADO_REGISTRADO);
        Profissional analista = buscarProfissional(dto.getProfissionalMatricula());
        ResultadoExame resultado = novoResultado(item, dto.getValorNumerico(), dto.getValorTexto(), dto.getObservacao(), analista, null, null);
        item.setResultadoAtual(resultado);
        item.setStatus(StatusItemExame.RESULTADO_REGISTRADO);
        itemRepository.save(item);
        registrar(item.getPedido(), item, TipoEventoExame.RESULTADO, null, analista);
        return detalhe(item.getPedido());
    }

    /** Quem responde tecnicamente libera; a partir daí o resultado não muda. */
    @Transactional
    public PedidoExameResponseDto liberar(UUID itemId, ProfissionalExameRequestDto dto) {
        ItemPedidoExame item = travarItem(itemId);
        controleDeAcesso.exigir(LIBERAR, item.getAmostra() != null ? item.getAmostra().getLaboratorio() : null);
        exigirStatus(item, "liberar", StatusItemExame.RESULTADO_REGISTRADO);
        Profissional liberador = buscarProfissional(dto.getProfissionalMatricula());
        ResultadoExame resultado = item.getResultadoAtual();
        resultado.setLiberadoPor(liberador);
        resultado.setLiberadoEm(Instant.now());
        resultadoRepository.save(resultado);
        item.setStatus(StatusItemExame.LIBERADO);
        itemRepository.save(item);
        registrar(item.getPedido(), item, TipoEventoExame.LIBERACAO, null, liberador);
        return detalhe(item.getPedido());
    }

    /** Corrige resultado liberado: grava outro, que aponta para o corrigido e já sai liberado. */
    @Transactional
    public PedidoExameResponseDto retificar(UUID itemId, RetificacaoResultadoExameRequestDto dto) {
        ItemPedidoExame item = travarItem(itemId);
        controleDeAcesso.exigir(LIBERAR, item.getAmostra() != null ? item.getAmostra().getLaboratorio() : null);
        exigirStatus(item, "retificar", StatusItemExame.LIBERADO);
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        ResultadoExame anterior = item.getResultadoAtual();
        ResultadoExame novo = novoResultado(item, dto.getValorNumerico(), dto.getValorTexto(), dto.getObservacao(), profissional,
                anterior, dto.getMotivo().trim());
        item.setResultadoAtual(novo);
        itemRepository.save(item);
        registrar(item.getPedido(), item, TipoEventoExame.RETIFICACAO, dto.getMotivo().trim(), profissional);
        return detalhe(item.getPedido());
    }

    /** Cancela um exame ainda não liberado: quem pediu, ou o laboratório que está com a amostra. */
    @Transactional
    public PedidoExameResponseDto cancelar(UUID itemId, CancelamentoItemExameRequestDto dto) {
        ItemPedidoExame item = travarItem(itemId);
        try {
            controleDeAcesso.exigir(SOLICITAR, item.getPedido().getUnidadeSolicitante());
        } catch (ResourceForbiddenException semSolicitar) {
            controleDeAcesso.exigir(ANALISAR, item.getAmostra() != null ? item.getAmostra().getLaboratorio() : item.getPedido().getUnidadeSolicitante());
        }
        exigirStatus(item, "cancelar", StatusItemExame.SOLICITADO, StatusItemExame.COLETADO, StatusItemExame.RESULTADO_REGISTRADO);
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        item.setStatus(StatusItemExame.CANCELADO);
        item.setMotivoCancelamento(dto.getMotivo().trim());
        itemRepository.save(item);
        registrar(item.getPedido(), item, TipoEventoExame.CANCELAMENTO, dto.getMotivo().trim(), profissional);
        return detalhe(item.getPedido());
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** Pedidos sem valores, do mais recente ao mais antigo, das unidades (solicitante, coleta, laboratório) no escopo. */
    @Transactional(readOnly = true)
    public List<PedidoExameResumoDto> listar(UUID pacienteId, String situacao) {
        List<PedidoExame> pedidos = pacienteId != null ? pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(pacienteId)
                : pedidoRepository.findAllByOrderBySolicitadoEmDesc();
        Map<UUID, List<ItemPedidoExame>> itens = itensPorPedido(pedidos);
        Map<UUID, List<AmostraExame>> amostras = new LinkedHashMap<>();
        return controleDeAcesso.filtrar(pedidos.stream(), p -> unidadesDo(p, itens.getOrDefault(p.getUuid(), List.of()), amostras), VER)
                .map(p -> PedidoExameResumoDto.fromPedido(p, itens.getOrDefault(p.getUuid(), List.of())))
                .filter(r -> situacao == null || situacao.isBlank() || r.getSituacao().equals(situacao))
                .toList();
    }

    /**
     * Lista de trabalho por exame, sem valores: PARA_COLETAR (aguardando coleta, nas unidades solicitantes do escopo),
     * EM_ANALISE (coletados, no laboratório) e PARA_LIBERAR (com resultado, no laboratório). Urgente primeiro.
     */
    @Transactional(readOnly = true)
    public List<ItemTrabalhoExameDto> trabalho(String etapa) {
        StatusItemExame status;
        String permissao;
        switch (etapa) {
            case "PARA_COLETAR" -> { status = StatusItemExame.SOLICITADO; permissao = COLETAR; }
            case "EM_ANALISE" -> { status = StatusItemExame.COLETADO; permissao = ANALISAR; }
            case "PARA_LIBERAR" -> { status = StatusItemExame.RESULTADO_REGISTRADO; permissao = LIBERAR; }
            default -> throw new ResourceBadRequestException("Etapa inválida: use PARA_COLETAR, EM_ANALISE ou PARA_LIBERAR.");
        }
        List<ItemPedidoExame> itens = itemRepository.findByStatus(status);
        Map<String, MotivoRejeicaoAmostra> recoleta = status == StatusItemExame.SOLICITADO ? recoletas(itens) : Map.of();
        return controleDeAcesso.filtrar(itens.stream(), i -> Stream.of(status == StatusItemExame.SOLICITADO
                        ? i.getPedido().getUnidadeSolicitante() : i.getAmostra().getLaboratorio()), permissao)
                .sorted(ORDEM_DE_TRABALHO)
                .map(i -> ItemTrabalhoExameDto.fromItem(i, recoleta.get(chaveRecoleta(i.getPedido().getUuid(), i.getExame().getMaterial()))))
                .toList();
    }

    /**
     * Pedido e material → motivo da rejeição mais recente. A amostra é uma por material e a rejeição solta os exames dela,
     * então o exame aguardando coleta com amostra rejeitada do mesmo material no pedido é recoleta (ADR-0095).
     */
    private Map<String, MotivoRejeicaoAmostra> recoletas(List<ItemPedidoExame> aguardando) {
        if (aguardando.isEmpty()) {
            return Map.of();
        }
        Map<String, AmostraExame> maisRecente = new java.util.HashMap<>();
        for (AmostraExame a : amostraRepository.findByPedido_UuidInAndRejeitadaEmIsNotNull(
                aguardando.stream().map(i -> i.getPedido().getUuid()).distinct().toList())) {
            maisRecente.merge(chaveRecoleta(a.getPedido().getUuid(), a.getMaterial()), a,
                    (x, y) -> x.getRejeitadaEm().isAfter(y.getRejeitadaEm()) ? x : y);
        }
        Map<String, MotivoRejeicaoAmostra> motivos = new java.util.HashMap<>();
        maisRecente.forEach((k, a) -> motivos.put(k, a.getMotivoRejeicao()));
        return motivos;
    }

    private static String chaveRecoleta(UUID pedidoId, MaterialExame material) {
        return pedidoId + ":" + material;
    }

    /** O detalhe com indicação clínica, resultados, amostras e eventos: só no escopo de uma das unidades do pedido. */
    @Transactional(readOnly = true)
    public PedidoExameResponseDto buscarPorId(UUID uuid) {
        PedidoExame pedido = buscarPedido(uuid);
        ContextoAuditoria.unidade(pedido.getUnidadeSolicitante());
        List<ItemPedidoExame> itens = itemRepository.findByPedido_Uuid(uuid);
        controleDeAcesso.filtrar(Stream.of(pedido), p -> unidadesDo(p, itens, new LinkedHashMap<>()), VER).findAny()
                .orElseThrow(() -> new ResourceForbiddenException("Este pedido é de unidades fora do seu acesso."));
        return detalhe(pedido);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** Grava o resultado de uma vez: as colunas são imutáveis. Com {@code retificacaoDe}, já sai liberado por quem retificou. */
    private ResultadoExame novoResultado(ItemPedidoExame item, BigDecimal valorNumerico, String valorTexto, String observacao,
                                         Profissional analista, ResultadoExame retificacaoDe, String motivoRetificacao) {
        ExameLaboratorial exame = item.getExame();
        ResultadoExame r = new ResultadoExame();
        r.setItem(item);
        if (exame.getTipoResultado() == TipoResultadoExame.NUMERICO) {
            if (valorNumerico == null) {
                throw new ResourceBadRequestException("O exame " + exame.getNome() + " tem resultado numérico: informe o valor.");
            }
            r.setValorNumerico(valorNumerico);
            r.setUnidadeMedida(exame.getUnidadeMedida());
            r.setReferenciaMinima(exame.getReferenciaMinima());
            r.setReferenciaMaxima(exame.getReferenciaMaxima());
            r.setInterpretacao(interpretar(valorNumerico, exame.getReferenciaMinima(), exame.getReferenciaMaxima()));
        } else {
            if (textoOuNulo(valorTexto) == null) {
                throw new ResourceBadRequestException("O exame " + exame.getNome() + " tem resultado em texto: informe o resultado.");
            }
            r.setValorTexto(valorTexto.trim());
            r.setReferenciaTexto(exame.getReferenciaTexto());
        }
        r.setObservacao(textoOuNulo(observacao));
        r.setAnalisadoPor(analista);
        r.setRegistradoEm(Instant.now());
        r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        if (retificacaoDe != null) {
            r.setRetificacaoDe(retificacaoDe);
            r.setMotivoRetificacao(motivoRetificacao);
            r.setLiberadoPor(analista);
            r.setLiberadoEm(Instant.now());
        }
        return resultadoRepository.save(r);
    }

    static InterpretacaoResultado interpretar(BigDecimal valor, BigDecimal minima, BigDecimal maxima) {
        if (minima == null && maxima == null) {
            return null;
        }
        if (minima != null && valor.compareTo(minima) < 0) {
            return InterpretacaoResultado.ABAIXO;
        }
        if (maxima != null && valor.compareTo(maxima) > 0) {
            return InterpretacaoResultado.ACIMA;
        }
        return InterpretacaoResultado.NORMAL;
    }

    private Map<UUID, List<ItemPedidoExame>> itensPorPedido(List<PedidoExame> pedidos) {
        if (pedidos.isEmpty()) {
            return Map.of();
        }
        return itemRepository.findByPedido_UuidIn(pedidos.stream().map(PedidoExame::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(i -> i.getPedido().getUuid()));
    }

    /** As unidades que respondem pelo pedido: a solicitante, e a coleta e o laboratório das amostras. */
    private Stream<UnidadeDeSaude> unidadesDo(PedidoExame p, List<ItemPedidoExame> itens, Map<UUID, List<AmostraExame>> cache) {
        Stream<UnidadeDeSaude> daAmostra = itens.stream().filter(i -> i.getAmostra() != null)
                .flatMap(i -> Stream.of(i.getAmostra().getUnidadeColeta(), i.getAmostra().getLaboratorio()));
        return Stream.concat(Stream.of(p.getUnidadeSolicitante()), daAmostra);
    }

    private PedidoExameResponseDto detalhe(PedidoExame pedido) {
        ContextoAuditoria.paciente(pedido.getPaciente().getUuid());
        ContextoAuditoria.registro(pedido.getUuid());
        return PedidoExameResponseDto.fromPedido(pedido, itemRepository.findByPedido_Uuid(pedido.getUuid()),
                amostraRepository.findByPedido_UuidOrderByColetadaEmAsc(pedido.getUuid()),
                eventoRepository.findByPedido_UuidOrderByOcorridoEmAsc(pedido.getUuid()));
    }

    private void registrar(PedidoExame pedido, ItemPedidoExame item, TipoEventoExame tipo, String texto, Profissional profissional) {
        EventoExame e = new EventoExame();
        e.setPedido(pedido);
        e.setItem(item);
        e.setTipo(tipo);
        e.setTexto(texto);
        e.setProfissional(profissional);
        e.setOcorridoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
    }

    private String novoCodigo() {
        for (int tentativa = 0; tentativa < 20; tentativa++) {
            StringBuilder sb = new StringBuilder("AM").append(DIA.format(LocalDateTime.now())).append('-');
            for (int k = 0; k < 4; k++) {
                sb.append(LETRAS.charAt(SORTEIO.nextInt(LETRAS.length())));
            }
            if (!amostraRepository.existsByCodigo(sb.toString())) {
                return sb.toString();
            }
        }
        throw new IllegalStateException("Não foi possível gerar um código de amostra livre.");
    }

    private ItemPedidoExame travarItem(UUID itemId) {
        ItemPedidoExame item = itemRepository.findByIdParaAtualizar(itemId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um exame de pedido com o id " + itemId + " em nossos registros."));
        ContextoAuditoria.paciente(item.getPedido().getPaciente().getUuid());
        return item;
    }

    private static void exigirStatus(ItemPedidoExame item, String acao, StatusItemExame... permitidos) {
        if (Stream.of(permitidos).noneMatch(s -> s == item.getStatus())) {
            throw new ResourceUnprocessableEntityException("Não é possível " + acao + " um exame " + item.getStatus() + ".");
        }
    }

    private PedidoExame buscarPedido(UUID uuid) {
        return pedidoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um pedido de exame com o id " + uuid + " em nossos registros."));
    }

    private Profissional buscarProfissional(String matricula) {
        return profissionalRepository.findByMatricula(matricula).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    private UnidadeDeSaude buscarUnidade(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
