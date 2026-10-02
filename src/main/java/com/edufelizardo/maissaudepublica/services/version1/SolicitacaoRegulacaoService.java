package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.EventoRegulacao;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AgendamentoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AutorizacaoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ComplementoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RealizacaoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReclassificacaoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SolicitacaoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SolicitacaoRegulacaoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SolicitacaoRegulacaoResumoDto;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Regulação do acesso (ADR-0087): a unidade de origem solicita, a Central regula — autoriza com a vaga,
 * devolve para complementar ou nega — e qualquer um dos dois lados cancela. Cada passo grava um
 * {@link EventoRegulacao}.
 *
 * <p>O ciclo fecha na unidade executante (ADR-0089): a solicitação autorizada vira um {@link Agendamento} com
 * o profissional que vai atender, e o desfecho é registrado lá — realizado, com a contrarreferência para a
 * origem, ou falta.
 *
 * <p>Escopo: quem solicita responde pela unidade solicitante ({@value #SOLICITAR}); quem regula precisa de
 * {@value #REGULAR} num escopo que cubra a unidade solicitante — o regulador lotado no município enxerga as
 * unidades dele. O regulador não decide a própria solicitação.
 */
@Service
public class SolicitacaoRegulacaoService {

    public static final String CONSULTAR = "REGULACAO.CONSULTAR";
    public static final String SOLICITAR = "REGULACAO.SOLICITAR";
    public static final String REGULAR = "REGULACAO.REGULAR";

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

    /** Fila: prioridade (vermelho primeiro) e, dentro dela, quem pediu antes. */
    static final Comparator<SolicitacaoRegulacao> ORDEM_DA_FILA = Comparator
            .comparing(SolicitacaoRegulacao::getPrioridade)
            .thenComparing(SolicitacaoRegulacao::getSolicitadoEm);

    @Autowired
    private SolicitacaoRegulacaoRepository repository;

    @Autowired
    private EventoRegulacaoRepository eventoRepository;

    @Autowired
    private ProcedimentoReguladoService procedimentoService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Solicitante ────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public SolicitacaoRegulacaoResponseDto solicitar(SolicitacaoRegulacaoRequestDto dto) {
        UnidadeDeSaude unidade = buscarUnidade(dto.getUnidadeSolicitanteId());
        controleDeAcesso.exigir(SOLICITAR, unidade);
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um paciente com o id " + dto.getPacienteId() + " em nossos registros."));
        ContextoAuditoria.paciente(paciente.getUuid());
        if (!paciente.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O paciente está inativo; reative o cadastro antes de solicitar.");
        }
        ProcedimentoRegulado procedimento = procedimentoService.buscar(dto.getProcedimentoId());
        if (!procedimento.isAtivo()) {
            throw new ResourceUnprocessableEntityException(
                    "O procedimento " + procedimento.getNome() + " está fora de uso no catálogo da regulação.");
        }
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        if (repository.existsByPaciente_UuidAndProcedimento_UuidAndStatusIn(paciente.getUuid(), procedimento.getUuid(),
                StatusSolicitacaoRegulacao.EM_ABERTO)) {
            throw new ResourceConflictException("O paciente já tem uma solicitação de " + procedimento.getNome()
                    + " em andamento; acompanhe ou cancele a existente.");
        }

        SolicitacaoRegulacao solicitacao = repository.save(new SolicitacaoRegulacao(paciente, procedimento, unidade,
                profissional, normalizarCid(dto.getCid()), dto.getJustificativa().trim(), dto.getPrioridade(),
                Instant.now(), UsuarioAutenticado.cpf()));
        registrar(solicitacao, TipoEventoRegulacao.SOLICITACAO, null, profissional);
        return detalhe(solicitacao);
    }

    /** Devolvida → volta à fila, na posição original (a hora do pedido não muda). */
    @Transactional
    public SolicitacaoRegulacaoResponseDto complementar(UUID uuid, ComplementoRegulacaoRequestDto dto) {
        SolicitacaoRegulacao solicitacao = travar(uuid);
        controleDeAcesso.exigir(SOLICITAR, solicitacao.getUnidadeSolicitante());
        exigirStatus(solicitacao, "complementar", StatusSolicitacaoRegulacao.DEVOLVIDA);
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        solicitacao.setStatus(StatusSolicitacaoRegulacao.SOLICITADA);
        registrar(solicitacao, TipoEventoRegulacao.COMPLEMENTO, dto.getComplemento().trim(), profissional);
        return detalhe(solicitacao);
    }

    /** Pelo solicitante ou pela regulação, antes da realização. */
    @Transactional
    public SolicitacaoRegulacaoResponseDto cancelar(UUID uuid, MotivoRegulacaoRequestDto dto) {
        SolicitacaoRegulacao solicitacao = travar(uuid);
        try {
            controleDeAcesso.exigir(SOLICITAR, solicitacao.getUnidadeSolicitante());
        } catch (ResourceForbiddenException semSolicitar) {
            controleDeAcesso.exigir(REGULAR, solicitacao.getUnidadeSolicitante());
        }
        exigirStatus(solicitacao, "cancelar", StatusSolicitacaoRegulacao.CANCELAVEIS.toArray(StatusSolicitacaoRegulacao[]::new));
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        if (solicitacao.getAgendamento() != null) {
            fecharAgendamento(solicitacao.getAgendamento(), StatusAgendamento.CANCELADO, "Regulação cancelada: " + dto.getMotivo().trim());
        }
        solicitacao.setStatus(StatusSolicitacaoRegulacao.CANCELADA);
        registrar(solicitacao, TipoEventoRegulacao.CANCELAMENTO, dto.getMotivo().trim(), profissional);
        return detalhe(solicitacao);
    }

    // ── Regulador ──────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public SolicitacaoRegulacaoResponseDto reclassificar(UUID uuid, ReclassificacaoRegulacaoRequestDto dto) {
        SolicitacaoRegulacao solicitacao = travarParaRegular(uuid, "reclassificar");
        Profissional regulador = buscarProfissional(dto.getProfissionalMatricula());
        PrioridadeRegulacao anterior = solicitacao.getPrioridade();
        if (anterior == dto.getPrioridade()) {
            throw new ResourceUnprocessableEntityException("A solicitação já está com a prioridade " + anterior + ".");
        }
        solicitacao.setPrioridade(dto.getPrioridade());
        registrar(solicitacao, TipoEventoRegulacao.RECLASSIFICACAO,
                "De " + anterior + " para " + dto.getPrioridade() + ": " + dto.getMotivo().trim(), regulador);
        return detalhe(solicitacao);
    }

    @Transactional
    public SolicitacaoRegulacaoResponseDto autorizar(UUID uuid, AutorizacaoRegulacaoRequestDto dto) {
        SolicitacaoRegulacao solicitacao = travarParaRegular(uuid, "autorizar");
        Profissional regulador = buscarRegulador(dto.getProfissionalMatricula(), solicitacao);
        UnidadeDeSaude executante = buscarUnidade(dto.getUnidadeExecutanteId());
        if (!executante.isAtivo()) {
            throw new ResourceUnprocessableEntityException("A unidade " + executante.getNome() + " está inativa.");
        }
        if (dto.getDataHoraPrevista().isBefore(LocalDateTime.now().withSecond(0).withNano(0))) {
            throw new ResourceUnprocessableEntityException("A data e a hora da vaga não podem estar no passado.");
        }
        solicitacao.setUnidadeExecutante(executante);
        solicitacao.setDataHoraPrevista(dto.getDataHoraPrevista());
        solicitacao.setStatus(StatusSolicitacaoRegulacao.AUTORIZADA);
        String vaga = executante.getNome() + ", " + DATA_HORA.format(dto.getDataHoraPrevista());
        String observacao = textoOuNulo(dto.getObservacao());
        Profissional profissionalExecutante = textoOuNulo(dto.getProfissionalExecutanteMatricula()) == null ? null
                : buscarProfissional(dto.getProfissionalExecutanteMatricula().trim());
        registrar(solicitacao, TipoEventoRegulacao.AUTORIZACAO, observacao == null ? vaga : vaga + ". " + observacao, regulador);
        if (profissionalExecutante != null) {
            agendarNaExecutante(solicitacao, profissionalExecutante, dto.getDataHoraPrevista(), regulador);
        }
        return detalhe(solicitacao);
    }

    @Transactional
    public SolicitacaoRegulacaoResponseDto devolver(UUID uuid, MotivoRegulacaoRequestDto dto) {
        SolicitacaoRegulacao solicitacao = travarParaRegular(uuid, "devolver");
        Profissional regulador = buscarProfissional(dto.getProfissionalMatricula());
        solicitacao.setStatus(StatusSolicitacaoRegulacao.DEVOLVIDA);
        registrar(solicitacao, TipoEventoRegulacao.DEVOLUCAO, dto.getMotivo().trim(), regulador);
        return detalhe(solicitacao);
    }

    @Transactional
    public SolicitacaoRegulacaoResponseDto negar(UUID uuid, MotivoRegulacaoRequestDto dto) {
        SolicitacaoRegulacao solicitacao = travarParaRegular(uuid, "negar");
        Profissional regulador = buscarRegulador(dto.getProfissionalMatricula(), solicitacao);
        solicitacao.setStatus(StatusSolicitacaoRegulacao.NEGADA);
        registrar(solicitacao, TipoEventoRegulacao.NEGATIVA, dto.getMotivo().trim(), regulador);
        return detalhe(solicitacao);
    }

    // ── Unidade executante (ADR-0089) ──────────────────────────────────────────────────────────────

    /**
     * Agenda a solicitação autorizada com o profissional que vai atender. Quem agenda é a recepção da
     * executante ({@code AGENDAMENTO.GERENCIAR} lá) ou a própria regulação.
     */
    @Transactional
    public SolicitacaoRegulacaoResponseDto agendar(UUID uuid, AgendamentoRegulacaoRequestDto dto) {
        SolicitacaoRegulacao solicitacao = travar(uuid);
        try {
            controleDeAcesso.exigir("AGENDAMENTO.GERENCIAR", solicitacao.getUnidadeExecutante());
        } catch (ResourceForbiddenException semAgenda) {
            controleDeAcesso.exigir(REGULAR, solicitacao.getUnidadeSolicitante());
        }
        exigirStatus(solicitacao, "agendar", StatusSolicitacaoRegulacao.AUTORIZADA);
        Profissional quem = buscarProfissional(dto.getProfissionalMatricula());
        Profissional executante = buscarProfissional(dto.getProfissionalExecutanteMatricula());
        LocalDateTime quando = dto.getDataHora() != null ? dto.getDataHora() : solicitacao.getDataHoraPrevista();
        if (quando.isBefore(LocalDateTime.now().withSecond(0).withNano(0))) {
            throw new ResourceUnprocessableEntityException("A data e a hora do agendamento não podem estar no passado.");
        }
        agendarNaExecutante(solicitacao, executante, quando, quem);
        return detalhe(solicitacao);
    }

    /** Atendido na executante: a contrarreferência volta para quem solicitou. Só quem registra o atendimento clínico. */
    @Transactional
    public SolicitacaoRegulacaoResponseDto registrarRealizacao(UUID uuid, RealizacaoRegulacaoRequestDto dto) {
        SolicitacaoRegulacao solicitacao = travar(uuid);
        try {
            controleDeAcesso.exigir("CONSULTA.REGISTRAR", solicitacao.getUnidadeExecutante());
        } catch (ResourceForbiddenException semConsulta) {
            controleDeAcesso.exigir("PROCEDIMENTO.REGISTRAR", solicitacao.getUnidadeExecutante());
        }
        exigirStatus(solicitacao, "registrar a realização de", StatusSolicitacaoRegulacao.AGENDADA);
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        String contrarreferencia = dto.getContrarreferencia().trim();
        fecharAgendamento(solicitacao.getAgendamento(), StatusAgendamento.REALIZADO, null);
        solicitacao.setContrarreferencia(contrarreferencia);
        solicitacao.setConcluidoEm(Instant.now());
        solicitacao.setStatus(StatusSolicitacaoRegulacao.REALIZADA);
        // O texto clínico fica na solicitação (leitura auditada); o evento diz só que houve retorno.
        registrar(solicitacao, TipoEventoRegulacao.REALIZACAO, "Contrarreferência registrada para " + solicitacao.getUnidadeSolicitante().getNome(), profissional);
        return detalhe(solicitacao);
    }

    /** O paciente não compareceu ao agendamento na executante. */
    @Transactional
    public SolicitacaoRegulacaoResponseDto registrarFalta(UUID uuid, MotivoRegulacaoRequestDto dto) {
        SolicitacaoRegulacao solicitacao = travar(uuid);
        try {
            controleDeAcesso.exigir("AGENDAMENTO.GERENCIAR", solicitacao.getUnidadeExecutante());
        } catch (ResourceForbiddenException semAgenda) {
            controleDeAcesso.exigir("ATENDIMENTO.GERENCIAR", solicitacao.getUnidadeExecutante());
        }
        exigirStatus(solicitacao, "registrar a falta em", StatusSolicitacaoRegulacao.AGENDADA);
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        fecharAgendamento(solicitacao.getAgendamento(), StatusAgendamento.CANCELADO, "Paciente faltou: " + dto.getMotivo().trim());
        solicitacao.setConcluidoEm(Instant.now());
        solicitacao.setStatus(StatusSolicitacaoRegulacao.FALTOU);
        registrar(solicitacao, TipoEventoRegulacao.FALTA, dto.getMotivo().trim(), profissional);
        return detalhe(solicitacao);
    }

    private void agendarNaExecutante(SolicitacaoRegulacao solicitacao, Profissional executante, LocalDateTime quando,
                                     Profissional quemRegistra) {
        TipoAgendamento tipo = solicitacao.getProcedimento().getTipo() == TipoProcedimentoRegulado.CONSULTA_ESPECIALIZADA
                ? TipoAgendamento.CONSULTA : TipoAgendamento.PROCEDIMENTO;
        Agendamento agendamento = agendamentoRepository.save(new Agendamento(solicitacao.getPaciente(), executante, quando,
                StatusAgendamento.AGENDADO, tipo, limitarObservacao("Regulação: " + solicitacao.getProcedimento().getNome()
                + " (solicitação " + solicitacao.getUuid() + ")")));
        solicitacao.setAgendamento(agendamento);
        solicitacao.setDataHoraPrevista(quando);
        solicitacao.setStatus(StatusSolicitacaoRegulacao.AGENDADA);
        registrar(solicitacao, TipoEventoRegulacao.AGENDAMENTO,
                "Com " + executante.getNome() + " em " + DATA_HORA.format(quando), quemRegistra);
    }

    /** A observação do agendamento é uma coluna de 255 caracteres. */
    private static String limitarObservacao(String texto) {
        return texto.length() > 255 ? texto.substring(0, 255) : texto;
    }

    private void fecharAgendamento(Agendamento agendamento, StatusAgendamento status, String observacao) {
        agendamento.setStatus(status);
        if (observacao != null) {
            String atual = agendamento.getObservacao();
            String texto = atual == null || atual.isBlank() ? observacao : atual + " | " + observacao;
            agendamento.setObservacao(limitarObservacao(texto));
        }
        agendamentoRepository.save(agendamento);
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** A fila do regulador: só SOLICITADA, das unidades no escopo de quem regula, na ordem da fila. */
    @Transactional(readOnly = true)
    public List<SolicitacaoRegulacaoResumoDto> fila(UUID procedimentoId, PrioridadeRegulacao prioridade) {
        List<SolicitacaoRegulacao> naFila = repository.findByStatus(StatusSolicitacaoRegulacao.SOLICITADA);
        Map<UUID, Integer> posicoes = posicoes(naFila);
        return controleDeAcesso.filtrar(naFila.stream(), s -> Stream.of(s.getUnidadeSolicitante()), REGULAR)
                .filter(s -> procedimentoId == null || s.getProcedimento().getUuid().equals(procedimentoId))
                .filter(s -> prioridade == null || s.getPrioridade() == prioridade)
                .sorted(ORDEM_DA_FILA)
                .map(s -> SolicitacaoRegulacaoResumoDto.fromSolicitacao(s, posicoes.get(s.getUuid())))
                .toList();
    }

    /** Da mais recente para a mais antiga, sem dado clínico, das unidades (solicitante ou executante) no escopo. */
    @Transactional(readOnly = true)
    public List<SolicitacaoRegulacaoResumoDto> listar(StatusSolicitacaoRegulacao status, UUID pacienteId,
                                                      UUID unidadeSolicitanteId, UUID procedimentoId) {
        Map<UUID, Integer> posicoes = posicoes(repository.findByStatus(StatusSolicitacaoRegulacao.SOLICITADA));
        Stream<SolicitacaoRegulacao> base = pacienteId != null
                ? repository.findByPaciente_Uuid(pacienteId).stream().sorted(Comparator.comparing(SolicitacaoRegulacao::getSolicitadoEm).reversed())
                : repository.findAllByOrderBySolicitadoEmDesc().stream();
        return controleDeAcesso.filtrar(base, s -> Stream.of(s.getUnidadeSolicitante(), s.getUnidadeExecutante()),
                        CONSULTAR, SOLICITAR, REGULAR)
                .filter(s -> status == null || s.getStatus() == status)
                .filter(s -> unidadeSolicitanteId == null || s.getUnidadeSolicitante().getUuid().equals(unidadeSolicitanteId))
                .filter(s -> procedimentoId == null || s.getProcedimento().getUuid().equals(procedimentoId))
                .map(s -> SolicitacaoRegulacaoResumoDto.fromSolicitacao(s, posicoes.get(s.getUuid())))
                .toList();
    }

    /** O detalhe clínico: só quem solicita ou regula, no escopo de uma das unidades. */
    @Transactional(readOnly = true)
    public SolicitacaoRegulacaoResponseDto buscarPorId(UUID uuid) {
        SolicitacaoRegulacao solicitacao = repository.findById(uuid).orElseThrow(() -> naoEncontrada(uuid));
        ContextoAuditoria.unidade(solicitacao.getUnidadeSolicitante());
        controleDeAcesso.filtrar(Stream.of(solicitacao), s -> Stream.of(s.getUnidadeSolicitante(), s.getUnidadeExecutante()),
                        SOLICITAR, REGULAR)
                .findAny()
                .orElseThrow(() -> new ResourceForbiddenException("Esta solicitação é de unidades fora do seu acesso."));
        return detalhe(solicitacao);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** Posição de cada solicitação na fila do seu procedimento, contando a rede inteira (a fila é uma só). */
    static Map<UUID, Integer> posicoes(List<SolicitacaoRegulacao> naFila) {
        Map<UUID, Integer> posicoes = new HashMap<>();
        naFila.stream()
                .collect(Collectors.groupingBy(s -> s.getProcedimento().getUuid()))
                .values()
                .forEach(fila -> {
                    List<SolicitacaoRegulacao> ordenada = fila.stream().sorted(ORDEM_DA_FILA).toList();
                    for (int i = 0; i < ordenada.size(); i++) {
                        posicoes.put(ordenada.get(i).getUuid(), i + 1);
                    }
                });
        return posicoes;
    }

    private SolicitacaoRegulacaoResponseDto detalhe(SolicitacaoRegulacao solicitacao) {
        ContextoAuditoria.paciente(solicitacao.getPaciente().getUuid());
        ContextoAuditoria.registro(solicitacao.getUuid());
        Integer posicao = solicitacao.getStatus() == StatusSolicitacaoRegulacao.SOLICITADA
                ? posicoes(repository.findByStatus(StatusSolicitacaoRegulacao.SOLICITADA)).get(solicitacao.getUuid())
                : null;
        return SolicitacaoRegulacaoResponseDto.fromSolicitacao(solicitacao, posicao,
                eventoRepository.findBySolicitacao_UuidOrderByOcorridoEmAsc(solicitacao.getUuid()));
    }

    private void registrar(SolicitacaoRegulacao solicitacao, TipoEventoRegulacao tipo, String texto, Profissional profissional) {
        repository.save(solicitacao);
        eventoRepository.save(new EventoRegulacao(solicitacao, tipo, texto, profissional, Instant.now(), UsuarioAutenticado.cpf()));
    }

    private SolicitacaoRegulacao travar(UUID uuid) {
        SolicitacaoRegulacao solicitacao = repository.findByIdParaAtualizar(uuid).orElseThrow(() -> naoEncontrada(uuid));
        ContextoAuditoria.paciente(solicitacao.getPaciente().getUuid());
        return solicitacao;
    }

    private SolicitacaoRegulacao travarParaRegular(UUID uuid, String acao) {
        SolicitacaoRegulacao solicitacao = travar(uuid);
        controleDeAcesso.exigir(REGULAR, solicitacao.getUnidadeSolicitante());
        exigirStatus(solicitacao, acao, StatusSolicitacaoRegulacao.SOLICITADA);
        return solicitacao;
    }

    private static void exigirStatus(SolicitacaoRegulacao solicitacao, String acao, StatusSolicitacaoRegulacao... permitidos) {
        if (Stream.of(permitidos).noneMatch(s -> s == solicitacao.getStatus())) {
            throw new ResourceUnprocessableEntityException("Não é possível " + acao + " uma solicitação "
                    + solicitacao.getStatus() + ".");
        }
    }

    /** Quem decide (autoriza ou nega) não pode ser quem pediu. */
    private Profissional buscarRegulador(String matricula, SolicitacaoRegulacao solicitacao) {
        Profissional regulador = buscarProfissional(matricula);
        if (regulador.getUuid().equals(solicitacao.getProfissionalSolicitante().getUuid())) {
            throw new ResourceUnprocessableEntityException(
                    "Quem solicitou não regula a própria solicitação; ela precisa ser decidida por outro regulador.");
        }
        return regulador;
    }

    private Profissional buscarProfissional(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    private UnidadeDeSaude buscarUnidade(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }

    private ResourceNotFoundException naoEncontrada(UUID uuid) {
        return new ResourceNotFoundException(
                "Não foi possível encontrar uma solicitação de regulação com o id " + uuid + " em nossos registros.");
    }

    /** "e11.9 " → "E119": maiúsculas, sem ponto, como o CID-10 é guardado. */
    static String normalizarCid(String cid) {
        return cid.trim().toUpperCase(Locale.ROOT).replace(".", "");
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
