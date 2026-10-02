package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.BlocoAgenda;
import com.edufelizardo.maissaudepublica.models.BloqueioAgenda;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.BlocoAgendaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.BloqueioAgendaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AgendaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.BlocoAgendaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.BloqueioAgendaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DiaAgendaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ItemAgendaDto;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOperacional;
import com.edufelizardo.maissaudepublica.repositories.AfastamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.BlocoAgendaRepository;
import com.edufelizardo.maissaudepublica.repositories.BloqueioAgendaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agenda do profissional (ADR-0091): blocos recorrentes por unidade, bloqueios e, na hora de montar a agenda, as
 * férias e os afastamentos aprovados do RH. Daí saem as vagas — o bloco fatiado em vagas, menos o que está
 * bloqueado e o que já tem marcação — e a regra de marcação: vaga livre, ou encaixe; nunca em bloqueio nem com o
 * profissional afastado.
 */
@Service
public class AgendaService {

    public static final String GERENCIAR = "AGENDAMENTO.GERENCIAR";
    static final String[] VER = {"AGENDAMENTO.GERENCIAR", "ATENDIMENTO.GERENCIAR", "REGULACAO.REGULAR"};
    /** Períodos maiores pesam demais para montar de uma vez. */
    private static final int MAX_DIAS = 62;
    private static final Set<StatusAfastamento> AFASTA = Set.of(StatusAfastamento.APROVADO, StatusAfastamento.EM_ANDAMENTO);

    @Autowired
    private BlocoAgendaRepository blocoRepository;

    @Autowired
    private BloqueioAgendaRepository bloqueioRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private AfastamentoRepository afastamentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private FuncionamentoUnidade funcionamentoUnidade;

    // ── Blocos ─────────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public BlocoAgendaResponseDto criarBloco(BlocoAgendaRequestDto dto) {
        UnidadeDeSaude unidade = buscarUnidade(dto.getUnidadeId());
        controleDeAcesso.exigir(GERENCIAR, unidade);
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        if (!dto.getHoraFim().isAfter(dto.getHoraInicio())) {
            throw new ResourceBadRequestException("O fim do bloco precisa ser depois do início.");
        }
        long minutos = Duration.between(dto.getHoraInicio(), dto.getHoraFim()).toMinutes();
        if (minutos < dto.getDuracaoMinutos()) {
            throw new ResourceBadRequestException("O bloco é menor que uma vaga de " + dto.getDuracaoMinutos() + " minutos.");
        }
        LocalDate desde = dto.getVigenteDesde() != null ? dto.getVigenteDesde() : LocalDate.now();
        if (dto.getVigenteAte() != null && dto.getVigenteAte().isBefore(desde)) {
            throw new ResourceBadRequestException("O fim da vigência precisa ser igual ou depois do início.");
        }
        FuncionamentoUnidade.Funcionamento funcionamento = funcionamentoUnidade.de(unidade);
        if (!funcionamento.semRestricao() && !funcionamento.aberta(proximo(dto.getDiaSemana()).atTime(dto.getHoraInicio()),
                proximo(dto.getDiaSemana()).atTime(dto.getHoraFim()))) {
            throw new ResourceUnprocessableEntityException("O bloco fica fora do horário de funcionamento da " + unidade.getNome()
                    + " em " + dto.getDiaSemana() + ".");
        }
        BlocoAgenda novo = new BlocoAgenda(profissional, unidade, dto.getDiaSemana(), dto.getHoraInicio(), dto.getHoraFim(),
                dto.getDuracaoMinutos(), dto.getTipo(), desde, dto.getVigenteAte());
        // O profissional não está em dois lugares ao mesmo tempo: nenhum outro bloco dele, em qualquer unidade,
        // no mesmo dia da semana, com horário e vigência que se cruzem.
        blocoRepository.findByProfissional_UuidOrderByDiaSemanaAscHoraInicioAsc(profissional.getUuid()).stream()
                .filter(b -> b.getDiaSemana() == novo.getDiaSemana())
                .filter(b -> b.getHoraInicio().isBefore(novo.getHoraFim()) && b.getHoraFim().isAfter(novo.getHoraInicio()))
                .filter(b -> vigenciasSeCruzam(b, novo))
                .findFirst()
                .ifPresent(b -> {
                    throw new ResourceConflictException("O profissional já tem um bloco na " + b.getUnidade().getNome() + " de "
                            + b.getHoraInicio() + " a " + b.getHoraFim() + " nesse dia da semana.");
                });
        return BlocoAgendaResponseDto.fromBloco(blocoRepository.save(novo));
    }

    /** Encerra o bloco: as vagas deixam de existir depois de {@code vigenteAte}; as marcações já feitas ficam. */
    @Transactional
    public BlocoAgendaResponseDto encerrarBloco(UUID uuid, LocalDate vigenteAte) {
        BlocoAgenda bloco = blocoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um bloco de agenda com o id " + uuid + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, bloco.getUnidade());
        if (vigenteAte.isBefore(bloco.getVigenteDesde().minusDays(1))) {
            throw new ResourceBadRequestException("O bloco começa em " + bloco.getVigenteDesde() + "; não pode terminar antes.");
        }
        if (bloco.getVigenteAte() != null && !vigenteAte.isBefore(bloco.getVigenteAte())) {
            throw new ResourceUnprocessableEntityException("O bloco já termina em " + bloco.getVigenteAte() + ".");
        }
        bloco.setVigenteAte(vigenteAte);
        return BlocoAgendaResponseDto.fromBloco(blocoRepository.save(bloco));
    }

    @Transactional(readOnly = true)
    public List<BlocoAgendaResponseDto> listarBlocos(String profissionalMatricula, UUID unidadeId) {
        List<BlocoAgenda> blocos;
        if (profissionalMatricula != null && !profissionalMatricula.isBlank()) {
            Profissional p = buscarProfissional(profissionalMatricula);
            blocos = unidadeId != null
                    ? blocoRepository.findByProfissional_UuidAndUnidade_UuidOrderByDiaSemanaAscHoraInicioAsc(p.getUuid(), unidadeId)
                    : blocoRepository.findByProfissional_UuidOrderByDiaSemanaAscHoraInicioAsc(p.getUuid());
        } else if (unidadeId != null) {
            blocos = blocoRepository.findByUnidade_UuidOrderByDiaSemanaAscHoraInicioAsc(unidadeId);
        } else {
            throw new ResourceBadRequestException("Informe o profissional ou a unidade.");
        }
        return controleDeAcesso.filtrar(blocos.stream(), b -> java.util.stream.Stream.of(b.getUnidade()), VER)
                .map(BlocoAgendaResponseDto::fromBloco).toList();
    }

    // ── Bloqueios ──────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public BloqueioAgendaResponseDto criarBloqueio(BloqueioAgendaRequestDto dto) {
        Profissional profissional = textoOuNulo(dto.getProfissionalMatricula()) == null ? null
                : buscarProfissional(dto.getProfissionalMatricula().trim());
        UnidadeDeSaude unidade = dto.getUnidadeId() == null ? null : buscarUnidade(dto.getUnidadeId());
        if (profissional == null && unidade == null) {
            throw new ResourceBadRequestException("Informe o profissional, a unidade ou os dois.");
        }
        if (!dto.getFim().isAfter(dto.getInicio())) {
            throw new ResourceBadRequestException("O fim do bloqueio precisa ser depois do início.");
        }
        controleDeAcesso.exigir(GERENCIAR, unidade);
        BloqueioAgenda salvo = bloqueioRepository.save(new BloqueioAgenda(profissional, unidade, dto.getInicio(), dto.getFim(),
                dto.getMotivo(), textoOuNulo(dto.getDescricao()), Instant.now(), UsuarioAutenticado.cpf()));
        return BloqueioAgendaResponseDto.fromBloqueio(salvo);
    }

    /** Bloqueios que tocam o período e valem para o profissional na unidade (dele ou da unidade inteira). */
    @Transactional(readOnly = true)
    public List<BloqueioAgendaResponseDto> listarBloqueios(String profissionalMatricula, UUID unidadeId, LocalDate de, LocalDate ate) {
        Profissional profissional = buscarProfissional(profissionalMatricula);
        UnidadeDeSaude unidade = buscarUnidade(unidadeId);
        controleDeAcesso.exigirVisivel(unidade, VER);
        exigirPeriodo(de, ate);
        return bloqueioRepository.findAplicaveis(profissional.getUuid(), unidade.getUuid(), de.atStartOfDay(), ate.plusDays(1).atStartOfDay())
                .stream().map(BloqueioAgendaResponseDto::fromBloqueio).toList();
    }

    @Transactional
    public void removerBloqueio(UUID uuid) {
        BloqueioAgenda bloqueio = bloqueioRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um bloqueio de agenda com o id " + uuid + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, bloqueio.getUnidade());
        ContextoAuditoria.registro(uuid);
        bloqueioRepository.delete(bloqueio);
    }

    // ── Agenda e vagas ─────────────────────────────────────────────────────────────────────────────

    /** A agenda dia a dia, com vagas, marcações, encaixes, bloqueios e afastamentos, e o resumo do período. */
    @Transactional(readOnly = true)
    public AgendaResponseDto agenda(String profissionalMatricula, UUID unidadeId, LocalDate de, LocalDate ate) {
        Profissional profissional = buscarProfissional(profissionalMatricula);
        UnidadeDeSaude unidade = buscarUnidade(unidadeId);
        controleDeAcesso.exigirVisivel(unidade, VER);
        exigirPeriodo(de, ate);
        Montagem m = montar(profissional, unidade, de, ate);

        int ofertadas = 0;
        int ocupadas = 0;
        List<DiaAgendaDto> dias = new ArrayList<>();
        for (LocalDate dia = de; !dia.isAfter(ate); dia = dia.plusDays(1)) {
            List<ItemAgendaDto> itens = new ArrayList<>();
            Optional<Afastamento> afastamento = m.afastamentoEm(dia);
            if (afastamento.isPresent()) {
                itens.add(new ItemAgendaDto("AFASTAMENTO", dia.atStartOfDay(), dia.plusDays(1).atStartOfDay(), null, null, null,
                        null, null, afastamento.get().getTipo().name()));
            }
            for (BloqueioAgenda b : m.bloqueios) {
                if (b.cobre(profissional.getUuid(), unidade.getUuid(), dia.atStartOfDay(), dia.plusDays(1).atStartOfDay())) {
                    itens.add(new ItemAgendaDto("BLOQUEIO", max(b.getInicio(), dia.atStartOfDay()), min(b.getFim(), dia.plusDays(1).atStartOfDay()),
                            null, null, null, null, null, b.getMotivo().name() + (b.getDescricao() != null ? ": " + b.getDescricao() : "")));
                }
            }
            List<Agendamento> doDia = m.agendamentosEm(dia);
            for (Vaga v : m.vagasEm(dia)) {
                Optional<Agendamento> marcado = doDia.stream()
                        .filter(a -> !a.isEncaixe() && a.getDataHora().equals(v.inicio)).findFirst();
                if (marcado.isPresent()) {
                    ofertadas++;
                    ocupadas++;
                    itens.add(item(marcado.get(), "MARCACAO", v.fim));
                } else if (!v.bloqueada) {
                    ofertadas++;
                    itens.add(new ItemAgendaDto("VAGA", v.inicio, v.fim, v.bloco.getTipo(), null, null, null, null, null));
                }
            }
            // Marcação fora das vagas (encaixe, ou anterior à agenda) também aparece.
            Set<LocalDateTime> inicios = m.vagasEm(dia).stream().map(v -> v.inicio).collect(Collectors.toSet());
            for (Agendamento a : doDia) {
                if (a.isEncaixe() || !inicios.contains(a.getDataHora())) {
                    itens.add(item(a, a.isEncaixe() ? "ENCAIXE" : "MARCACAO", a.getDataHora().plusMinutes(m.duracaoPadrao())));
                }
            }
            itens.sort(Comparator.comparing(ItemAgendaDto::getInicio));
            dias.add(new DiaAgendaDto(dia, itens));
        }
        List<Agendamento> todos = m.agendamentos;
        return new AgendaResponseDto(profissional.getMatricula(), profissional.getNome(), unidade.getUuid(), unidade.getNome(), de, ate,
                ofertadas, ocupadas, ofertadas == 0 ? 0 : Math.round(ocupadas * 100f / ofertadas),
                (int) todos.stream().filter(a -> a.getStatus() != StatusAgendamento.CANCELADO).count(),
                (int) todos.stream().filter(a -> a.isEncaixe() && a.getStatus() != StatusAgendamento.CANCELADO).count(),
                (int) todos.stream().filter(a -> a.getStatus() == StatusAgendamento.FALTOU).count(),
                dias, avisoDaUnidade(unidade));
    }

    /** Vagas livres a partir de agora: para a nova marcação e para a autorização da regulação. */
    @Transactional(readOnly = true)
    public List<ItemAgendaDto> vagas(String profissionalMatricula, UUID unidadeId, LocalDate de, LocalDate ate) {
        Profissional profissional = buscarProfissional(profissionalMatricula);
        UnidadeDeSaude unidade = buscarUnidade(unidadeId);
        controleDeAcesso.exigirVisivel(unidade, VER);
        exigirPeriodo(de, ate);
        Montagem m = montar(profissional, unidade, de, ate);
        LocalDateTime agora = LocalDateTime.now();
        List<ItemAgendaDto> livres = new ArrayList<>();
        for (LocalDate dia = de; !dia.isAfter(ate); dia = dia.plusDays(1)) {
            if (m.afastamentoEm(dia).isPresent()) {
                continue;
            }
            Set<LocalDateTime> ocupadas = m.agendamentosEm(dia).stream().filter(a -> !a.isEncaixe())
                    .map(Agendamento::getDataHora).collect(Collectors.toSet());
            for (Vaga v : m.vagasEm(dia)) {
                if (!v.bloqueada && !ocupadas.contains(v.inicio) && v.inicio.isAfter(agora)) {
                    livres.add(new ItemAgendaDto("VAGA", v.inicio, v.fim, v.bloco.getTipo(), null, null, null, null, null));
                }
            }
        }
        return livres;
    }

    /**
     * Regra de marcação com unidade (ADR-0091): nunca com o profissional afastado nem num bloqueio; fora disso, numa
     * vaga livre da agenda, ou como encaixe.
     */
    @Transactional(readOnly = true)
    public void exigirMarcacaoPossivel(Profissional profissional, UnidadeDeSaude unidade, LocalDateTime dataHora, boolean encaixe,
                                       UUID ignorarAgendamento) {
        LocalDate dia = dataHora.toLocalDate();
        Montagem m = montar(profissional, unidade, dia, dia);
        // Unidade fechada (em obra ou inoperante) não recebe marcação, nem encaixe (ADR-0101).
        if (m.funcionamento.fechada()) {
            throw new ResourceUnprocessableEntityException("A " + unidade.getNome() + " está " + m.funcionamento.situacao()
                    + (unidade.getMotivoSituacao() != null ? " (" + unidade.getMotivoSituacao() + ")" : "") + " e não recebe marcação.");
        }
        if (!m.funcionamento.aberta(dataHora, dataHora.plusMinutes(1))) {
            throw new ResourceUnprocessableEntityException("A " + unidade.getNome() + " não funciona nesse horário.");
        }
        m.afastamentoEm(dia).ifPresent(a -> {
            throw new ResourceUnprocessableEntityException("O profissional está afastado (" + a.getTipo() + ") de "
                    + a.getDataInicio() + " a " + a.getDataFim() + ".");
        });
        m.bloqueios.stream()
                .filter(b -> b.cobre(profissional.getUuid(), unidade.getUuid(), dataHora, dataHora.plusMinutes(1)))
                .findFirst()
                .ifPresent(b -> {
                    throw new ResourceUnprocessableEntityException("A agenda está bloqueada nesse horário (" + b.getMotivo() + ").");
                });
        if (encaixe) {
            return;
        }
        boolean temVaga = m.vagasEm(dia).stream().anyMatch(v -> v.inicio.equals(dataHora));
        boolean ocupada = m.agendamentosEm(dia).stream()
                .anyMatch(a -> !a.isEncaixe() && a.getDataHora().equals(dataHora) && !a.getUuid().equals(ignorarAgendamento));
        if (!temVaga || ocupada) {
            throw new ResourceUnprocessableEntityException((temVaga ? "Essa vaga já está ocupada" : "Esse horário não é uma vaga da agenda")
                    + " do profissional nessa unidade. Escolha uma vaga livre ou marque como encaixe.");
        }
    }

    /** O aviso da situação da unidade na agenda (ADR-0101): fechada, ou funcionando com restrição. Em operação, nulo. */
    private static String avisoDaUnidade(UnidadeDeSaude u) {
        if (u.getSituacaoOperacional() == null || u.getSituacaoOperacional() == SituacaoOperacional.EM_OPERACAO) {
            return null;
        }
        String texto = switch (u.getSituacaoOperacional()) {
            case EM_MANUTENCAO -> "Unidade em manutenção";
            case EM_OBRA -> "Unidade em obra: sem vagas nem marcação";
            case INOPERANTE -> "Unidade inoperante: sem vagas nem marcação";
            default -> "";
        };
        return texto + (u.getMotivoSituacao() != null ? " · " + u.getMotivoSituacao() : "")
                + (u.getPrevisaoRetorno() != null ? " · retorno previsto em " + u.getPrevisaoRetorno() : "") + ".";
    }

    /** O próximo dia com aquele dia da semana, para testar o horário de um bloco recorrente. */
    private static LocalDate proximo(java.time.DayOfWeek dia) {
        return LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(dia));
    }

    // ── Montagem ───────────────────────────────────────────────────────────────────────────────────

    private Montagem montar(Profissional profissional, UnidadeDeSaude unidade, LocalDate de, LocalDate ate) {
        LocalDateTime inicio = de.atStartOfDay();
        LocalDateTime fim = ate.plusDays(1).atStartOfDay();
        List<BlocoAgenda> blocos = blocoRepository
                .findByProfissional_UuidAndUnidade_UuidOrderByDiaSemanaAscHoraInicioAsc(profissional.getUuid(), unidade.getUuid());
        List<BloqueioAgenda> bloqueios = bloqueioRepository.findAplicaveis(profissional.getUuid(), unidade.getUuid(), inicio, fim);
        List<Afastamento> afastamentos = afastamentoRepository.findByProfissional_MatriculaOrderByDataInicioDesc(profissional.getMatricula())
                .stream().filter(a -> AFASTA.contains(a.getStatus())).toList();
        // A agenda é da unidade: entram as marcações nela e as antigas sem unidade.
        List<Agendamento> agendamentos = agendamentoRepository
                .findByProfissional_UuidAndDataHoraBetweenOrderByDataHoraAsc(profissional.getUuid(), inicio, fim.minusNanos(1)).stream()
                .filter(a -> a.getStatus() != StatusAgendamento.CANCELADO)
                .filter(a -> a.getUnidade() == null || a.getUnidade().getUuid().equals(unidade.getUuid()))
                .toList();
        return new Montagem(profissional.getUuid(), unidade.getUuid(), blocos, bloqueios, afastamentos, agendamentos,
                funcionamentoUnidade.de(unidade));
    }

    private record Vaga(BlocoAgenda bloco, LocalDateTime inicio, LocalDateTime fim, boolean bloqueada) {
    }

    private record Montagem(UUID profissionalId, UUID unidadeId, List<BlocoAgenda> blocos, List<BloqueioAgenda> bloqueios,
                            List<Afastamento> afastamentos, List<Agendamento> agendamentos,
                            FuncionamentoUnidade.Funcionamento funcionamento) {

        Optional<Afastamento> afastamentoEm(LocalDate dia) {
            return afastamentos.stream().filter(a -> !dia.isBefore(a.getDataInicio()) && !dia.isAfter(a.getDataFim())).findFirst();
        }

        List<Vaga> vagasEm(LocalDate dia) {
            boolean afastado = afastamentoEm(dia).isPresent();
            List<Vaga> vagas = new ArrayList<>();
            for (BlocoAgenda b : blocos) {
                if (!b.valeEm(dia)) {
                    continue;
                }
                for (LocalTime t = b.getHoraInicio(); !t.plusMinutes(b.getDuracaoMinutos()).isAfter(b.getHoraFim())
                        && t.plusMinutes(b.getDuracaoMinutos()).isAfter(t); t = t.plusMinutes(b.getDuracaoMinutos())) {
                    LocalDateTime ini = dia.atTime(t);
                    LocalDateTime fim = ini.plusMinutes(b.getDuracaoMinutos());
                    // Fora do horário da unidade, ou com a unidade fechada, a vaga não existe (ADR-0101).
                    if (!funcionamento.aberta(ini, fim)) {
                        continue;
                    }
                    boolean bloqueada = afastado || bloqueios.stream().anyMatch(x -> x.cobre(profissionalId, unidadeId, ini, fim));
                    vagas.add(new Vaga(b, ini, fim, bloqueada));
                }
            }
            vagas.sort(Comparator.comparing(Vaga::inicio));
            return vagas;
        }

        List<Agendamento> agendamentosEm(LocalDate dia) {
            return agendamentos.stream().filter(a -> a.getDataHora().toLocalDate().equals(dia)).toList();
        }

        /** Duração para desenhar marcação fora das vagas: a do primeiro bloco, ou 15 minutos. */
        int duracaoPadrao() {
            return blocos.stream().map(BlocoAgenda::getDuracaoMinutos).filter(Objects::nonNull).findFirst().orElse(15);
        }
    }

    private static ItemAgendaDto item(Agendamento a, String tipo, LocalDateTime fim) {
        return new ItemAgendaDto(tipo, a.getDataHora(), fim, a.getTipo(), a.getUuid(), a.getPaciente().getUuid(),
                a.getPaciente().getNome(), a.getStatus(), a.getObservacao());
    }

    private static boolean vigenciasSeCruzam(BlocoAgenda a, BlocoAgenda b) {
        LocalDate fimA = a.getVigenteAte() != null ? a.getVigenteAte() : LocalDate.MAX;
        LocalDate fimB = b.getVigenteAte() != null ? b.getVigenteAte() : LocalDate.MAX;
        return !a.getVigenteDesde().isAfter(fimB) && !b.getVigenteDesde().isAfter(fimA);
    }

    private static void exigirPeriodo(LocalDate de, LocalDate ate) {
        if (ate.isBefore(de)) {
            throw new ResourceBadRequestException("O fim do período precisa ser igual ou depois do início.");
        }
        if (Duration.between(de.atStartOfDay(), ate.atStartOfDay()).toDays() >= MAX_DIAS) {
            throw new ResourceBadRequestException("Consulte no máximo " + MAX_DIAS + " dias por vez.");
        }
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    Profissional buscarProfissional(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    UnidadeDeSaude buscarUnidade(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
