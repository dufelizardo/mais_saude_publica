package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AmostraExame;
import com.edufelizardo.maissaudepublica.models.ComunicacaoCriticoExame;
import com.edufelizardo.maissaudepublica.models.EntregaResultadoExame;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.EventoExame;
import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.FaixaReferenciaExame;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FaixaReferenciaExameDto;
import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.PedidoExame;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.ResultadoExame;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CancelamentoItemExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ColetaExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ComunicacaoCriticoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EntregaResultadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PedidoExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RejeicaoAmostraRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResultadoExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoResultadoExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ItemTrabalhoExameDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PedidoExameResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PedidoExameResumoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ResultadoAvisoExameDto;
import com.edufelizardo.maissaudepublica.models.enuns.FormaEntregaResultado;
import com.edufelizardo.maissaudepublica.models.enuns.InterpretacaoResultado;
import com.edufelizardo.maissaudepublica.models.enuns.MeioComunicacaoCritico;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoRejeicaoAmostra;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeExame;
import com.edufelizardo.maissaudepublica.models.enuns.StatusItemExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import com.edufelizardo.maissaudepublica.repositories.AmostraExameRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ComunicacaoCriticoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.EntregaResultadoExameRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
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
    /** Exames de imagem (ADR-0182): o técnico realiza e o radiologista lauda, no serviço de imagem do pedido. */
    public static final String REALIZAR = "IMAGEM.REALIZAR";
    public static final String LAUDAR = "IMAGEM.LAUDAR";
    static final String[] VER = {SOLICITAR, COLETAR, ANALISAR, LIBERAR, REALIZAR, LAUDAR};
    private static final String CAPACIDADE_IMAGEM = "DIAGNOSTICO_POR_IMAGEM";

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

    @Autowired
    private com.edufelizardo.maissaudepublica.repositories.RealizacaoExameImagemRepository realizacaoRepository;

    @Autowired
    private com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository capacidadeRepository;

    @Autowired
    private ComunicacaoCriticoExameRepository comunicacaoRepository;

    @Autowired
    private EntregaResultadoExameRepository entregaRepository;

    @Autowired
    private AvisoResultadoCritico avisoResultadoCritico;

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
        // Com exame de imagem, o pedido aponta o serviço de imagem, que precisa da capacidade no modelo operacional (ADR-0182).
        UnidadeDeSaude executanteImagem = null;
        if (exames.stream().anyMatch(PedidoExameService::deImagem)) {
            if (dto.getUnidadeExecutanteImagemId() == null) {
                throw new ResourceBadRequestException("O pedido tem exame de imagem: informe o serviço de imagem que vai realizá-lo.");
            }
            executanteImagem = buscarUnidade(dto.getUnidadeExecutanteImagemId());
            if (!capacidadeRepository.existsByUnidade_UuidAndCapacidade_CodigoAndFimIsNull(executanteImagem.getUuid(), CAPACIDADE_IMAGEM)) {
                throw new ResourceUnprocessableEntityException("A unidade " + executanteImagem.getNome()
                        + " não tem diagnóstico por imagem (capacidade do modelo operacional).");
            }
        }

        PedidoExame pedido = new PedidoExame();
        pedido.setUnidadeExecutanteImagem(executanteImagem);
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
                .filter(i -> i.getStatus() == StatusItemExame.SOLICITADO && !deImagem(i.getExame())).toList();
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
        avisarSeCritico(item, resultado);
        return detalhe(item.getPedido());
    }

    /** Corrige resultado liberado: grava outro, que aponta para o corrigido e já sai liberado. */
    @Transactional
    public PedidoExameResponseDto retificar(UUID itemId, RetificacaoResultadoExameRequestDto dto) {
        ItemPedidoExame item = travarItem(itemId);
        controleDeAcesso.exigir(LIBERAR, item.getAmostra() != null ? item.getAmostra().getLaboratorio() : null);
        exigirStatus(item, "retificar", StatusItemExame.LIBERADO);
        if (deImagem(item.getExame())) {
            throw new ResourceUnprocessableEntityException("Laudo de imagem se corrige por adendo do radiologista.");
        }
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        ResultadoExame anterior = item.getResultadoAtual();
        ResultadoExame novo = novoResultado(item, dto.getValorNumerico(), dto.getValorTexto(), dto.getObservacao(), profissional,
                anterior, dto.getMotivo().trim());
        item.setResultadoAtual(novo);
        itemRepository.save(item);
        registrar(item.getPedido(), item, TipoEventoExame.RETIFICACAO, dto.getMotivo().trim(), profissional);
        avisarSeCritico(item, novo);
        return detalhe(item.getPedido());
    }

    /** Cancela um exame ainda não liberado: quem pediu, ou o laboratório que está com a amostra. */
    @Transactional
    public PedidoExameResponseDto cancelar(UUID itemId, CancelamentoItemExameRequestDto dto) {
        ItemPedidoExame item = travarItem(itemId);
        try {
            controleDeAcesso.exigir(SOLICITAR, item.getPedido().getUnidadeSolicitante());
        } catch (ResourceForbiddenException semSolicitar) {
            if (deImagem(item.getExame())) {
                controleDeAcesso.exigir(REALIZAR, item.getPedido().getUnidadeExecutanteImagem());
            } else {
                controleDeAcesso.exigir(ANALISAR, item.getAmostra() != null ? item.getAmostra().getLaboratorio() : item.getPedido().getUnidadeSolicitante());
            }
        }
        exigirStatus(item, "cancelar", StatusItemExame.SOLICITADO, StatusItemExame.COLETADO, StatusItemExame.REALIZADO,
                StatusItemExame.RESULTADO_REGISTRADO);
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
     * EM_ANALISE (coletados, no laboratório) e PARA_LIBERAR (com resultado, no laboratório); e, dos exames de imagem, PARA_REALIZAR
     * e PARA_LAUDAR, no serviço de imagem do pedido (ADR-0182). Urgente primeiro.
     */
    @Transactional(readOnly = true)
    public List<ItemTrabalhoExameDto> trabalho(String etapa) {
        StatusItemExame status;
        String permissao;
        switch (etapa) {
            case "PARA_COLETAR" -> { status = StatusItemExame.SOLICITADO; permissao = COLETAR; }
            case "EM_ANALISE" -> { status = StatusItemExame.COLETADO; permissao = ANALISAR; }
            case "PARA_LIBERAR" -> { status = StatusItemExame.RESULTADO_REGISTRADO; permissao = LIBERAR; }
            case "PARA_REALIZAR", "PARA_LAUDAR" -> {
                StatusItemExame s = etapa.equals("PARA_REALIZAR") ? StatusItemExame.SOLICITADO : StatusItemExame.REALIZADO;
                return controleDeAcesso.filtrar(itemRepository.findByStatus(s).stream().filter(i -> deImagem(i.getExame())),
                                i -> Stream.of(i.getPedido().getUnidadeExecutanteImagem()), etapa.equals("PARA_REALIZAR") ? REALIZAR : LAUDAR)
                        .sorted(ORDEM_DE_TRABALHO)
                        .map(i -> ItemTrabalhoExameDto.fromItem(i, null))
                        .toList();
            }
            default -> throw new ResourceBadRequestException(
                    "Etapa inválida: use PARA_COLETAR, EM_ANALISE, PARA_LIBERAR, PARA_REALIZAR ou PARA_LAUDAR.");
        }
        List<ItemPedidoExame> itens = itemRepository.findByStatus(status).stream().filter(i -> !deImagem(i.getExame())).toList();
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

    // ── Ciência, comunicação do crítico e entrega (ADR-0181) ───────────────────────────────────────

    private static final Map<MeioComunicacaoCritico, String> MEIOS = Map.of(MeioComunicacaoCritico.TELEFONE, "telefone",
            MeioComunicacaoCritico.PRESENCIAL, "presencial", MeioComunicacaoCritico.MENSAGEM_NO_SISTEMA, "mensagem no sistema");

    /**
     * Os resultados liberados dos pedidos do profissional ainda sem ciência: críticos primeiro, depois do mais antigo ao mais
     * novo. Com login, é sempre o profissional logado; outra matrícula dá 403. Sem login (segurança desligada), a matrícula
     * é obrigatória (400).
     */
    @Transactional(readOnly = true)
    public List<ResultadoAvisoExameDto> paraCiencia(String matricula) {
        Profissional profissional = profissionalDaAcao(matricula, "ver os resultados para ciência");
        List<ItemPedidoExame> itens = itemRepository.findParaCiencia(profissional.getUuid());
        Map<UUID, Instant> comunicados = comunicadosEm(itens);
        return itens.stream()
                .sorted(Comparator.comparing((ItemPedidoExame i) -> ResultadoAvisoExameDto.critico(i.getResultadoAtual().getInterpretacao()) ? 0 : 1)
                        .thenComparing(i -> i.getResultadoAtual().getLiberadoEm()))
                .map(i -> ResultadoAvisoExameDto.fromItem(i, comunicados.get(i.getResultadoAtual().getUuid())))
                .toList();
    }

    /** Ciência do solicitante em um resultado liberado. Só quem pediu (422); uma vez por resultado (409). */
    @Transactional
    public PedidoExameResponseDto darCiencia(UUID itemId, ProfissionalExameRequestDto dto) {
        ItemPedidoExame item = travarItem(itemId);
        Profissional profissional = solicitanteDaCiencia(item.getPedido(), dto.getProfissionalMatricula());
        exigirStatus(item, "dar ciência de", StatusItemExame.LIBERADO);
        if (item.getResultadoAtual().getCienciaEm() != null) {
            throw new ResourceConflictException("A ciência deste resultado já foi registrada.");
        }
        registrarCiencia(item, profissional);
        return detalhe(item.getPedido());
    }

    /** Ciência de todos os resultados liberados do pedido ainda sem ciência. Nenhum pendente: 409. */
    @Transactional
    public PedidoExameResponseDto darCienciaDoPedido(UUID pedidoId, ProfissionalExameRequestDto dto) {
        PedidoExame pedido = buscarPedido(pedidoId);
        ContextoAuditoria.paciente(pedido.getPaciente().getUuid());
        Profissional profissional = solicitanteDaCiencia(pedido, dto.getProfissionalMatricula());
        List<ItemPedidoExame> pendentes = itemRepository.findByPedido_Uuid(pedidoId).stream()
                .filter(i -> i.getStatus() == StatusItemExame.LIBERADO && i.getResultadoAtual().getCienciaEm() == null).toList();
        if (pendentes.isEmpty()) {
            throw new ResourceConflictException("Não há resultado liberado aguardando ciência neste pedido.");
        }
        pendentes.forEach(i -> registrarCiencia(i, profissional));
        return detalhe(pedido);
    }

    /**
     * Os críticos liberados ainda sem comunicação registrada e sem ciência do solicitante, nos laboratórios do escopo de quem
     * libera. Do mais antigo ao mais novo.
     */
    @Transactional(readOnly = true)
    public List<ResultadoAvisoExameDto> criticosAComunicar() {
        List<ItemPedidoExame> criticos = itemRepository.findCriticosSemCiencia();
        Map<UUID, Instant> comunicados = comunicadosEm(criticos);
        List<ItemPedidoExame> pendentes = criticos.stream().filter(i -> !comunicados.containsKey(i.getResultadoAtual().getUuid())).toList();
        // Laboratório: quem libera no laboratório da amostra; imagem: o radiologista do serviço de imagem (ADR-0182).
        Stream<ItemPedidoExame> doLaboratorio = controleDeAcesso.filtrar(pendentes.stream().filter(i -> !deImagem(i.getExame())),
                i -> Stream.of(i.getAmostra().getLaboratorio()), LIBERAR);
        Stream<ItemPedidoExame> daImagem = controleDeAcesso.filtrar(pendentes.stream().filter(i -> deImagem(i.getExame())),
                i -> Stream.of(i.getPedido().getUnidadeExecutanteImagem()), LAUDAR);
        return Stream.concat(doLaboratorio, daImagem)
                .sorted(Comparator.comparing(i -> i.getResultadoAtual().getLiberadoEm()))
                .map(i -> ResultadoAvisoExameDto.fromItem(i, null))
                .toList();
    }

    /**
     * O laboratório registra que comunicou o valor crítico (RDC 786/2023): quem recebeu, por qual meio e quando. Só resultado
     * crítico liberado (422); uma por resultado (409); nem no futuro nem antes da liberação (422).
     */
    @Transactional
    public PedidoExameResponseDto comunicarCritico(UUID itemId, ComunicacaoCriticoRequestDto dto) {
        ItemPedidoExame item = travarItem(itemId);
        if (deImagem(item.getExame())) {
            controleDeAcesso.exigir(LAUDAR, item.getPedido().getUnidadeExecutanteImagem());
        } else {
            controleDeAcesso.exigir(LIBERAR, item.getAmostra() != null ? item.getAmostra().getLaboratorio() : null);
        }
        exigirStatus(item, "comunicar o crítico de", StatusItemExame.LIBERADO);
        ResultadoExame resultado = item.getResultadoAtual();
        if (!ResultadoAvisoExameDto.critico(resultado.getInterpretacao())) {
            throw new ResourceUnprocessableEntityException("O resultado de " + item.getExame().getNome() + " não é crítico.");
        }
        if (comunicacaoRepository.existsByResultado_Uuid(resultado.getUuid())) {
            throw new ResourceConflictException("A comunicação deste resultado crítico já foi registrada.");
        }
        LocalDateTime quando = dto.getComunicadoEm() != null ? dto.getComunicadoEm() : LocalDateTime.now().withNano(0);
        if (quando.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new ResourceUnprocessableEntityException("A comunicação não pode estar no futuro.");
        }
        Instant comunicadoEm = quando.atZone(ZoneId.systemDefault()).toInstant();
        if (resultado.getLiberadoEm() != null && comunicadoEm.isBefore(resultado.getLiberadoEm().minusSeconds(300))) {
            throw new ResourceUnprocessableEntityException("A comunicação não pode ser anterior à liberação do resultado.");
        }
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        ComunicacaoCriticoExame c = new ComunicacaoCriticoExame();
        c.setItem(item);
        c.setResultado(resultado);
        c.setReceptorNome(dto.getReceptorNome().trim());
        c.setMeio(dto.getMeio());
        c.setComunicadoEm(comunicadoEm);
        c.setProfissional(profissional);
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        comunicacaoRepository.save(c);
        registrar(item.getPedido(), item, TipoEventoExame.COMUNICACAO_CRITICO,
                "Comunicado a " + c.getReceptorNome() + " (" + MEIOS.get(dto.getMeio()) + ")", profissional);
        return detalhe(item.getPedido());
    }

    /**
     * Entrega do resultado ao paciente, na unidade informada: quem coleta ou quem pede exames ali (403). Pedido sem exame em
     * andamento e com ao menos um liberado (422). Terceiro pede nome e documento (400). Segunda via é outra entrega.
     */
    @Transactional
    public PedidoExameResponseDto entregar(UUID pedidoId, EntregaResultadoRequestDto dto) {
        PedidoExame pedido = buscarPedido(pedidoId);
        ContextoAuditoria.paciente(pedido.getPaciente().getUuid());
        UnidadeDeSaude unidade = buscarUnidade(dto.getUnidadeId());
        try {
            controleDeAcesso.exigir(COLETAR, unidade);
        } catch (ResourceForbiddenException semColetar) {
            controleDeAcesso.exigir(SOLICITAR, unidade);
        }
        List<ItemPedidoExame> itens = itemRepository.findByPedido_Uuid(pedidoId);
        if (itens.stream().anyMatch(i -> i.getStatus() == StatusItemExame.SOLICITADO || i.getStatus() == StatusItemExame.COLETADO
                || i.getStatus() == StatusItemExame.REALIZADO || i.getStatus() == StatusItemExame.RESULTADO_REGISTRADO)) {
            throw new ResourceUnprocessableEntityException("O pedido ainda tem exame em andamento: entregue quando todos estiverem liberados.");
        }
        if (itens.stream().noneMatch(i -> i.getStatus() == StatusItemExame.LIBERADO)) {
            throw new ResourceUnprocessableEntityException("O pedido não tem resultado liberado para entregar.");
        }
        boolean peloPaciente = Boolean.TRUE.equals(dto.getRetiradoPeloPaciente());
        if (!peloPaciente && (textoOuNulo(dto.getTerceiroNome()) == null || textoOuNulo(dto.getTerceiroDocumento()) == null)) {
            throw new ResourceBadRequestException("Retirada por outra pessoa: informe o nome e o documento de quem retirou.");
        }
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        boolean segundaVia = !entregaRepository.findByPedido_UuidOrderByEntregueEmAsc(pedidoId).isEmpty();
        EntregaResultadoExame e = new EntregaResultadoExame();
        e.setPedido(pedido);
        e.setRetiradoPeloPaciente(peloPaciente);
        e.setTerceiroNome(peloPaciente ? null : dto.getTerceiroNome().trim());
        e.setTerceiroDocumento(peloPaciente ? null : dto.getTerceiroDocumento().trim());
        e.setTerceiroParentesco(peloPaciente ? null : textoOuNulo(dto.getTerceiroParentesco()));
        e.setForma(dto.getForma());
        e.setUnidade(unidade);
        e.setProfissional(profissional);
        e.setEntregueEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        entregaRepository.save(e);
        String quem = peloPaciente ? "ao paciente"
                : "a " + e.getTerceiroNome() + (e.getTerceiroParentesco() != null ? " (" + e.getTerceiroParentesco() + ")" : "");
        registrar(pedido, null, TipoEventoExame.ENTREGA, (segundaVia ? "Segunda via entregue " : "Resultado entregue ")
                + (dto.getForma() == FormaEntregaResultado.NA_CONSULTA ? "na consulta " : "impresso ") + quem, profissional);
        return detalhe(pedido);
    }

    private void registrarCiencia(ItemPedidoExame item, Profissional profissional) {
        ResultadoExame r = item.getResultadoAtual();
        r.setCienciaPor(profissional);
        r.setCienciaEm(Instant.now());
        resultadoRepository.save(r);
        registrar(item.getPedido(), item, TipoEventoExame.CIENCIA, null, profissional);
    }

    /** Quem dá ciência é o solicitante do pedido (422), e, com login, o próprio profissional logado (403). */
    private Profissional solicitanteDaCiencia(PedidoExame pedido, String matricula) {
        Profissional profissional = profissionalDaAcao(matricula, "dar ciência de resultado");
        if (!profissional.getUuid().equals(pedido.getProfissionalSolicitante().getUuid())) {
            throw new ResourceUnprocessableEntityException("Só quem pediu o exame registra a ciência do resultado.");
        }
        return profissional;
    }

    /** Com login, o profissional logado (outra matrícula: 403); sem login, a matrícula informada (obrigatória, 400). */
    private Profissional profissionalDaAcao(String matricula, String acao) {
        String cpf = UsuarioAutenticado.cpf();
        Profissional logado = cpf == null ? null : profissionalRepository.findByCpfAndAtivoTrue(cpf).orElse(null);
        if (logado != null) {
            if (textoOuNulo(matricula) != null && !matricula.trim().equals(logado.getMatricula())) {
                throw new ResourceForbiddenException("Só o próprio profissional pode " + acao + ".");
            }
            return logado;
        }
        if (cpf != null) {
            throw new ResourceForbiddenException("O seu usuário não tem cadastro de profissional para " + acao + ".");
        }
        if (textoOuNulo(matricula) == null) {
            throw new ResourceBadRequestException("Informe a matrícula do profissional.");
        }
        return buscarProfissional(matricula.trim());
    }

    /** Resultado → quando o crítico foi comunicado. */
    private Map<UUID, Instant> comunicadosEm(List<ItemPedidoExame> itens) {
        if (itens.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Instant> mapa = new java.util.HashMap<>();
        comunicacaoRepository.findByResultado_UuidIn(itens.stream().map(i -> i.getResultadoAtual().getUuid()).toList())
                .forEach(c -> mapa.merge(c.getResultado().getUuid(), c.getComunicadoEm(), (x, y) -> x.isBefore(y) ? x : y));
        return mapa;
    }

    private void avisarSeCritico(ItemPedidoExame item, ResultadoExame resultado) {
        if (ResultadoAvisoExameDto.critico(resultado.getInterpretacao())) {
            avisoResultadoCritico.avisar(item);
        }
    }

    // ── Exames de imagem (ADR-0182) ────────────────────────────────────────────────────────────────

    static boolean deImagem(ExameLaboratorial exame) {
        return exame.getCategoria() == com.edufelizardo.maissaudepublica.models.enuns.CategoriaExame.IMAGEM;
    }

    /**
     * O técnico registra a realização no serviço de imagem do pedido (403 fora dele): quando, equipamento e contraste. Só exame
     * de imagem aguardando (422); nada no futuro (422).
     */
    @Transactional
    public PedidoExameResponseDto realizar(UUID itemId, com.edufelizardo.maissaudepublica.models.dtos.version1.request.RealizacaoExameImagemRequestDto dto) {
        ItemPedidoExame item = travarItem(itemId);
        if (!deImagem(item.getExame())) {
            throw new ResourceUnprocessableEntityException("O exame " + item.getExame().getNome() + " é laboratorial: registre a coleta.");
        }
        UnidadeDeSaude servico = item.getPedido().getUnidadeExecutanteImagem();
        controleDeAcesso.exigir(REALIZAR, servico);
        exigirStatus(item, "realizar", StatusItemExame.SOLICITADO);
        LocalDateTime quando = dto.getRealizadaEm() != null ? dto.getRealizadaEm() : LocalDateTime.now().withNano(0);
        if (quando.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new ResourceUnprocessableEntityException("A realização não pode estar no futuro.");
        }
        boolean contraste = Boolean.TRUE.equals(dto.getContraste());
        if (contraste && textoOuNulo(dto.getContrasteDescricao()) == null) {
            throw new ResourceBadRequestException("Com contraste: informe qual contraste foi usado.");
        }
        Profissional tecnico = buscarProfissional(dto.getProfissionalMatricula());
        com.edufelizardo.maissaudepublica.models.RealizacaoExameImagem r = new com.edufelizardo.maissaudepublica.models.RealizacaoExameImagem();
        r.setItem(item);
        r.setUnidade(servico);
        r.setRealizadaEm(quando.atZone(ZoneId.systemDefault()).toInstant());
        r.setTecnico(tecnico);
        r.setEquipamento(textoOuNulo(dto.getEquipamento()));
        r.setContraste(contraste);
        r.setContrasteDescricao(contraste ? dto.getContrasteDescricao().trim() : null);
        r.setObservacao(textoOuNulo(dto.getObservacao()));
        r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        realizacaoRepository.save(r);
        item.setStatus(StatusItemExame.REALIZADO);
        itemRepository.save(item);
        registrar(item.getPedido(), item, TipoEventoExame.REALIZACAO, "No " + servico.getNome()
                + (r.getEquipamento() != null ? ", " + r.getEquipamento() : "") + (contraste ? ", com contraste" : ""), tecnico);
        return detalhe(item.getPedido());
    }

    /**
     * O radiologista escreve e assina o laudo num passo só: o laudo sai liberado (não há a liberação técnica do laboratório).
     * Só exame realizado (422). Achado crítico segue a comunicação da ADR-0181.
     */
    @Transactional
    public PedidoExameResponseDto laudar(UUID itemId, com.edufelizardo.maissaudepublica.models.dtos.version1.request.LaudoImagemRequestDto dto) {
        ItemPedidoExame item = itemDeImagem(itemId);
        exigirStatus(item, "laudar", StatusItemExame.REALIZADO);
        Profissional radiologista = buscarProfissional(dto.getProfissionalMatricula());
        ResultadoExame laudo = novoLaudo(item, dto, radiologista, null, null);
        item.setResultadoAtual(laudo);
        item.setStatus(StatusItemExame.LIBERADO);
        itemRepository.save(item);
        registrar(item.getPedido(), item, TipoEventoExame.LAUDO, CLASSIFICACOES.get(laudo.getInterpretacao()), radiologista);
        avisarSeCritico(item, laudo);
        return detalhe(item.getPedido());
    }

    /** Adendo ao laudo liberado, com motivo: grava outro laudo ligado ao anterior, já liberado. */
    @Transactional
    public PedidoExameResponseDto adendo(UUID itemId, com.edufelizardo.maissaudepublica.models.dtos.version1.request.LaudoImagemRequestDto dto) {
        ItemPedidoExame item = itemDeImagem(itemId);
        exigirStatus(item, "registrar adendo de", StatusItemExame.LIBERADO);
        if (textoOuNulo(dto.getMotivo()) == null) {
            throw new ResourceBadRequestException("Informe o motivo do adendo.");
        }
        Profissional radiologista = buscarProfissional(dto.getProfissionalMatricula());
        ResultadoExame laudo = novoLaudo(item, dto, radiologista, item.getResultadoAtual(), dto.getMotivo().trim());
        item.setResultadoAtual(laudo);
        itemRepository.save(item);
        registrar(item.getPedido(), item, TipoEventoExame.ADENDO, dto.getMotivo().trim(), radiologista);
        avisarSeCritico(item, laudo);
        return detalhe(item.getPedido());
    }

    private static final Map<InterpretacaoResultado, String> CLASSIFICACOES = Map.of(InterpretacaoResultado.NORMAL, "Sem alterações",
            InterpretacaoResultado.ALTERADO, "Com alterações", InterpretacaoResultado.ACHADO_CRITICO, "Achado crítico");

    private ItemPedidoExame itemDeImagem(UUID itemId) {
        ItemPedidoExame item = travarItem(itemId);
        if (!deImagem(item.getExame())) {
            throw new ResourceUnprocessableEntityException("O exame " + item.getExame().getNome() + " é laboratorial: não tem laudo de imagem.");
        }
        controleDeAcesso.exigir(LAUDAR, item.getPedido().getUnidadeExecutanteImagem());
        return item;
    }

    private ResultadoExame novoLaudo(ItemPedidoExame item, com.edufelizardo.maissaudepublica.models.dtos.version1.request.LaudoImagemRequestDto dto,
                                     Profissional radiologista, ResultadoExame adendoDe, String motivo) {
        if (!CLASSIFICACOES.containsKey(dto.getClassificacao())) {
            throw new ResourceBadRequestException("Classifique o laudo como NORMAL (sem alterações), ALTERADO ou ACHADO_CRITICO.");
        }
        String url = textoOuNulo(dto.getImagemUrl());
        if (url != null && !url.toLowerCase(Locale.ROOT).startsWith("https://")) {
            throw new ResourceBadRequestException("O link das imagens precisa começar com https://.");
        }
        Instant agora = Instant.now();
        ResultadoExame r = new ResultadoExame();
        r.setItem(item);
        r.setLaudoTecnica(textoOuNulo(dto.getTecnica()));
        r.setLaudoAchados(dto.getAchados().trim());
        r.setLaudoConclusao(dto.getConclusao().trim());
        r.setValorTexto(dto.getConclusao().trim());
        r.setImagemUrl(url);
        r.setInterpretacao(dto.getClassificacao());
        r.setAnalisadoPor(radiologista);
        r.setRegistradoEm(agora);
        r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        r.setLiberadoPor(radiologista);
        r.setLiberadoEm(agora);
        r.setRetificacaoDe(adendoDe);
        r.setMotivoRetificacao(motivo);
        return resultadoRepository.save(r);
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
            // A faixa do sexo e da idade do paciente na data da coleta; senão, a faixa geral do catálogo (ADR-0180).
            Paciente paciente = item.getPedido().getPaciente();
            LocalDate dataColeta = item.getAmostra() != null && item.getAmostra().getColetadaEm() != null
                    ? item.getAmostra().getColetadaEm().toLocalDate() : LocalDate.now();
            Optional<FaixaReferenciaExame> faixa = exameService.faixaPara(exame, paciente.getSexo(), paciente.getDataNascimento(), dataColeta);
            if (faixa.isPresent()) {
                FaixaReferenciaExame f = faixa.get();
                r.setReferenciaMinima(f.getReferenciaMinima());
                r.setReferenciaMaxima(f.getReferenciaMaxima());
                r.setCriticoMinimo(f.getCriticoMinimo());
                r.setCriticoMaximo(f.getCriticoMaximo());
                r.setFaixaDescricao(FaixaReferenciaExameDto.rotulo(f));
            } else {
                r.setReferenciaMinima(exame.getReferenciaMinima());
                r.setReferenciaMaxima(exame.getReferenciaMaxima());
                r.setCriticoMinimo(exame.getCriticoMinimo());
                r.setCriticoMaximo(exame.getCriticoMaximo());
                boolean temLimite = Stream.of(exame.getReferenciaMinima(), exame.getReferenciaMaxima(), exame.getCriticoMinimo(),
                        exame.getCriticoMaximo()).anyMatch(java.util.Objects::nonNull);
                r.setFaixaDescricao(temLimite ? ExameLaboratorialService.FAIXA_GERAL : null);
            }
            r.setInterpretacao(interpretar(valorNumerico, r.getReferenciaMinima(), r.getReferenciaMaxima(), r.getCriticoMinimo(),
                    r.getCriticoMaximo()));
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
        return interpretar(valor, minima, maxima, null, null);
    }

    /** Crítico vence o fora da faixa: abaixo do crítico mínimo é crítico baixo; acima do crítico máximo, crítico alto (ADR-0180). */
    static InterpretacaoResultado interpretar(BigDecimal valor, BigDecimal minima, BigDecimal maxima, BigDecimal criticoMinimo,
                                              BigDecimal criticoMaximo) {
        if (criticoMinimo != null && valor.compareTo(criticoMinimo) < 0) {
            return InterpretacaoResultado.CRITICO_BAIXO;
        }
        if (criticoMaximo != null && valor.compareTo(criticoMaximo) > 0) {
            return InterpretacaoResultado.CRITICO_ALTO;
        }
        if (minima == null && maxima == null) {
            return criticoMinimo != null || criticoMaximo != null ? InterpretacaoResultado.NORMAL : null;
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
        Stream<UnidadeDeSaude> daImagem = Stream.ofNullable(p.getUnidadeExecutanteImagem());
        return Stream.concat(Stream.concat(Stream.of(p.getUnidadeSolicitante()), daAmostra), daImagem);
    }

    private PedidoExameResponseDto detalhe(PedidoExame pedido) {
        ContextoAuditoria.paciente(pedido.getPaciente().getUuid());
        ContextoAuditoria.registro(pedido.getUuid());
        return PedidoExameResponseDto.fromPedido(pedido, itemRepository.findByPedido_Uuid(pedido.getUuid()),
                amostraRepository.findByPedido_UuidOrderByColetadaEmAsc(pedido.getUuid()),
                eventoRepository.findByPedido_UuidOrderByOcorridoEmAsc(pedido.getUuid()),
                comunicacaoRepository.findByItem_Pedido_UuidOrderByComunicadoEmAsc(pedido.getUuid()),
                entregaRepository.findByPedido_UuidOrderByEntregueEmAsc(pedido.getUuid()),
                realizacaoRepository.findByItem_Pedido_UuidOrderByRealizadaEmAsc(pedido.getUuid()));
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
