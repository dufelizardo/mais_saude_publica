package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.HorarioUnidade;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.TurnoEscala;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CopiarSemanaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DesignarTurnoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TurnoEscalaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CopiaSemanaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EscalaSemanaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TurnoEscalaDto;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAfastamento;
import com.edufelizardo.maissaudepublica.repositories.AfastamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.HorarioUnidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroEquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TurnoEscalaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Escalas (ADR-0105): turnos com data na unidade, vagas abertas e designação, com as regras de horário da unidade (ADR-0101),
 * lotação e afastamento do RH, sobreposição, e os alertas de jornada semanal e de descanso entre turnos.
 */
@Service
public class EscalaService {

    public static final String GERENCIAR = "ESCALA.GERENCIAR";
    static final String[] VER = {GERENCIAR, EquipeService.GERENCIAR, "RH.CONSULTAR", "RH.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"};
    private static final Set<StatusAfastamento> AFASTA = Set.of(StatusAfastamento.APROVADO, StatusAfastamento.EM_ANDAMENTO);
    /** Descanso mínimo entre duas jornadas (CLT, art. 66). */
    private static final long DESCANSO_MINIMO = 11 * 60;
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private TurnoEscalaRepository turnoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private MembroEquipeRepository membroRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private LotacaoRepository lotacaoRepository;

    @Autowired
    private AfastamentoRepository afastamentoRepository;

    @Autowired
    private HorarioUnidadeRepository horarioRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /**
     * A semana da unidade: as pessoas lotadas nela (ou os membros da equipe) e quem tem turno nela, com os turnos da semana
     * em qualquer unidade, as horas, os alertas e as ausências; as vagas abertas; e as ausências dos próximos 30 dias.
     */
    @Transactional(readOnly = true)
    public EscalaSemanaResponseDto semana(UUID unidadeId, LocalDate dia, UUID equipeId) {
        UnidadeDeSaude unidade = buscarUnidade(unidadeId);
        controleDeAcesso.exigirVisivel(unidade, VER);
        LocalDate seg = segunda(dia != null ? dia : LocalDate.now());
        LocalDateTime de = seg.atStartOfDay();
        LocalDateTime ate = seg.plusDays(7).atStartOfDay();
        Equipe equipe = equipeId == null ? null : equipeDaUnidade(equipeId, unidade);

        List<TurnoEscala> daUnidade = turnoRepository.daUnidadeEntre(unidade.getUuid(), de, ate).stream()
                .filter(t -> equipe == null || (t.getEquipe() != null && t.getEquipe().getUuid().equals(equipe.getUuid())))
                .toList();

        List<Equipe> equipesDaUnidade = equipeRepository.findAllByOrderByNomeAsc().stream()
                .filter(e -> e.getUnidade().getUuid().equals(unidade.getUuid())).toList();
        List<MembroEquipe> membros = equipesDaUnidade.isEmpty() ? List.of()
                : membroRepository.findByEquipe_UuidInAndFimIsNull(equipesDaUnidade.stream().map(Equipe::getUuid).toList());
        Map<UUID, List<String>> equipesPorPessoa = membros.stream().collect(Collectors.groupingBy(m -> m.getProfissional().getUuid(),
                Collectors.mapping(m -> m.getEquipe().getNome(), Collectors.toList())));

        List<Lotacao> vigentes = lotacaoRepository.findVigentesComUnidadeECargo();
        Map<UUID, Lotacao> lotacoes = new HashMap<>();
        for (Lotacao l : vigentes) {
            boolean aqui = l.getUnidade() != null && l.getUnidade().getUuid().equals(unidade.getUuid());
            lotacoes.merge(l.getProfissional().getUuid(), l, (a, b) -> aqui ? b : a);
        }

        Map<UUID, Profissional> pessoas = new LinkedHashMap<>();
        if (equipe != null) {
            membros.stream().filter(m -> m.getEquipe().getUuid().equals(equipe.getUuid()))
                    .forEach(m -> pessoas.put(m.getProfissional().getUuid(), m.getProfissional()));
        } else {
            vigentes.stream()
                    .filter(l -> l.getUnidade() != null && l.getUnidade().getUuid().equals(unidade.getUuid()))
                    .forEach(l -> pessoas.put(l.getProfissional().getUuid(), l.getProfissional()));
        }
        daUnidade.stream().filter(t -> !t.vaga()).forEach(t -> pessoas.put(t.getProfissional().getUuid(), t.getProfissional()));

        Map<UUID, List<TurnoEscala>> turnosPorPessoa = pessoas.isEmpty() ? Map.of()
                : turnoRepository.dosProfissionaisEntre(pessoas.keySet(), de.minusDays(1), ate.plusDays(1)).stream()
                .collect(Collectors.groupingBy(t -> t.getProfissional().getUuid()));

        LocalDate hoje = LocalDate.now();
        List<Afastamento> afastamentos = afastamentoRepository.findQueTocam(seg.isBefore(hoje) ? seg : hoje,
                hoje.plusDays(30).isAfter(seg.plusDays(6)) ? hoje.plusDays(30) : seg.plusDays(6), AFASTA).stream()
                .filter(a -> pessoas.containsKey(a.getProfissional().getUuid())).toList();

        List<EscalaSemanaResponseDto.Linha> linhas = new ArrayList<>();
        for (Profissional p : pessoas.values()) {
            List<TurnoEscala> todos = turnosPorPessoa.getOrDefault(p.getUuid(), List.of());
            List<TurnoEscala> naSemana = todos.stream().filter(t -> !t.getInicioEm().isBefore(de) && t.getInicioEm().isBefore(ate)).toList();
            Lotacao l = lotacoes.get(p.getUuid());
            Integer jornada = l != null ? l.getJornadaSemanalHoras() : null;
            List<String> alertas = alertasDaSemana(todos, jornada, seg);
            linhas.add(new EscalaSemanaResponseDto.Linha(p.getMatricula(), p.getNome(), conselho(p),
                    l != null && l.getCargo() != null ? l.getCargo().getNome() : null, jornada,
                    horas(naSemana.stream().filter(t -> t.getTipo().somaNaJornada()).mapToLong(TurnoEscala::minutos).sum()),
                    horas(naSemana.stream().filter(t -> !t.getTipo().somaNaJornada()).mapToLong(TurnoEscala::minutos).sum()),
                    equipesPorPessoa.getOrDefault(p.getUuid(), List.of()), alertas,
                    afastamentos.stream().filter(a -> a.getProfissional().getUuid().equals(p.getUuid())
                            && !a.getDataInicio().isAfter(seg.plusDays(6)) && !a.getDataFim().isBefore(seg)).map(a -> ausencia(a, l)).toList(),
                    naSemana.stream().map(t -> dto(t, List.of())).toList()));
        }
        linhas.sort(Comparator.comparing(EscalaSemanaResponseDto.Linha::getNome, String.CASE_INSENSITIVE_ORDER));

        List<EscalaSemanaResponseDto.Ausencia> proximas = afastamentos.stream()
                .filter(a -> !a.getDataFim().isBefore(hoje) && !a.getDataInicio().isAfter(hoje.plusDays(30)))
                .sorted(Comparator.comparing(Afastamento::getDataInicio))
                .map(a -> ausencia(a, lotacoes.get(a.getProfissional().getUuid()))).toList();

        return new EscalaSemanaResponseDto(unidade.getUuid(), unidade.getNome(), unidade.isFunciona24h(), seg, seg.plusDays(6),
                horas(daUnidade.stream().filter(t -> t.getTipo().somaNaJornada()).mapToLong(TurnoEscala::minutos).sum()),
                linhas.size(), daUnidade.size(), (int) daUnidade.stream().filter(TurnoEscala::vaga).count(),
                (int) daUnidade.stream().filter(t -> t.getTipo().plantao()).count(),
                (int) linhas.stream().filter(x -> !x.getAusencias().isEmpty()).count(),
                (int) linhas.stream().filter(x -> !x.getAlertas().isEmpty()).count(),
                linhas, daUnidade.stream().filter(TurnoEscala::vaga).map(t -> dto(t, List.of())).toList(), proximas);
    }

    // ── Escrita ────────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public TurnoEscalaDto criar(TurnoEscalaRequestDto dto) {
        UnidadeDeSaude unidade = buscarUnidade(dto.getUnidadeId());
        controleDeAcesso.exigir(GERENCIAR, unidade);
        TurnoEscala t = new TurnoEscala();
        t.setUnidade(unidade);
        aplicar(t, dto);
        String matricula = dto.getProfissionalMatricula() == null || dto.getProfissionalMatricula().isBlank() ? null : dto.getProfissionalMatricula().trim();
        t.setProfissional(matricula == null ? null : buscarProfissional(matricula));
        if (t.vaga() && t.getFuncao() == null) {
            throw new ResourceBadRequestException("A vaga aberta precisa da função que falta.");
        }
        if (t.getInicioEm().toLocalDate().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("A escala é planejada: não se cria turno em dia que já passou.");
        }
        completarFuncao(t);
        validar(t);
        t.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        t = turnoRepository.save(t);
        ContextoAuditoria.unidade(unidade);
        ContextoAuditoria.registro(t.getUuid());
        return dto(t, alertas(t));
    }

    /** Edita tipo, data, horário, equipe, função e descrição. A unidade fica; a troca de profissional é pela designação. */
    @Transactional
    public TurnoEscalaDto atualizar(UUID uuid, TurnoEscalaRequestDto dto) {
        TurnoEscala t = buscar(uuid);
        controleDeAcesso.exigir(GERENCIAR, t.getUnidade());
        exigirNaoTerminado(t);
        if (!dto.getUnidadeId().equals(t.getUnidade().getUuid())) {
            throw new ResourceUnprocessableEntityException("A unidade do turno não muda pela edição.");
        }
        String matricula = dto.getProfissionalMatricula() == null || dto.getProfissionalMatricula().isBlank() ? null : dto.getProfissionalMatricula().trim();
        String atual = t.vaga() ? null : t.getProfissional().getMatricula();
        if (!Objects.equals(matricula, atual)) {
            throw new ResourceUnprocessableEntityException("Para trocar o profissional do turno, use a designação.");
        }
        aplicar(t, dto);
        if (t.vaga() && t.getFuncao() == null) {
            throw new ResourceBadRequestException("A vaga aberta precisa da função que falta.");
        }
        if (t.getInicioEm().toLocalDate().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("O turno não pode ir para um dia que já passou.");
        }
        validar(t);
        ContextoAuditoria.registro(t.getUuid());
        return dto(turnoRepository.save(t), alertas(t));
    }

    /** Preenche a vaga ou troca o profissional do turno, com as mesmas regras do cadastro. */
    @Transactional
    public TurnoEscalaDto designar(UUID uuid, DesignarTurnoRequestDto dto) {
        TurnoEscala t = buscar(uuid);
        controleDeAcesso.exigir(GERENCIAR, t.getUnidade());
        exigirNaoTerminado(t);
        Profissional novo = buscarProfissional(dto.getProfissionalMatricula().trim());
        Profissional anterior = t.getProfissional();
        if (anterior != null && anterior.getUuid().equals(novo.getUuid())) {
            throw new ResourceConflictException(novo.getNome() + " já está neste turno.");
        }
        t.setProfissional(novo);
        completarFuncao(t);
        validar(t);
        turnoRepository.save(t);
        String motivo = dto.getMotivo() == null || dto.getMotivo().isBlank() ? "" : " Motivo: " + dto.getMotivo().trim();
        ContextoAuditoria.registro(t.getUuid());
        ContextoAuditoria.detalhe(anterior == null ? "Vaga preenchida por " + novo.getMatricula() + "." + motivo
                : "Troca de " + anterior.getMatricula() + " por " + novo.getMatricula() + "." + motivo);
        return dto(t, alertas(t));
    }

    /** Remove um turno que ainda não começou. */
    @Transactional
    public void remover(UUID uuid) {
        TurnoEscala t = buscar(uuid);
        controleDeAcesso.exigir(GERENCIAR, t.getUnidade());
        if (!t.getInicioEm().isAfter(LocalDateTime.now())) {
            throw new ResourceUnprocessableEntityException("O turno já começou; fica no registro da escala.");
        }
        ContextoAuditoria.registro(t.getUuid());
        turnoRepository.delete(t);
    }

    /**
     * Copia os turnos da semana de origem para a de destino, no mesmo dia da semana e horário. Os que caem em dia passado ou
     * quebram uma regra (afastamento, lotação, sobreposição) ficam de fora e voltam com o motivo; as vagas são copiadas como
     * vagas.
     */
    @Transactional
    public CopiaSemanaResponseDto copiarSemana(CopiarSemanaRequestDto dto) {
        UnidadeDeSaude unidade = buscarUnidade(dto.getUnidadeId());
        controleDeAcesso.exigir(GERENCIAR, unidade);
        LocalDate origem = segunda(dto.getOrigem());
        LocalDate destino = segunda(dto.getDestino());
        if (origem.equals(destino)) {
            throw new ResourceBadRequestException("A semana de destino precisa ser outra.");
        }
        long dias = ChronoUnit.DAYS.between(origem, destino);
        int copiados = 0;
        List<CopiaSemanaResponseDto.Ignorado> ignorados = new ArrayList<>();
        for (TurnoEscala o : turnoRepository.daUnidadeEntre(unidade.getUuid(), origem.atStartOfDay(), origem.plusDays(7).atStartOfDay())) {
            TurnoEscala t = new TurnoEscala(null, unidade, o.getEquipe(), o.getProfissional(), o.getFuncao(), o.getTipo(),
                    o.getInicioEm().plusDays(dias), o.getFimEm().plusDays(dias), o.getDescricao(), UsuarioAutenticado.cpf());
            String nome = t.vaga() ? null : t.getProfissional().getNome();
            if (t.getInicioEm().toLocalDate().isBefore(LocalDate.now())) {
                ignorados.add(new CopiaSemanaResponseDto.Ignorado(t.getInicioEm().toLocalDate(), nome, "O dia já passou."));
                continue;
            }
            try {
                validar(t);
            } catch (ResourceUnprocessableEntityException | ResourceConflictException | ResourceBadRequestException e) {
                ignorados.add(new CopiaSemanaResponseDto.Ignorado(t.getInicioEm().toLocalDate(), nome, e.getMessage()));
                continue;
            }
            turnoRepository.save(t);
            copiados++;
        }
        ContextoAuditoria.unidade(unidade);
        ContextoAuditoria.detalhe("Cópia da semana de " + origem + " para " + destino + ": " + copiados + " turno(s), "
                + ignorados.size() + " de fora.");
        return new CopiaSemanaResponseDto(copiados, ignorados);
    }

    // ── Regras ─────────────────────────────────────────────────────────────────────────────────────

    private void aplicar(TurnoEscala t, TurnoEscalaRequestDto dto) {
        t.setTipo(dto.getTipo());
        t.setEquipe(dto.getEquipeId() == null ? null : equipeDaUnidade(dto.getEquipeId(), t.getUnidade()));
        t.setFuncao(dto.getFuncao());
        LocalDateTime inicio = dto.getData().atTime(dto.getInicio());
        LocalDateTime fim = dto.getFim().isAfter(dto.getInicio()) ? dto.getData().atTime(dto.getFim()) : dto.getData().plusDays(1).atTime(dto.getFim());
        t.setInicioEm(inicio);
        t.setFimEm(fim);
        t.setDescricao(dto.getDescricao() == null || dto.getDescricao().isBlank() ? null : dto.getDescricao().trim());
    }

    /** Sem função informada, vale a do profissional na equipe do turno. */
    private void completarFuncao(TurnoEscala t) {
        if (t.getFuncao() != null || t.vaga() || t.getEquipe() == null) {
            return;
        }
        membroRepository.findByProfissional_UuidAndFimIsNull(t.getProfissional().getUuid()).stream()
                .filter(m -> m.getEquipe().getUuid().equals(t.getEquipe().getUuid())).findFirst()
                .ifPresent(m -> t.setFuncao(m.getFuncao()));
    }

    private void validar(TurnoEscala t) {
        UnidadeDeSaude u = t.getUnidade();
        if (!u.isAtivo()) {
            throw new ResourceUnprocessableEntityException("A " + u.getNome() + " está inativa.");
        }
        if (u.getSituacaoOperacional() != null && u.getSituacaoOperacional().fechada()) {
            throw new ResourceUnprocessableEntityException("A " + u.getNome() + " está fechada (" + u.getSituacaoOperacional()
                    + "); não recebe escala.");
        }
        long minutos = t.minutos();
        if (t.getTipo().mesmoDia() && !t.getFimEm().toLocalDate().equals(t.getInicioEm().toLocalDate())) {
            throw new ResourceBadRequestException("O turno de " + rotulo(t) + " termina no mesmo dia em que começa.");
        }
        Integer exata = t.getTipo().duracaoExata();
        if (exata != null && minutos != exata) {
            throw new ResourceBadRequestException("O " + rotulo(t) + " dura " + exata / 60 + " horas.");
        }
        if (minutos > t.getTipo().duracaoMaxima()) {
            throw new ResourceBadRequestException("O turno de " + rotulo(t) + " vai até " + t.getTipo().duracaoMaxima() / 60 + " horas.");
        }
        if (t.getTipo().exige24h() && !u.isFunciona24h()) {
            throw new ResourceUnprocessableEntityException("Plantão e turno da noite só em unidade 24 horas; a " + u.getNome()
                    + " não funciona 24 horas.");
        }
        if (t.getTipo().dentroDoHorario() && !u.isFunciona24h()) {
            caberNoHorario(t);
        }
        if (t.getEquipe() != null && !t.getEquipe().isAtiva()) {
            throw new ResourceUnprocessableEntityException("A equipe " + t.getEquipe().getNome() + " está inativa.");
        }
        if (!t.vaga()) {
            validarProfissional(t);
        }
    }

    /** O turno cabe entre a primeira abertura e o último fechamento do dia (o intervalo de almoço da unidade não conta). */
    private void caberNoHorario(TurnoEscala t) {
        List<HorarioUnidade> horarios = horarioRepository.findByUnidade_UuidOrderByDiaSemanaAscAbreAsc(t.getUnidade().getUuid());
        if (horarios.isEmpty()) {
            return;
        }
        DayOfWeek dia = t.getInicioEm().getDayOfWeek();
        List<HorarioUnidade> doDia = horarios.stream().filter(h -> h.getDiaSemana() == dia).toList();
        String nomeDia = dia.getDisplayName(TextStyle.FULL, PT_BR);
        if (doDia.isEmpty()) {
            throw new ResourceUnprocessableEntityException("A " + t.getUnidade().getNome() + " não abre " + nomeDia + ".");
        }
        LocalTime abre = doDia.stream().map(HorarioUnidade::getAbre).min(Comparator.naturalOrder()).orElseThrow();
        LocalTime fecha = doDia.stream().map(HorarioUnidade::getFecha).max(Comparator.naturalOrder()).orElseThrow();
        if (t.getInicioEm().toLocalTime().isBefore(abre) || t.getFimEm().toLocalTime().isAfter(fecha)) {
            throw new ResourceUnprocessableEntityException("O turno precisa caber no horário da unidade: " + abre.format(HORA) + " às "
                    + fecha.format(HORA) + " (" + nomeDia + ").");
        }
    }

    private void validarProfissional(TurnoEscala t) {
        Profissional p = t.getProfissional();
        LocalDate data = t.getInicioEm().toLocalDate();
        if (!p.isAtivo()) {
            throw new ResourceUnprocessableEntityException(p.getNome() + " está desligado.");
        }
        boolean lotado = lotacaoRepository.findByProfissional_MatriculaOrderByDataInicioDesc(p.getMatricula()).stream()
                .anyMatch(l -> l.getUnidade() != null && l.getUnidade().getUuid().equals(t.getUnidade().getUuid())
                        && !l.getDataInicio().isAfter(data) && (l.getDataFim() == null || !l.getDataFim().isBefore(data)));
        if (!lotado) {
            throw new ResourceUnprocessableEntityException(p.getNome() + " não tem lotação na " + t.getUnidade().getNome() + " em "
                    + data.format(DIA) + ".");
        }
        LocalDate ultimoDia = t.getFimEm().toLocalDate();
        afastamentoRepository.findByProfissional_MatriculaOrderByDataInicioDesc(p.getMatricula()).stream()
                .filter(a -> AFASTA.contains(a.getStatus()) && !a.getDataInicio().isAfter(ultimoDia) && !a.getDataFim().isBefore(data))
                .findFirst().ifPresent(a -> {
                    throw new ResourceUnprocessableEntityException(p.getNome() + " está " + situacao(a.getTipo()) + " de "
                            + a.getDataInicio().format(DIA) + " a " + a.getDataFim().format(DIA) + " (RH).");
                });
        turnoRepository.dosProfissionaisEntre(List.of(p.getUuid()), t.getInicioEm(), t.getFimEm()).stream()
                .filter(o -> !Objects.equals(o.getUuid(), t.getUuid())).findFirst().ifPresent(o -> {
                    throw new ResourceConflictException(p.getNome() + " já tem turno das " + o.getInicioEm().format(HORA) + " às "
                            + o.getFimEm().format(HORA) + " em " + o.getInicioEm().format(DIA) + " na " + o.getUnidade().getNome() + ".");
                });
    }

    /** Alertas do turno salvo: os da semana de quem está nele e, com equipe, se a pessoa não é membro dela. */
    private List<String> alertas(TurnoEscala t) {
        if (t.vaga()) {
            return List.of();
        }
        Profissional p = t.getProfissional();
        LocalDate seg = segunda(t.getInicioEm().toLocalDate());
        List<TurnoEscala> todos = turnoRepository.dosProfissionaisEntre(List.of(p.getUuid()), seg.minusDays(1).atStartOfDay(),
                seg.plusDays(8).atStartOfDay());
        Integer jornada = lotacaoRepository.findByProfissional_MatriculaOrderByDataInicioDesc(p.getMatricula()).stream()
                .filter(l -> l.getDataFim() == null && l.getUnidade() != null && l.getUnidade().getUuid().equals(t.getUnidade().getUuid()))
                .map(Lotacao::getJornadaSemanalHoras).filter(Objects::nonNull).findFirst().orElse(null);
        List<String> alertas = new ArrayList<>(alertasDaSemana(todos, jornada, seg));
        if (t.getEquipe() != null && membroRepository.findByProfissional_UuidAndFimIsNull(p.getUuid()).stream()
                .noneMatch(m -> m.getEquipe().getUuid().equals(t.getEquipe().getUuid()))) {
            alertas.add(p.getNome() + " não é membro da equipe " + t.getEquipe().getNome() + ".");
        }
        return alertas;
    }

    /** Jornada da semana acima da contratada e descanso menor que 11 horas entre turnos que tocam a semana. */
    private List<String> alertasDaSemana(List<TurnoEscala> turnos, Integer jornada, LocalDate seg) {
        LocalDateTime de = seg.atStartOfDay();
        LocalDateTime ate = seg.plusDays(7).atStartOfDay();
        Set<String> alertas = new LinkedHashSet<>();
        long minutos = turnos.stream().filter(t -> t.getTipo().somaNaJornada())
                .filter(t -> !t.getInicioEm().isBefore(de) && t.getInicioEm().isBefore(ate)).mapToLong(TurnoEscala::minutos).sum();
        if (jornada != null && minutos > jornada * 60L) {
            alertas.add("Semana com " + rotuloHoras(minutos) + ", acima da jornada contratada de " + jornada + "h.");
        }
        List<TurnoEscala> trabalho = turnos.stream().filter(t -> t.getTipo().somaNaJornada())
                .sorted(Comparator.comparing(TurnoEscala::getInicioEm)).toList();
        for (int i = 1; i < trabalho.size(); i++) {
            TurnoEscala a = trabalho.get(i - 1);
            TurnoEscala b = trabalho.get(i);
            boolean tocaSemana = b.getInicioEm().isBefore(ate) && !a.getFimEm().isBefore(de);
            long descanso = ChronoUnit.MINUTES.between(a.getFimEm(), b.getInicioEm());
            if (tocaSemana && descanso >= 0 && descanso < DESCANSO_MINIMO) {
                alertas.add("Descanso de " + rotuloHoras(descanso) + " entre " + a.getFimEm().format(DIA) + " e "
                        + b.getInicioEm().format(DIA) + " (mínimo de 11h).");
            }
        }
        return List.copyOf(alertas);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private TurnoEscalaDto dto(TurnoEscala t, List<String> alertas) {
        Profissional p = t.getProfissional();
        return new TurnoEscalaDto(t.getUuid(), t.getUnidade().getUuid(), t.getUnidade().getNome(),
                t.getEquipe() != null ? t.getEquipe().getUuid() : null, t.getEquipe() != null ? t.getEquipe().getNome() : null,
                p != null ? p.getMatricula() : null, p != null ? p.getNome() : null, t.getFuncao(), t.getTipo(),
                t.getInicioEm().toLocalDate(), t.getInicioEm(), t.getFimEm(), horas(t.minutos()), t.getDescricao(), t.vaga(), alertas);
    }

    private EscalaSemanaResponseDto.Ausencia ausencia(Afastamento a, Lotacao l) {
        Profissional p = a.getProfissional();
        return new EscalaSemanaResponseDto.Ausencia(p.getMatricula(), p.getNome(), l != null && l.getCargo() != null ? l.getCargo().getNome() : null,
                a.getTipo(), a.getDataInicio(), a.getDataFim());
    }

    private static String conselho(Profissional p) {
        return p.getConselhoClasse() == null ? null : p.getConselhoClasse() + " " + Objects.toString(p.getNumeroConselho(), "");
    }

    /** Sem revelar o motivo de saúde: licença médica aparece só como licença. */
    private static String situacao(TipoAfastamento tipo) {
        return switch (tipo) {
            case FERIAS -> "em férias";
            case LICENCA_MEDICA, LICENCA_PESSOAL -> "de licença";
            case OUTROS -> "afastado";
        };
    }

    private static String rotulo(TurnoEscala t) {
        return switch (t.getTipo()) {
            case MANHA -> "manhã";
            case TARDE -> "tarde";
            case NOITE -> "noite";
            case PLANTAO_12H -> "plantão de 12 horas";
            case PLANTAO_24H -> "plantão de 24 horas";
            case SOBREAVISO -> "sobreaviso";
            case CAPACITACAO -> "capacitação";
        };
    }

    private static double horas(long minutos) {
        return Math.round(minutos / 6.0) / 10.0;
    }

    private static String rotuloHoras(long minutos) {
        return minutos % 60 == 0 ? minutos / 60 + "h" : minutos / 60 + "h" + String.format("%02d", minutos % 60);
    }

    private static LocalDate segunda(LocalDate dia) {
        return dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private void exigirNaoTerminado(TurnoEscala t) {
        if (!t.getFimEm().isAfter(LocalDateTime.now())) {
            throw new ResourceUnprocessableEntityException("O turno já terminou; fica no registro da escala.");
        }
    }

    private Equipe equipeDaUnidade(UUID equipeId, UnidadeDeSaude unidade) {
        Equipe e = equipeRepository.findById(equipeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma equipe com o id " + equipeId + " em nossos registros."));
        if (!e.getUnidade().getUuid().equals(unidade.getUuid())) {
            throw new ResourceUnprocessableEntityException("A equipe " + e.getNome() + " é de outra unidade.");
        }
        return e;
    }

    private UnidadeDeSaude buscarUnidade(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }

    private Profissional buscarProfissional(String matricula) {
        return profissionalRepository.findByMatricula(matricula).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    private TurnoEscala buscar(UUID uuid) {
        return turnoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o turno " + uuid + " em nossos registros."));
    }
}
