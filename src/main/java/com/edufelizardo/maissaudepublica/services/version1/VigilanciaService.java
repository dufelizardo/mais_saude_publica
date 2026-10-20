package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Agravo;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.ClassificacaoCaso;
import com.edufelizardo.maissaudepublica.models.EventoCrise;
import com.edufelizardo.maissaudepublica.models.FichaAgravo;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.InvestigacaoEpidemiologica;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.NotificacaoNegativa;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.PedidoExame;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AgravoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CampoFichaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ClassificacaoCasoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoNotificacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FichaAgravoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InvestigacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NotificacaoNegativaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NotificacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AgravoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.BoletimVigilanciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NotificacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NotificacaoNegativaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NotificacoesDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SemanasEpidemiologicasDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SugestaoNotificacaoDto;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCampoFicha;
import com.edufelizardo.maissaudepublica.repositories.AgravoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ClassificacaoCasoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoCriseRepository;
import com.edufelizardo.maissaudepublica.repositories.FichaAgravoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.InvestigacaoEpidemiologicaRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoNegativaRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Vigilância epidemiológica, fatia V1 (ADR-0124, desenho na ADR-0110): catálogo de agravos com ficha versionada,
 * notificação, investigação, classificação com histórico, encerramento, notificação negativa semanal e a sugestão de
 * notificação a partir da rede. A rede detecta, mas não notifica sozinha: notificar é ato profissional.
 */
@Service
public class VigilanciaService {

    public static final String NOTIFICAR = "VIGILANCIA.NOTIFICAR";
    public static final String INVESTIGAR = "VIGILANCIA.INVESTIGAR";
    public static final String GERENCIAR = "VIGILANCIA.GERENCIAR";
    /** O agravo de violência; a crise de autoagressão da SM3 sugere esta notificação (ADR-0123). */
    public static final String AGRAVO_VIOLENCIA = "VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA";
    /** Janela da sugestão de notificação. */
    static final Duration JANELA_SUGESTAO = Duration.ofDays(30);

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<ClassificacaoCasoEpidemiologico> FINAIS = Set.of(ClassificacaoCasoEpidemiologico.CONFIRMADO,
            ClassificacaoCasoEpidemiologico.DESCARTADO, ClassificacaoCasoEpidemiologico.INCONCLUSIVO);

    @Autowired
    private AgravoRepository agravoRepository;

    @Autowired
    private FichaAgravoRepository fichaRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private InvestigacaoEpidemiologicaRepository investigacaoRepository;

    @Autowired
    private ClassificacaoCasoRepository classificacaoRepository;

    @Autowired
    private NotificacaoNegativaRepository negativaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private PedidoExameRepository pedidoExameRepository;

    @Autowired
    private EventoCriseRepository criseRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private AutorizacaoService autorizacaoService;

    // ── Catálogo de agravos ────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AgravoDto> agravos() {
        return agravoRepository.findAllByOrderByNomeAsc().stream().map(this::agravoDto).toList();
    }

    @Transactional(readOnly = true)
    public AgravoDto agravo(UUID uuid) {
        return agravoDto(buscarAgravo(uuid));
    }

    @Transactional
    public AgravoDto criarAgravo(AgravoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        if (agravoRepository.existsByCodigo(dto.getCodigo())) {
            throw new ResourceConflictException("Já existe um agravo com o código " + dto.getCodigo() + ".");
        }
        Agravo a = new Agravo();
        a.setCodigo(dto.getCodigo());
        preencher(a, dto);
        a = agravoRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        return agravoDto(a);
    }

    @Transactional
    public AgravoDto atualizarAgravo(UUID uuid, AgravoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        Agravo a = buscarAgravo(uuid);
        if (!a.getCodigo().equals(dto.getCodigo())) {
            throw new ResourceUnprocessableEntityException("O código do agravo não muda (" + a.getCodigo() + ").");
        }
        preencher(a, dto);
        agravoRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        return agravoDto(a);
    }

    /** Grava a versão seguinte da ficha; as notificações antigas continuam na versão delas. */
    @Transactional
    public AgravoDto novaFicha(UUID agravoId, FichaAgravoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        Agravo a = buscarAgravo(agravoId);
        Set<String> chaves = new HashSet<>();
        for (CampoFichaDto c : dto.getCampos()) {
            if (!chaves.add(c.getChave())) {
                throw new ResourceBadRequestException("A chave " + c.getChave() + " aparece mais de uma vez na ficha.");
            }
            boolean comOpcoes = c.getTipo() == TipoCampoFicha.OPCAO || c.getTipo() == TipoCampoFicha.MULTIPLA;
            if (comOpcoes && (c.getOpcoes() == null || c.getOpcoes().isEmpty())) {
                throw new ResourceBadRequestException("O campo " + c.getChave() + " precisa das opções.");
            }
        }
        int versao = fichaRepository.findFirstByAgravo_UuidOrderByVersaoDesc(a.getUuid()).map(f -> f.getVersao() + 1).orElse(1);
        FichaAgravo f = new FichaAgravo();
        f.setAgravo(a);
        f.setVersao(versao);
        f.setDefinicao(json(Map.of("campos", dto.getCampos())));
        f.setCriadaEm(Instant.now());
        f.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        fichaRepository.save(f);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Ficha versão " + versao + ".");
        return agravoDto(a);
    }

    // ── Notificação ────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public NotificacaoDto notificar(NotificacaoRequestDto dto) {
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(NOTIFICAR, unidade);
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + dto.getPacienteId() + " em nossos registros."));
        ContextoAuditoria.paciente(paciente.getUuid());
        Agravo agravo = buscarAgravo(dto.getAgravoId());
        if (!agravo.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O agravo " + agravo.getNome() + " está inativo.");
        }
        LocalDate hoje = LocalDate.now();
        LocalDate data = dto.getDataNotificacao() != null ? dto.getDataNotificacao() : hoje;
        if (data.isAfter(hoje)) {
            throw new ResourceBadRequestException("A notificação não pode ser no futuro.");
        }
        if (dto.getDataPrimeirosSintomas() != null && dto.getDataPrimeirosSintomas().isAfter(data)) {
            throw new ResourceBadRequestException("Os primeiros sintomas não podem ser depois da notificação.");
        }
        Notificacao n = new Notificacao();
        n.setPaciente(paciente);
        n.setAgravo(agravo);
        n.setUnidade(unidade);
        n.setProfissional(profissional(dto.getProfissionalMatricula()));
        n.setDataPrimeirosSintomas(dto.getDataPrimeirosSintomas());
        n.setDataNotificacao(data);
        n.setOrigem(dto.getOrigem());
        n.setAtendimento(referencia(dto.getAtendimentoId(), atendimentoRepository::findById, Atendimento::getPaciente, paciente, "atendimento"));
        n.setInternacao(referencia(dto.getInternacaoId(), internacaoRepository::findById, Internacao::getPaciente, paciente, "internação"));
        n.setPedidoExame(referencia(dto.getPedidoExameId(), pedidoExameRepository::findById, PedidoExame::getPaciente, paciente, "pedido de exame"));
        EventoCrise crise = referencia(dto.getCriseId(), criseRepository::findById, EventoCrise::getPaciente, paciente, "crise");
        n.setCrise(crise);
        n.setLocalProvavel(texto(dto.getLocalProvavel()));
        Optional<FichaAgravo> ficha = fichaRepository.findFirstByAgravo_UuidOrderByVersaoDesc(agravo.getUuid());
        Map<String, Object> respostas = dto.getRespostas() != null ? dto.getRespostas() : Map.of();
        if (ficha.isPresent()) {
            validarRespostas(campos(ficha.get()), respostas);
            n.setFichaVersao(ficha.get().getVersao());
            n.setRespostas(json(respostas));
        } else if (!respostas.isEmpty()) {
            throw new ResourceBadRequestException("O agravo " + agravo.getNome() + " não tem ficha específica; envie a notificação sem respostas.");
        }
        n.setSituacao(SituacaoNotificacao.NOTIFICADA);
        n.setPrazoEncerramento(data.plusDays(agravo.getPrazoEncerramentoDias()));
        n.setRegistradaEm(Instant.now());
        n.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        String aviso = notificacaoRepository.findByPaciente_Uuid(paciente.getUuid()).stream()
                .anyMatch(x -> x.getAgravo().getUuid().equals(agravo.getUuid()) && x.getSituacao() != SituacaoNotificacao.ENCERRADA)
                ? "O paciente já tem outra notificação de " + agravo.getNome() + " em aberto; confira se não é o mesmo caso." : null;
        n = notificacaoRepository.save(n);
        // A crise da SM3 fica notificada: a pendência some (ADR-0123).
        if (crise != null && crise.getNotificadaEm() == null) {
            crise.setNotificadaEm(data);
            crise.setNotificacaoNumero(n.getUuid().toString().substring(0, 8).toUpperCase(Locale.ROOT));
            crise.setNotificacaoRegistradaPorCpf(UsuarioAutenticado.cpf());
            criseRepository.save(crise);
        }
        ContextoAuditoria.registro(n.getUuid());
        ContextoAuditoria.detalhe("Notificação de " + agravo.getCodigo() + " (" + dto.getOrigem() + ").");
        NotificacaoDto d = detalhe(n, true);
        d.setAviso(aviso);
        return d;
    }

    /** Abre ou atualiza a investigação; a notificação passa a em investigação. */
    @Transactional
    public NotificacaoDto investigar(UUID notificacaoId, InvestigacaoRequestDto dto) {
        Notificacao n = paraInvestigar(notificacaoId);
        InvestigacaoEpidemiologica i = investigacaoRepository.findByNotificacao_Uuid(n.getUuid()).orElseGet(() -> {
            InvestigacaoEpidemiologica nova = new InvestigacaoEpidemiologica();
            nova.setNotificacao(n);
            nova.setIniciadaEm(Instant.now());
            return nova;
        });
        i.setInvestigador(profissional(dto.getInvestigadorMatricula()));
        i.setExposicao(texto(dto.getExposicao()));
        i.setLocalProvavelInfeccao(texto(dto.getLocalProvavelInfeccao()));
        i.setMedidas(texto(dto.getMedidas()));
        Set<UUID> exames = new HashSet<>();
        for (UUID e : dto.getExamesIds() == null ? List.<UUID>of() : dto.getExamesIds()) {
            PedidoExame p = pedidoExameRepository.findById(e).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o pedido de exame " + e + " em nossos registros."));
            if (!p.getPaciente().getUuid().equals(n.getPaciente().getUuid())) {
                throw new ResourceUnprocessableEntityException("O pedido de exame " + e + " é de outro paciente.");
            }
            exames.add(e);
        }
        i.setExamesIds(exames);
        i.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        investigacaoRepository.save(i);
        if (n.getSituacao() == SituacaoNotificacao.NOTIFICADA) {
            n.setSituacao(SituacaoNotificacao.EM_INVESTIGACAO);
            notificacaoRepository.save(n);
        }
        ContextoAuditoria.detalhe("Investigação.");
        return detalhe(n, true);
    }

    /** Classifica o caso (histórico). Confirmado e descartado pedem o critério, que precisa ser um dos aceitos pelo agravo. */
    @Transactional
    public NotificacaoDto classificar(UUID notificacaoId, ClassificacaoCasoRequestDto dto) {
        Notificacao n = paraInvestigar(notificacaoId);
        boolean pedeCriterio = dto.getClassificacao() == ClassificacaoCasoEpidemiologico.CONFIRMADO
                || dto.getClassificacao() == ClassificacaoCasoEpidemiologico.DESCARTADO;
        if (pedeCriterio && dto.getCriterio() == null) {
            throw new ResourceBadRequestException("Confirmado e descartado pedem o critério de classificação.");
        }
        if (dto.getCriterio() != null && !n.getAgravo().getCriterios().contains(dto.getCriterio())) {
            throw new ResourceUnprocessableEntityException("O agravo " + n.getAgravo().getNome() + " não aceita o critério " + dto.getCriterio() + ".");
        }
        ClassificacaoCaso c = new ClassificacaoCaso();
        c.setNotificacao(n);
        c.setClassificacao(dto.getClassificacao());
        c.setCriterio(dto.getCriterio());
        c.setJustificativa(texto(dto.getJustificativa()));
        c.setClassificadaEm(Instant.now());
        c.setResponsavel(profissional(null));
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        classificacaoRepository.save(c);
        if (n.getSituacao() == SituacaoNotificacao.NOTIFICADA) {
            n.setSituacao(SituacaoNotificacao.EM_INVESTIGACAO);
            notificacaoRepository.save(n);
        }
        ContextoAuditoria.detalhe("Classificação: " + dto.getClassificacao() + ".");
        return detalhe(n, true);
    }

    /** Encerra com a classificação final (a última) e a evolução; depois, a notificação não muda. */
    @Transactional
    public NotificacaoDto encerrar(UUID notificacaoId, EncerramentoNotificacaoRequestDto dto) {
        Notificacao n = paraInvestigar(notificacaoId);
        List<ClassificacaoCaso> historico = classificacaoRepository.findDaNotificacao(n.getUuid());
        ClassificacaoCasoEpidemiologico atual = historico.isEmpty() ? null : historico.get(0).getClassificacao();
        if (atual == null || !FINAIS.contains(atual)) {
            throw new ResourceUnprocessableEntityException("Para encerrar, classifique o caso como confirmado, descartado ou inconclusivo.");
        }
        n.setClassificacaoFinal(atual);
        n.setEvolucao(dto.getEvolucao());
        n.setEncerradaEm(Instant.now());
        n.setSituacao(SituacaoNotificacao.ENCERRADA);
        notificacaoRepository.save(n);
        ContextoAuditoria.detalhe("Encerrada: " + atual + ", " + dto.getEvolucao() + ".");
        return detalhe(n, true);
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** As notificações do escopo; as de agravo sensível só para quem investiga ou notificou. */
    @Transactional(readOnly = true)
    public NotificacoesDto notificacoes(UUID unidadeId, UUID agravoId, SituacaoNotificacao situacao, boolean atrasadas) {
        LocalDate hoje = LocalDate.now();
        List<Notificacao> visiveis = controleDeAcesso.filtrar(notificacaoRepository.findTodas().stream(), n -> Stream.of(n.getUnidade()),
                        NOTIFICAR, INVESTIGAR)
                .filter(this::podeVerConteudo)
                .filter(n -> unidadeId == null || n.getUnidade().getUuid().equals(unidadeId))
                .filter(n -> agravoId == null || n.getAgravo().getUuid().equals(agravoId))
                .toList();
        List<NotificacaoDto> todos = visiveis.stream().map(n -> resumo(n, hoje)).toList();
        List<NotificacaoDto> lista = todos.stream()
                .filter(d -> situacao == null || d.getSituacao() == situacao)
                .filter(d -> !atrasadas || d.isEncerramentoAtrasado())
                .sorted(Comparator.comparing((NotificacaoDto d) -> !d.isEncerramentoAtrasado())
                        .thenComparing(NotificacaoDto::getDataNotificacao, Comparator.reverseOrder()))
                .toList();
        return new NotificacoesDto(todos.stream().filter(d -> d.getSituacao() != SituacaoNotificacao.ENCERRADA).count(),
                todos.stream().filter(d -> d.getSituacao() == SituacaoNotificacao.EM_INVESTIGACAO).count(),
                todos.stream().filter(NotificacaoDto::isEncerramentoAtrasado).count(), lista);
    }

    @Transactional(readOnly = true)
    public NotificacaoDto notificacao(UUID uuid) {
        Notificacao n = buscarNotificacao(uuid);
        ContextoAuditoria.paciente(n.getPaciente().getUuid());
        ContextoAuditoria.registro(n.getUuid());
        controleDeAcesso.exigirVisivel(n.getUnidade(), NOTIFICAR, INVESTIGAR);
        if (!podeVerConteudo(n)) {
            throw new ResourceForbiddenException("Notificação de agravo sensível: só a vigilância e quem notificou veem o detalhe.");
        }
        return detalhe(n, true);
    }

    /**
     * Sugestões de notificação nas unidades do escopo: internações e pedidos de exame dos últimos 30 dias com CID de agravo
     * compulsório, e crises da SM3 com notificação pendente, quando o paciente ainda não tem notificação daquele agravo.
     */
    @Transactional(readOnly = true)
    public List<SugestaoNotificacaoDto> sugestoes(UUID unidadeId) {
        Instant desde = Instant.now().minus(JANELA_SUGESTAO);
        List<Agravo> compulsorios = agravoRepository.findAllByOrderByNomeAsc().stream().filter(a -> a.isAtivo() && a.isCompulsorio()).toList();
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(NOTIFICAR);
        java.util.function.Predicate<UnidadeDeSaude> noEscopo = u -> u != null && visiveis.map(ids -> ids.contains(u.getUuid())).orElse(true)
                && (unidadeId == null || u.getUuid().equals(unidadeId));
        Map<UUID, List<Notificacao>> porPaciente = new HashMap<>();
        java.util.function.Function<UUID, List<Notificacao>> notificacoesDe = p -> porPaciente.computeIfAbsent(p, notificacaoRepository::findByPaciente_Uuid);
        List<SugestaoNotificacaoDto> lista = new ArrayList<>();
        for (Internacao i : internacaoRepository.findAllByOrderByAdmitidaEmDesc()) {
            if (i.getAdmitidaEm().isBefore(desde) || !noEscopo.test(i.getUnidade())) {
                continue;
            }
            agravoDoCid(compulsorios, i.getCid()).filter(a -> semNotificacao(notificacoesDe.apply(i.getPaciente().getUuid()), a, i.getAdmitidaEm()))
                    .ifPresent(a -> lista.add(new SugestaoNotificacaoDto(OrigemNotificacao.INTERNACAO, i.getUuid(), i.getPaciente().getUuid(),
                            i.getPaciente().getNome(), i.getUnidade().getUuid(), i.getUnidade().getNome(), a.getUuid(), a.getCodigo(), a.getNome(),
                            i.getCid(), i.getAdmitidaEm())));
        }
        for (PedidoExame p : pedidoExameRepository.findAllByOrderBySolicitadoEmDesc()) {
            if (p.getSolicitadoEm().isBefore(desde) || !noEscopo.test(p.getUnidadeSolicitante())) {
                continue;
            }
            agravoDoCid(compulsorios, p.getCid()).filter(a -> semNotificacao(notificacoesDe.apply(p.getPaciente().getUuid()), a, p.getSolicitadoEm()))
                    .ifPresent(a -> lista.add(new SugestaoNotificacaoDto(OrigemNotificacao.EXAME, p.getUuid(), p.getPaciente().getUuid(),
                            p.getPaciente().getNome(), p.getUnidadeSolicitante().getUuid(), p.getUnidadeSolicitante().getNome(), a.getUuid(),
                            a.getCodigo(), a.getNome(), p.getCid(), p.getSolicitadoEm())));
        }
        Optional<Agravo> violencia = compulsorios.stream().filter(a -> AGRAVO_VIOLENCIA.equals(a.getCodigo())).findFirst();
        if (violencia.isPresent()) {
            for (EventoCrise c : criseRepository.findTodas()) {
                if (c.isNotificacaoSugerida() && c.getNotificadaEm() == null && noEscopo.test(c.getUnidade())) {
                    lista.add(new SugestaoNotificacaoDto(OrigemNotificacao.SAUDE_MENTAL, c.getUuid(), c.getPaciente().getUuid(), c.getPaciente().getNome(),
                            c.getUnidade().getUuid(), c.getUnidade().getNome(), violencia.get().getUuid(), violencia.get().getCodigo(),
                            violencia.get().getNome(), null, c.getIdentificadaEm()));
                }
            }
        }
        // Uma sugestão por paciente e agravo: a mais recente.
        Map<String, SugestaoNotificacaoDto> unicas = new LinkedHashMap<>();
        lista.stream().sorted(Comparator.comparing(SugestaoNotificacaoDto::getDetectadaEm).reversed())
                .forEach(s -> unicas.putIfAbsent(s.getPacienteId() + "|" + s.getAgravoId(), s));
        return List.copyOf(unicas.values());
    }

    /** Boletim só com agregados, por agravo, no escopo. */
    @Transactional(readOnly = true)
    public BoletimVigilanciaDto boletim(UUID unidadeId) {
        Map<String, long[]> contagem = new LinkedHashMap<>();
        Map<String, String> nomes = new HashMap<>();
        controleDeAcesso.filtrar(notificacaoRepository.findTodas().stream(), n -> Stream.of(n.getUnidade()), NOTIFICAR, INVESTIGAR, GERENCIAR)
                .filter(n -> unidadeId == null || n.getUnidade().getUuid().equals(unidadeId))
                .forEach(n -> {
                    long[] c = contagem.computeIfAbsent(n.getAgravo().getCodigo(), k -> new long[5]);
                    nomes.put(n.getAgravo().getCodigo(), n.getAgravo().getNome());
                    c[n.getSituacao().ordinal()]++;
                    if (n.getClassificacaoFinal() == ClassificacaoCasoEpidemiologico.CONFIRMADO) {
                        c[3]++;
                    }
                    if (n.getClassificacaoFinal() == ClassificacaoCasoEpidemiologico.DESCARTADO) {
                        c[4]++;
                    }
                });
        return new BoletimVigilanciaDto(contagem.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(e -> new BoletimVigilanciaDto.Linha(e.getKey(), nomes.get(e.getKey()), e.getValue()[0], e.getValue()[1], e.getValue()[2],
                        e.getValue()[3], e.getValue()[4])).toList());
    }

    // ── Notificação negativa ───────────────────────────────────────────────────────────────────────

    /** A unidade declara que não houve caso na semana; sem semana, a anterior (a que acabou). */
    @Transactional
    public NotificacaoNegativaDto declararNegativa(NotificacaoNegativaRequestDto dto) {
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(NOTIFICAR, unidade);
        SemanaEpidemiologica atual = SemanaEpidemiologica.de(LocalDate.now());
        SemanaEpidemiologica se = dto.getAno() != null && dto.getSemana() != null ? new SemanaEpidemiologica(dto.getAno(), dto.getSemana())
                : atual.anterior();
        if (!se.valida()) {
            throw new ResourceBadRequestException("A semana " + se.semana() + " não existe em " + se.ano() + ".");
        }
        if (se.inicio().isAfter(atual.inicio())) {
            throw new ResourceBadRequestException("A semana epidemiológica ainda não começou.");
        }
        if (negativaRepository.existsByUnidade_UuidAndAnoAndSemana(unidade.getUuid(), se.ano(), se.semana())) {
            throw new ResourceConflictException("A " + unidade.getNome() + " já declarou a semana " + se.semana() + "/" + se.ano() + ".");
        }
        if (notificacaoRepository.existsByUnidade_UuidAndDataNotificacaoBetween(unidade.getUuid(), se.inicio(), se.fim())) {
            throw new ResourceUnprocessableEntityException("A unidade notificou na semana " + se.semana() + "/" + se.ano()
                    + ": a notificação negativa é só para a semana sem caso.");
        }
        NotificacaoNegativa n = new NotificacaoNegativa();
        n.setUnidade(unidade);
        n.setAno(se.ano());
        n.setSemana(se.semana());
        n.setResponsavel(profissional(null));
        n.setDeclaradaEm(Instant.now());
        n.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        n = negativaRepository.save(n);
        ContextoAuditoria.registro(n.getUuid());
        return negativaDto(n);
    }

    /** As declarações e as semanas recentes (as 8 últimas encerradas) sem notificação nem declaração, por unidade. */
    @Transactional(readOnly = true)
    public SemanasEpidemiologicasDto semanas(UUID unidadeId) {
        SemanaEpidemiologica atual = SemanaEpidemiologica.de(LocalDate.now());
        List<UnidadeDeSaude> unidades = controleDeAcesso.filtrar(unidadeDeSaudeRepository.findAll().stream().filter(UnidadeDeSaude::isAtivo),
                        Stream::of, NOTIFICAR)
                .filter(u -> unidadeId == null || u.getUuid().equals(unidadeId))
                .toList();
        Set<UUID> ids = unidades.stream().map(UnidadeDeSaude::getUuid).collect(Collectors.toSet());
        List<NotificacaoNegativa> declaradas = negativaRepository.findTodas().stream().filter(n -> ids.contains(n.getUnidade().getUuid())).toList();
        Set<String> feitas = declaradas.stream().map(n -> n.getUnidade().getUuid() + "|" + n.getAno() + "|" + n.getSemana()).collect(Collectors.toSet());
        List<Notificacao> notificacoes = notificacaoRepository.findTodas().stream().filter(n -> ids.contains(n.getUnidade().getUuid())).toList();
        List<NotificacaoNegativaDto> pendentes = new ArrayList<>();
        // Só as unidades com o hábito de notificar entram nas pendências (alguma notificação ou declaração antes).
        Set<UUID> ativas = new HashSet<>();
        notificacoes.forEach(n -> ativas.add(n.getUnidade().getUuid()));
        declaradas.forEach(n -> ativas.add(n.getUnidade().getUuid()));
        for (UnidadeDeSaude u : unidades) {
            if (unidadeId == null && !ativas.contains(u.getUuid())) {
                continue;
            }
            SemanaEpidemiologica se = atual;
            for (int k = 0; k < 8; k++) {
                se = se.anterior();
                SemanaEpidemiologica s = se;
                boolean notificou = notificacoes.stream().anyMatch(n -> n.getUnidade().getUuid().equals(u.getUuid())
                        && !n.getDataNotificacao().isBefore(s.inicio()) && !n.getDataNotificacao().isAfter(s.fim()));
                if (!notificou && !feitas.contains(u.getUuid() + "|" + s.ano() + "|" + s.semana())) {
                    pendentes.add(new NotificacaoNegativaDto(null, u.getUuid(), u.getNome(), s.ano(), s.semana(), s.inicio(), s.fim(), null, null));
                }
            }
        }
        return new SemanasEpidemiologicasDto(atual.ano(), atual.semana(),
                declaradas.stream().map(this::negativaDto).toList(), pendentes);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** Agravo sensível: quem investiga na unidade da notificação ou quem notificou; com a autorização desligada, todos. */
    private boolean podeVerConteudo(Notificacao n) {
        if (!n.getAgravo().isSensivel() || !controleDeAcesso.ativo()) {
            return true;
        }
        String cpf = UsuarioAutenticado.cpf();
        return cpf != null && (cpf.equals(n.getRegistradoPorCpf()) || autorizacaoService.tem(cpf, INVESTIGAR, n.getUnidade().getUuid()));
    }

    private Notificacao paraInvestigar(UUID uuid) {
        Notificacao n = buscarNotificacao(uuid);
        controleDeAcesso.exigir(INVESTIGAR, n.getUnidade());
        ContextoAuditoria.paciente(n.getPaciente().getUuid());
        ContextoAuditoria.registro(n.getUuid());
        if (n.getSituacao() == SituacaoNotificacao.ENCERRADA) {
            throw new ResourceUnprocessableEntityException("A notificação foi encerrada e não muda mais.");
        }
        return n;
    }

    /** O agravo compulsório cujo CID (prefixo) cobre o CID registrado na rede. */
    static Optional<Agravo> agravoDoCid(Collection<Agravo> agravos, String cid) {
        if (cid == null || cid.isBlank()) {
            return Optional.empty();
        }
        String c = cid.replace(".", "").trim().toUpperCase(Locale.ROOT);
        return agravos.stream().filter(a -> a.getCids().stream().anyMatch(p -> c.startsWith(p.replace(".", "").toUpperCase(Locale.ROOT))))
                .findFirst();
    }

    /** Sem notificação do agravo em aberto, nem feita depois da detecção. */
    private static boolean semNotificacao(List<Notificacao> doPaciente, Agravo agravo, Instant detectadaEm) {
        LocalDate dia = detectadaEm.atZone(ZoneId.systemDefault()).toLocalDate();
        return doPaciente.stream().noneMatch(n -> n.getAgravo().getUuid().equals(agravo.getUuid())
                && (n.getSituacao() != SituacaoNotificacao.ENCERRADA || !n.getDataNotificacao().isBefore(dia)));
    }

    /** Respostas conferidas contra a ficha: chaves conhecidas, obrigatórios preenchidos e tipos. */
    static void validarRespostas(List<CampoFichaDto> campos, Map<String, Object> respostas) {
        Map<String, CampoFichaDto> porChave = campos.stream().collect(Collectors.toMap(CampoFichaDto::getChave, c -> c));
        for (String chave : respostas.keySet()) {
            if (!porChave.containsKey(chave)) {
                throw new ResourceBadRequestException("A ficha não tem o campo " + chave + ".");
            }
        }
        for (CampoFichaDto c : campos) {
            Object v = respostas.get(c.getChave());
            boolean vazio = v == null || (v instanceof String s && s.isBlank()) || (v instanceof Collection<?> l && l.isEmpty());
            if (vazio) {
                if (Boolean.TRUE.equals(c.getObrigatorio())) {
                    throw new ResourceBadRequestException("Responda o campo obrigatório \"" + c.getRotulo() + "\".");
                }
                continue;
            }
            boolean ok = switch (c.getTipo()) {
                case SIM_NAO -> v instanceof Boolean;
                case TEXTO -> v instanceof String s && s.length() <= 2000;
                case NUMERO -> v instanceof Number;
                case DATA -> v instanceof String s && data(s);
                case OPCAO -> v instanceof String s && c.getOpcoes() != null && c.getOpcoes().contains(s);
                case MULTIPLA -> v instanceof Collection<?> l && c.getOpcoes() != null && c.getOpcoes().containsAll(l);
            };
            if (!ok) {
                throw new ResourceBadRequestException("Resposta inválida no campo \"" + c.getRotulo() + "\".");
            }
        }
    }

    private static boolean data(String s) {
        try {
            LocalDate.parse(s);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private <T> T referencia(UUID id, java.util.function.Function<UUID, Optional<T>> busca, java.util.function.Function<T, Paciente> paciente,
                             Paciente esperado, String nome) {
        if (id == null) {
            return null;
        }
        T t = busca.apply(id).orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o(a) " + nome + " " + id + " em nossos registros."));
        if (!paciente.apply(t).getUuid().equals(esperado.getUuid())) {
            throw new ResourceUnprocessableEntityException("O(a) " + nome + " informado(a) é de outro paciente.");
        }
        return t;
    }

    private Profissional profissional(String matricula) {
        String m = texto(matricula);
        if (m != null) {
            return profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar um profissional com a matrícula " + m + " em nossos registros."));
        }
        String cpf = UsuarioAutenticado.cpf();
        return cpf == null ? null : profissionalRepository.findByCpfAndAtivoTrue(cpf).orElse(null);
    }

    private void preencher(Agravo a, AgravoRequestDto dto) {
        a.setNome(dto.getNome().trim());
        a.setDescricao(texto(dto.getDescricao()));
        Set<String> cids = new HashSet<>();
        for (String c : dto.getCids() == null ? List.<String>of() : dto.getCids()) {
            String n = c == null ? "" : c.replace(".", "").trim().toUpperCase(Locale.ROOT);
            if (!n.matches("^[A-Z][0-9]{2}[0-9A-Z]?$")) {
                throw new ResourceBadRequestException("CID-10 inválido: " + c + " (ex.: A90, A92.0).");
            }
            cids.add(n);
        }
        a.setCids(cids);
        a.setCompulsorio(!Boolean.FALSE.equals(dto.getCompulsorio()));
        a.setPeriodicidade(dto.getPeriodicidade());
        a.setPrazoEncerramentoDias(dto.getPrazoEncerramentoDias());
        a.setCriterios(new HashSet<>(dto.getCriterios()));
        a.setSensivel(Boolean.TRUE.equals(dto.getSensivel()));
        a.setAtivo(!Boolean.FALSE.equals(dto.getAtivo()));
    }

    private AgravoDto agravoDto(Agravo a) {
        Optional<FichaAgravo> f = fichaRepository.findFirstByAgravo_UuidOrderByVersaoDesc(a.getUuid());
        return new AgravoDto(a.getUuid(), a.getCodigo(), a.getNome(), a.getDescricao(), a.getCids().stream().sorted().toList(), a.isCompulsorio(),
                a.getPeriodicidade(), a.getPrazoEncerramentoDias(), a.getCriterios().stream().sorted().toList(), a.isSensivel(), a.isAtivo(),
                f.map(FichaAgravo::getVersao).orElse(null), f.map(this::campos).orElse(List.of()));
    }

    private List<CampoFichaDto> campos(FichaAgravo f) {
        try {
            Map<String, List<CampoFichaDto>> m = JSON.readValue(f.getDefinicao(), new TypeReference<Map<String, List<CampoFichaDto>>>() { });
            return m.getOrDefault("campos", List.of());
        } catch (Exception e) {
            throw new IllegalStateException("Ficha do agravo ilegível: " + f.getUuid(), e);
        }
    }

    private static String json(Object o) {
        try {
            return JSON.writeValueAsString(o);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private NotificacaoDto resumo(Notificacao n, LocalDate hoje) {
        List<ClassificacaoCaso> historico = classificacaoRepository.findDaNotificacao(n.getUuid());
        return new NotificacaoDto(n.getUuid(), n.getPaciente().getUuid(), n.getPaciente().getNome(), n.getAgravo().getUuid(), n.getAgravo().getCodigo(),
                n.getAgravo().getNome(), n.getAgravo().isSensivel(), n.getAgravo().getPeriodicidade(), n.getUnidade().getUuid(), n.getUnidade().getNome(),
                n.getProfissional() != null ? n.getProfissional().getNome() : null, n.getDataPrimeirosSintomas(), n.getDataNotificacao(), n.getOrigem(),
                n.getCrise() != null ? n.getCrise().getUuid() : null, null, null, n.getFichaVersao(), List.of(), n.getSituacao(),
                n.getPrazoEncerramento(), n.getSituacao() != SituacaoNotificacao.ENCERRADA && n.getPrazoEncerramento().isBefore(hoje),
                historico.isEmpty() ? null : historico.get(0).getClassificacao(), n.getClassificacaoFinal(), n.getEvolucao(), n.getEncerradaEm(),
                null, null, List.of());
    }

    private NotificacaoDto detalhe(Notificacao n, boolean completo) {
        NotificacaoDto d = resumo(n, LocalDate.now());
        if (!completo) {
            return d;
        }
        d.setLocalProvavel(n.getLocalProvavel());
        if (n.getFichaVersao() != null) {
            fichaRepository.findByAgravo_UuidAndVersao(n.getAgravo().getUuid(), n.getFichaVersao()).ifPresent(f -> d.setFicha(campos(f)));
            try {
                d.setRespostas(n.getRespostas() == null ? Map.of() : JSON.readValue(n.getRespostas(), new TypeReference<Map<String, Object>>() { }));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        investigacaoRepository.findByNotificacao_Uuid(n.getUuid()).ifPresent(i -> d.setInvestigacao(new NotificacaoDto.Investigacao(
                i.getInvestigador() != null ? i.getInvestigador().getNome() : null, i.getIniciadaEm(), i.getExposicao(), i.getLocalProvavelInfeccao(),
                i.getMedidas(), i.getExamesIds().stream().sorted().toList())));
        d.setClassificacoes(classificacaoRepository.findDaNotificacao(n.getUuid()).stream().map(c -> new NotificacaoDto.Classificacao(
                c.getClassificacao(), c.getCriterio(), c.getJustificativa(), c.getClassificadaEm(),
                c.getResponsavel() != null ? c.getResponsavel().getNome() : null)).toList());
        return d;
    }

    private NotificacaoNegativaDto negativaDto(NotificacaoNegativa n) {
        SemanaEpidemiologica se = new SemanaEpidemiologica(n.getAno(), n.getSemana());
        return new NotificacaoNegativaDto(n.getUuid(), n.getUnidade().getUuid(), n.getUnidade().getNome(), n.getAno(), n.getSemana(), se.inicio(),
                se.fim(), n.getResponsavel() != null ? n.getResponsavel().getNome() : null, n.getDeclaradaEm());
    }

    private Agravo buscarAgravo(UUID uuid) {
        return agravoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o agravo " + uuid + " em nossos registros."));
    }

    private Notificacao buscarNotificacao(UUID uuid) {
        return notificacaoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a notificação " + uuid + " em nossos registros."));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
