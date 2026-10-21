package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Agravo;
import com.edufelizardo.maissaudepublica.models.ClassificacaoCaso;
import com.edufelizardo.maissaudepublica.models.EventoAuditoria;
import com.edufelizardo.maissaudepublica.models.MedidaControle;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Surto;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CasosSurtoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MedidaControleRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoSurtoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SurtoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EventoLinhaDoTempoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MedidaControleDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SurtoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SurtosDto;
import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.EvolucaoCaso;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaControle;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSurto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSurto;
import com.edufelizardo.maissaudepublica.repositories.AgravoRepository;
import com.edufelizardo.maissaudepublica.repositories.ClassificacaoCasoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoAuditoriaRepository;
import com.edufelizardo.maissaudepublica.repositories.MedidaControleRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SurtoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Surto e medidas de controle, fatia V2b (ADR-0131, desenho na ADR-0110): o surto agrupa casos do mesmo agravo num
 * território ou estabelecimento, com o ciclo suspeito → em investigação → confirmado ou descartado → encerrado; as medidas
 * de controle são do surto ou de um caso isolado. A linha do tempo do surto vem da trilha de auditoria (cada ação deixa o
 * texto do que mudou), e não de uma tabela própria.
 */
@Service
public class SurtoService {

    static final Set<SituacaoMedidaControle> ABERTAS = EnumSet.of(SituacaoMedidaControle.PLANEJADA, SituacaoMedidaControle.EM_ANDAMENTO);
    private static final String NOTIFICAR = VigilanciaService.NOTIFICAR;
    private static final String INVESTIGAR = VigilanciaService.INVESTIGAR;

    @Autowired
    private SurtoRepository surtoRepository;

    @Autowired
    private MedidaControleRepository medidaRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private ClassificacaoCasoRepository classificacaoRepository;

    @Autowired
    private AgravoRepository agravoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private MicroareaRepository microareaRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private EventoAuditoriaRepository eventoAuditoriaRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private AutorizacaoService autorizacaoService;

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** Os surtos do escopo (os de agravo sensível só para quem investiga) e os indicadores. */
    @Transactional(readOnly = true)
    public SurtosDto surtos(UUID unidadeId, UUID agravoId, SituacaoSurto situacao) {
        LocalDate hoje = LocalDate.now();
        List<Surto> visiveis = controleDeAcesso.filtrar(surtoRepository.findTodos().stream(), s -> Stream.of(s.getUnidade()), NOTIFICAR, INVESTIGAR)
                .filter(this::podeVer)
                .filter(s -> unidadeId == null || s.getUnidade().getUuid().equals(unidadeId))
                .filter(s -> agravoId == null || s.getAgravo().getUuid().equals(agravoId))
                .toList();
        List<SurtoDto> todos = visiveis.stream().map(s -> resumo(s, hoje)).toList();
        Set<UUID> surtos = visiveis.stream().map(Surto::getUuid).collect(Collectors.toSet());
        long vencidas = medidaRepository.findBySituacaoIn(ABERTAS).stream()
                .filter(m -> vencida(m, hoje))
                .filter(m -> m.getSurto() != null ? surtos.contains(m.getSurto().getUuid()) : visivel(m.getNotificacao()))
                .filter(m -> unidadeId == null || (m.getSurto() != null ? m.getSurto().getUnidade() : m.getNotificacao().getUnidade()).getUuid().equals(unidadeId))
                .count();
        return new SurtosDto(todos.stream().filter(d -> d.getSituacao() != SituacaoSurto.ENCERRADO && d.getSituacao() != SituacaoSurto.DESCARTADO).count(),
                todos.stream().filter(d -> d.getSituacao() == SituacaoSurto.CONFIRMADO).count(), vencidas,
                todos.stream().filter(d -> situacao == null || d.getSituacao() == situacao).toList());
    }

    @Transactional(readOnly = true)
    public SurtoDto surto(UUID uuid) {
        Surto s = buscar(uuid);
        ContextoAuditoria.registro(s.getUuid());
        controleDeAcesso.exigirVisivel(s.getUnidade(), NOTIFICAR, INVESTIGAR);
        if (!podeVer(s)) {
            throw new ResourceForbiddenException("Surto de agravo sensível: só a vigilância vê o detalhe.");
        }
        return detalhe(s);
    }

    /** O surto que quem lê pode ver (o filtro do mapa). */
    @Transactional(readOnly = true)
    public Surto paraLer(UUID uuid) {
        Surto s = buscar(uuid);
        controleDeAcesso.exigirVisivel(s.getUnidade(), NOTIFICAR, INVESTIGAR);
        if (!podeVer(s)) {
            throw new ResourceForbiddenException("Surto de agravo sensível: só a vigilância vê o detalhe.");
        }
        return s;
    }

    // ── Surto ──────────────────────────────────────────────────────────────────────────────────────

    /** Abre o surto (suspeito), já com os casos informados. */
    @Transactional
    public SurtoDto abrir(SurtoRequestDto dto) {
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(INVESTIGAR, unidade);
        Agravo agravo = agravoRepository.findById(dto.getAgravoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o agravo " + dto.getAgravoId() + " em nossos registros."));
        Surto s = new Surto();
        s.setAgravo(agravo);
        s.setUnidade(unidade);
        aplicar(s, dto);
        s.setSituacao(SituacaoSurto.SUSPEITO);
        s.setAbertoEm(Instant.now());
        s.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        s = surtoRepository.save(s);
        ContextoAuditoria.registro(s.getUuid());
        int casos = incluir(s, dto.getNotificacoesIds() == null ? List.of() : dto.getNotificacoesIds());
        ContextoAuditoria.detalhe("Surto aberto: " + s.getNome() + (casos > 0 ? ", com " + casos + (casos == 1 ? " caso." : " casos.") : "."));
        return detalhe(s);
    }

    /** Os dados do surto; o agravo e a unidade não mudam. */
    @Transactional
    public SurtoDto atualizar(UUID uuid, SurtoRequestDto dto) {
        Surto s = paraGerir(uuid);
        if (!s.getAgravo().getUuid().equals(dto.getAgravoId()) || !s.getUnidade().getUuid().equals(dto.getUnidadeId())) {
            throw new ResourceUnprocessableEntityException("O agravo e a unidade responsável do surto não mudam.");
        }
        aplicar(s, dto);
        surtoRepository.save(s);
        ContextoAuditoria.detalhe("Dados atualizados.");
        return detalhe(s);
    }

    /**
     * Muda a situação. Encerrar pede o surto confirmado ou descartado, a conclusão e nenhuma medida em aberto (planejada ou
     * em andamento); depois, o surto não muda.
     */
    @Transactional
    public SurtoDto mudarSituacao(UUID uuid, SituacaoSurtoRequestDto dto) {
        Surto s = paraGerir(uuid);
        SituacaoSurto antes = s.getSituacao();
        if (dto.getSituacao() == SituacaoSurto.ENCERRADO) {
            if (antes != SituacaoSurto.CONFIRMADO && antes != SituacaoSurto.DESCARTADO) {
                throw new ResourceUnprocessableEntityException("Para encerrar, confirme ou descarte o surto.");
            }
            String conclusao = texto(dto.getConclusao());
            if (conclusao == null) {
                throw new ResourceBadRequestException("Encerrar pede a conclusão do surto.");
            }
            if (medidaRepository.existsBySurto_UuidAndSituacaoIn(s.getUuid(), ABERTAS)) {
                throw new ResourceUnprocessableEntityException("Há medidas planejadas ou em andamento: conclua ou cancele antes de encerrar.");
            }
            s.setConclusao(conclusao);
            s.setEncerradoEm(Instant.now());
            if (s.getFim() == null) {
                s.setFim(LocalDate.now());
            }
        }
        s.setSituacao(dto.getSituacao());
        surtoRepository.save(s);
        ContextoAuditoria.detalhe("Situação: " + rotulo(antes) + " → " + rotulo(dto.getSituacao()) + "."
                + (dto.getSituacao() == SituacaoSurto.ENCERRADO ? " " + s.getConclusao() : ""));
        return detalhe(s);
    }

    @Transactional
    public SurtoDto incluirCasos(UUID uuid, CasosSurtoRequestDto dto) {
        Surto s = paraGerir(uuid);
        int casos = incluir(s, dto.getNotificacoesIds());
        ContextoAuditoria.detalhe("Casos incluídos: " + casos + ".");
        return detalhe(s);
    }

    @Transactional
    public SurtoDto retirarCaso(UUID uuid, UUID notificacaoId) {
        Surto s = paraGerir(uuid);
        Notificacao n = notificacaoRepository.findById(notificacaoId).filter(x -> x.getSurto() != null && x.getSurto().getUuid().equals(s.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("A notificação " + notificacaoId + " não está no surto."));
        n.setSurto(null);
        n.setSurtoIncluidaEm(null);
        notificacaoRepository.save(n);
        ContextoAuditoria.detalhe("Caso retirado: notificação " + curto(n.getUuid()) + ".");
        return detalhe(s);
    }

    // ── Medidas de controle ────────────────────────────────────────────────────────────────────────

    @Transactional
    public MedidaControleDto medidaDoSurto(UUID surtoId, MedidaControleRequestDto dto) {
        Surto s = paraGerir(surtoId);
        MedidaControle m = new MedidaControle();
        m.setSurto(s);
        return criar(m, dto);
    }

    /** Medida de um caso isolado (o bloqueio vacinal de um caso de sarampo não precisa de surto). */
    @Transactional
    public MedidaControleDto medidaDoCaso(UUID notificacaoId, MedidaControleRequestDto dto) {
        Notificacao n = notificacaoRepository.findById(notificacaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a notificação " + notificacaoId + " em nossos registros."));
        exigirCaso(n);
        MedidaControle m = new MedidaControle();
        m.setNotificacao(n);
        return criar(m, dto);
    }

    /** Andamento da medida; concluir pede o resultado. Concluída ou cancelada, não muda. */
    @Transactional
    public MedidaControleDto atualizarMedida(UUID medidaId, MedidaControleRequestDto dto) {
        MedidaControle m = medidaRepository.findById(medidaId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a medida " + medidaId + " em nossos registros."));
        if (m.getSurto() != null) {
            paraGerir(m.getSurto().getUuid());
        } else {
            exigirCaso(m.getNotificacao());
        }
        if (!ABERTAS.contains(m.getSituacao())) {
            throw new ResourceUnprocessableEntityException("A medida foi " + (m.getSituacao() == SituacaoMedidaControle.CONCLUIDA ? "concluída" : "cancelada")
                    + " e não muda mais.");
        }
        SituacaoMedidaControle antes = m.getSituacao();
        aplicar(m, dto, dto.getSituacao() != null ? dto.getSituacao() : antes);
        medidaRepository.save(m);
        ContextoAuditoria.detalhe("Medida " + rotulo(m) + ": " + rotulo(antes) + " → " + rotulo(m.getSituacao()) + "."
                + (m.getResultado() != null && !ABERTAS.contains(m.getSituacao()) ? " " + m.getResultado() : ""));
        return medidaDto(m, LocalDate.now());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private MedidaControleDto criar(MedidaControle m, MedidaControleRequestDto dto) {
        m.setCriadaEm(Instant.now());
        m.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        aplicar(m, dto, dto.getSituacao() != null ? dto.getSituacao() : SituacaoMedidaControle.PLANEJADA);
        m = medidaRepository.save(m);
        ContextoAuditoria.detalhe("Medida: " + rotulo(m) + " (" + rotulo(m.getSituacao()).toLowerCase() + ")" + (m.getPrazo() != null ? ", prazo "
                + m.getPrazo() : "") + ".");
        return medidaDto(m, LocalDate.now());
    }

    private void aplicar(MedidaControle m, MedidaControleRequestDto dto, SituacaoMedidaControle situacao) {
        m.setTipo(dto.getTipo());
        m.setDescricao(dto.getDescricao().trim());
        m.setResponsavel(profissional(dto.getResponsavelMatricula()));
        m.setPrazo(dto.getPrazo());
        m.setResultado(texto(dto.getResultado()));
        if (situacao == SituacaoMedidaControle.CONCLUIDA && m.getResultado() == null) {
            throw new ResourceBadRequestException("Concluir a medida pede o resultado.");
        }
        m.setSituacao(situacao);
        m.setConcluidaEm(ABERTAS.contains(situacao) ? null : Instant.now());
    }

    private void aplicar(Surto s, SurtoRequestDto dto) {
        s.setNome(dto.getNome().trim());
        s.setTipo(dto.getTipo());
        if (dto.getTipo() == TipoSurto.INSTITUCIONAL) {
            if (dto.getTipoInstituicao() == null || texto(dto.getNomeInstituicao()) == null) {
                throw new ResourceBadRequestException("O surto institucional pede o tipo e o nome do estabelecimento.");
            }
            s.setTipoInstituicao(dto.getTipoInstituicao());
            s.setNomeInstituicao(texto(dto.getNomeInstituicao()));
            s.setEnderecoInstituicao(texto(dto.getEnderecoInstituicao()));
        } else {
            s.setTipoInstituicao(null);
            s.setNomeInstituicao(null);
            s.setEnderecoInstituicao(null);
        }
        s.getMicroareas().clear();
        for (UUID id : dto.getMicroareasIds() == null ? List.<UUID>of() : dto.getMicroareasIds()) {
            s.getMicroareas().add(microareaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a microárea " + id + " em nossos registros.")));
        }
        s.setDefinicaoCaso(texto(dto.getDefinicaoCaso()));
        LocalDate hoje = LocalDate.now();
        if (dto.getInicio().isAfter(hoje) || (dto.getFim() != null && dto.getFim().isAfter(hoje))) {
            throw new ResourceBadRequestException("O período do surto não pode ser no futuro.");
        }
        if (dto.getFim() != null && dto.getFim().isBefore(dto.getInicio())) {
            throw new ResourceBadRequestException("O fim do surto precisa ser depois do início.");
        }
        s.setInicio(dto.getInicio());
        s.setFim(dto.getFim());
    }

    /** Liga os casos ao surto: do mesmo agravo (422), visíveis a quem gere, e cada um num surto só (409). */
    private int incluir(Surto s, List<UUID> ids) {
        int novos = 0;
        for (UUID id : ids) {
            Notificacao n = notificacaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a notificação " + id + " em nossos registros."));
            controleDeAcesso.exigirVisivel(n.getUnidade(), NOTIFICAR, INVESTIGAR);
            if (!n.getAgravo().getUuid().equals(s.getAgravo().getUuid())) {
                throw new ResourceUnprocessableEntityException("A notificação " + curto(n.getUuid()) + " é de " + n.getAgravo().getNome()
                        + ", e o surto é de " + s.getAgravo().getNome() + ".");
            }
            if (n.getSurto() != null) {
                if (n.getSurto().getUuid().equals(s.getUuid())) {
                    continue;
                }
                throw new ResourceConflictException("A notificação " + curto(n.getUuid()) + " já está no surto " + n.getSurto().getNome() + ".");
            }
            n.setSurto(s);
            n.setSurtoIncluidaEm(Instant.now());
            notificacaoRepository.save(n);
            novos++;
        }
        return novos;
    }

    private Surto paraGerir(UUID uuid) {
        Surto s = buscar(uuid);
        controleDeAcesso.exigir(INVESTIGAR, s.getUnidade());
        ContextoAuditoria.registro(s.getUuid());
        if (!podeVer(s)) {
            throw new ResourceForbiddenException("Surto de agravo sensível: só a vigilância o gere.");
        }
        if (s.getSituacao() == SituacaoSurto.ENCERRADO) {
            throw new ResourceUnprocessableEntityException("O surto foi encerrado e não muda mais.");
        }
        return s;
    }

    /** A medida do caso é da investigação, na unidade da notificação; encerrada, não recebe medida. */
    private void exigirCaso(Notificacao n) {
        controleDeAcesso.exigir(INVESTIGAR, n.getUnidade());
        ContextoAuditoria.paciente(n.getPaciente().getUuid());
        ContextoAuditoria.registro(n.getUuid());
        if (!visivel(n)) {
            throw new ResourceForbiddenException("Notificação de agravo sensível: só a vigilância e quem notificou veem o detalhe.");
        }
        if (n.getSituacao() == SituacaoNotificacao.ENCERRADA) {
            throw new ResourceUnprocessableEntityException("A notificação foi encerrada e não muda mais.");
        }
    }

    /** Surto de agravo sensível: só quem investiga na unidade responsável; com a autorização desligada, todos. */
    private boolean podeVer(Surto s) {
        if (!s.getAgravo().isSensivel() || !controleDeAcesso.ativo()) {
            return true;
        }
        String cpf = UsuarioAutenticado.cpf();
        return cpf != null && autorizacaoService.tem(cpf, INVESTIGAR, s.getUnidade().getUuid());
    }

    /** A notificação no escopo de quem lê, com o sigilo do agravo sensível. */
    private boolean visivel(Notificacao n) {
        if (controleDeAcesso.filtrar(Stream.of(n), x -> Stream.of(x.getUnidade()), NOTIFICAR, INVESTIGAR).findAny().isEmpty()) {
            return false;
        }
        if (!n.getAgravo().isSensivel() || !controleDeAcesso.ativo()) {
            return true;
        }
        String cpf = UsuarioAutenticado.cpf();
        return cpf != null && (cpf.equals(n.getRegistradoPorCpf()) || autorizacaoService.tem(cpf, INVESTIGAR, n.getUnidade().getUuid()));
    }

    private SurtoDto resumo(Surto s, LocalDate hoje) {
        List<Notificacao> casos = notificacaoRepository.findBySurto_Uuid(s.getUuid());
        List<MedidaControle> medidas = medidaRepository.findBySurto_UuidOrderByCriadaEmAsc(s.getUuid());
        int confirmados = (int) casos.stream().filter(n -> classificacao(n) == ClassificacaoCasoEpidemiologico.CONFIRMADO).count();
        int obitos = (int) casos.stream().filter(n -> n.getEvolucao() == EvolucaoCaso.OBITO_PELO_AGRAVO).count();
        return new SurtoDto(s.getUuid(), s.getAgravo().getUuid(), s.getAgravo().getCodigo(), s.getAgravo().getNome(), s.getAgravo().isSensivel(),
                s.getUnidade().getUuid(), s.getUnidade().getNome(), s.getNome(), s.getTipo(), s.getTipoInstituicao(), s.getNomeInstituicao(),
                s.getEnderecoInstituicao(), s.getDefinicaoCaso(), s.getInicio(), s.getFim(), s.getSituacao(), s.getConclusao(), s.getEncerradoEm(),
                s.getAbertoEm(), casos.size(), confirmados, obitos, (int) medidas.stream().filter(m -> ABERTAS.contains(m.getSituacao())).count(),
                (int) medidas.stream().filter(m -> vencida(m, hoje)).count(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private SurtoDto detalhe(Surto s) {
        LocalDate hoje = LocalDate.now();
        SurtoDto d = resumo(s, hoje);
        List<Notificacao> casos = notificacaoRepository.findBySurto_Uuid(s.getUuid()).stream()
                .sorted(Comparator.comparing(SurtoService::dataDoCaso)).toList();
        d.setListaCasos(casos.stream().map(n -> new SurtoDto.Caso(n.getUuid(), n.getPaciente().getNome(), n.getUnidade().getNome(),
                n.getDataPrimeirosSintomas(), n.getDataNotificacao(), n.getSituacao(), classificacao(n), n.getEvolucao(),
                n.getMicroarea() != null ? n.getMicroarea().getCodigo() : null, n.getSurtoIncluidaEm())).toList());
        // As microáreas declaradas e as dos casos, com a contagem.
        Map<UUID, Microarea> microareas = new LinkedHashMap<>();
        s.getMicroareas().stream().sorted(Comparator.comparing(Microarea::getCodigo)).forEach(m -> microareas.put(m.getUuid(), m));
        casos.stream().map(Notificacao::getMicroarea).filter(m -> m != null).forEach(m -> microareas.putIfAbsent(m.getUuid(), m));
        Map<UUID, Long> porMicroarea = casos.stream().filter(n -> n.getMicroarea() != null)
                .collect(Collectors.groupingBy(n -> n.getMicroarea().getUuid(), Collectors.counting()));
        d.setMicroareas(microareas.values().stream().map(m -> new SurtoDto.Microarea(m.getUuid(), m.getCodigo(), m.getArea().getCodigo(), m.getGeometria(),
                porMicroarea.getOrDefault(m.getUuid(), 0L).intValue())).toList());
        d.setMedidas(medidaRepository.findBySurto_UuidOrderByCriadaEmAsc(s.getUuid()).stream().map(m -> medidaDto(m, hoje)).toList());
        List<LocalDate> datas = casos.stream().map(SurtoService::dataDoCaso).toList();
        LocalDate inicio = Stream.concat(Stream.of(s.getInicio()), datas.stream()).min(Comparator.naturalOrder()).orElse(s.getInicio());
        LocalDate fim = Stream.concat(Stream.of(s.getFim() != null ? s.getFim() : hoje), datas.stream()).max(Comparator.naturalOrder()).orElse(hoje);
        d.setCurva(VigilanciaService.curva(datas, inicio, fim));
        d.setLinhaDoTempo(linhaDoTempo(s));
        return d;
    }

    /** A linha do tempo do surto: as ações registradas na trilha de auditoria, da mais antiga à mais recente. */
    private List<EventoLinhaDoTempoDto> linhaDoTempo(Surto s) {
        Map<String, Optional<String>> nomes = new HashMap<>();
        List<EventoAuditoria> eventos = eventoAuditoriaRepository.findByRegistroIdOrderByOcorridoEmDesc(s.getUuid()).stream()
                .filter(e -> e.getAcao() != AcaoAuditoria.LEITURA && e.getResultado() == ResultadoAuditoria.PERMITIDO && e.getDetalhe() != null)
                .sorted(Comparator.comparing(EventoAuditoria::getOcorridoEm)).toList();
        return eventos.stream().map(e -> {
            String texto = e.getDetalhe();
            int i = texto.indexOf(": ");
            String autor = e.getUsuarioCpf() == null ? null : nomes.computeIfAbsent(e.getUsuarioCpf(),
                    cpf -> profissionalRepository.findByCpfAndAtivoTrue(cpf).map(Profissional::getNome)).orElse(null);
            return i > 0 && i < 40 ? new EventoLinhaDoTempoDto(e.getOcorridoEm(), texto.substring(0, i), texto.substring(i + 2), autor)
                    : new EventoLinhaDoTempoDto(e.getOcorridoEm(), texto, null, autor);
        }).toList();
    }

    MedidaControleDto medidaDto(MedidaControle m, LocalDate hoje) {
        return new MedidaControleDto(m.getUuid(), m.getSurto() != null ? m.getSurto().getUuid() : null,
                m.getNotificacao() != null ? m.getNotificacao().getUuid() : null, m.getTipo(), m.getDescricao(),
                m.getResponsavel() != null ? m.getResponsavel().getNome() : null, m.getPrazo(), vencida(m, hoje), m.getSituacao(), m.getResultado(),
                m.getCriadaEm(), m.getConcluidaEm());
    }

    static boolean vencida(MedidaControle m, LocalDate hoje) {
        return ABERTAS.contains(m.getSituacao()) && m.getPrazo() != null && m.getPrazo().isBefore(hoje);
    }

    private ClassificacaoCasoEpidemiologico classificacao(Notificacao n) {
        if (n.getClassificacaoFinal() != null) {
            return n.getClassificacaoFinal();
        }
        List<ClassificacaoCaso> h = classificacaoRepository.findDaNotificacao(n.getUuid());
        return h.isEmpty() ? null : h.get(0).getClassificacao();
    }

    private static LocalDate dataDoCaso(Notificacao n) {
        return n.getDataPrimeirosSintomas() != null ? n.getDataPrimeirosSintomas() : n.getDataNotificacao();
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

    private Surto buscar(UUID uuid) {
        return surtoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o surto " + uuid + " em nossos registros."));
    }

    static String rotulo(SituacaoSurto s) {
        return switch (s) {
            case SUSPEITO -> "Suspeito";
            case EM_INVESTIGACAO -> "Em investigação";
            case CONFIRMADO -> "Confirmado";
            case DESCARTADO -> "Descartado";
            case ENCERRADO -> "Encerrado";
        };
    }

    static String rotulo(SituacaoMedidaControle s) {
        return switch (s) {
            case PLANEJADA -> "Planejada";
            case EM_ANDAMENTO -> "Em andamento";
            case CONCLUIDA -> "Concluída";
            case CANCELADA -> "Cancelada";
        };
    }

    static String rotulo(MedidaControle m) {
        return switch (m.getTipo()) {
            case BLOQUEIO_VACINAL -> "bloqueio vacinal";
            case ISOLAMENTO -> "isolamento";
            case QUIMIOPROFILAXIA -> "quimioprofilaxia";
            case CONTROLE_VETORIAL -> "controle vetorial";
            case BUSCA_ATIVA -> "busca ativa";
            case EDUCACAO_EM_SAUDE -> "educação em saúde";
            case INTERDICAO -> "interdição ou recolhimento";
            case OUTRA -> "outra";
        };
    }

    private static String curto(UUID id) {
        return id.toString().substring(0, 8).toUpperCase(java.util.Locale.ROOT);
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
