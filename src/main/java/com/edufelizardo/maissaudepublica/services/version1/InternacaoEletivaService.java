package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.EventoInternacaoAgendada;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.InternacaoAgendada;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AgendamentoInternacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EletivaLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InternacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EletivaCensoDtos;
import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacaoAgendada;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoInternacaoAgendada;
import com.edufelizardo.maissaudepublica.repositories.EventoInternacaoAgendadaRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoAgendadaRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Internação eletiva agendada (ADR-0185): o agendamento com a data prevista, a reserva do leito da própria unidade a partir de dois
 * dias antes (vale até o fim do dia previsto), a admissão, a falta, a remarcação e o cancelamento. A reserva que passa do dia
 * previsto conta como livre na consulta, sem rotina, como na central de leitos (ADR-0184).
 */
@Service
public class InternacaoEletivaService {

    static final int DIAS_ANTES_PARA_RESERVAR = 2;
    private static final Set<StatusInternacaoAgendada> ATIVOS = Set.of(StatusInternacaoAgendada.AGENDADA, StatusInternacaoAgendada.RESERVADA);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private InternacaoAgendadaRepository agendamentoRepository;

    @Autowired
    private EventoInternacaoAgendadaRepository eventoRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private LeitoService leitoService;

    @Autowired
    private InternacaoService internacaoService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Transactional
    public EletivaCensoDtos.Agendamento agendar(AgendamentoInternacaoRequestDto dto) {
        UnidadeDeSaude unidade = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(InternacaoService.GERENCIAR, unidade);
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + dto.getPacienteId() + " em nossos registros."));
        ContextoAuditoria.paciente(paciente.getUuid());
        if (!paciente.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O paciente está inativo; reative o cadastro antes de agendar a internação.");
        }
        if (dto.getDataPrevista().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("A data prevista não pode ser no passado.");
        }
        if (agendamentoRepository.existsByPaciente_UuidAndStatusIn(paciente.getUuid(), ATIVOS)) {
            throw new ResourceConflictException("O paciente já tem uma internação eletiva agendada; remarque ou cancele a que existe.");
        }
        Profissional medico = profissional(dto.getMedicoMatricula());
        InternacaoAgendada a = new InternacaoAgendada();
        a.setPaciente(paciente);
        a.setUnidade(unidade);
        a.setDataPrevista(dto.getDataPrevista());
        a.setTipoLeito(dto.getTipoLeito());
        a.setMedico(medico);
        a.setCid(dto.getCid().trim().toUpperCase(Locale.ROOT).replace(".", ""));
        a.setProcedimento(dto.getProcedimento().trim());
        a.setPrevisaoDias(dto.getPrevisaoDias());
        a.setSolicitacaoRegulacaoId(dto.getSolicitacaoRegulacaoId());
        a.setStatus(StatusInternacaoAgendada.AGENDADA);
        a.setAgendadaEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        a = agendamentoRepository.save(a);
        registrar(a, TipoEventoInternacaoAgendada.AGENDAMENTO, "Para " + DATA.format(a.getDataPrevista()) + " · " + a.getProcedimento(), medico);
        return detalhe(a);
    }

    /** As eletivas das unidades do escopo entre duas datas (padrão: de hoje a seis dias), e as passadas ainda sem desfecho. */
    @Transactional(readOnly = true)
    public List<EletivaCensoDtos.Agendamento> agenda(UUID unidadeId, LocalDate de, LocalDate ate) {
        LocalDate inicio = de != null ? de : LocalDate.now();
        LocalDate fim = ate != null ? ate : inicio.plusDays(6);
        if (fim.isBefore(inicio) || fim.isAfter(inicio.plusDays(62))) {
            throw new ResourceBadRequestException("O período vai de 1 a 62 dias.");
        }
        Stream<InternacaoAgendada> doPeriodo = agendamentoRepository.findByDataPrevistaBetweenOrderByDataPrevistaAscAgendadaEmAsc(inicio, fim).stream();
        Stream<InternacaoAgendada> semDesfecho = agendamentoRepository.findByStatusInAndDataPrevistaBeforeOrderByDataPrevistaAsc(ATIVOS, inicio).stream();
        Instant agora = Instant.now();
        return controleDeAcesso.filtrar(Stream.concat(semDesfecho, doPeriodo), a -> Stream.of(a.getUnidade()), LeitoService.VER)
                .filter(a -> unidadeId == null || a.getUnidade().getUuid().equals(unidadeId))
                // Os sem desfecho são anteriores ao período: não se repetem.
                .map(a -> resumo(a, agora)).toList();
    }

    @Transactional(readOnly = true)
    public EletivaCensoDtos.Agendamento buscarPorId(UUID uuid) {
        InternacaoAgendada a = agendamentoRepository.findById(uuid).orElseThrow(() -> naoEncontrado(uuid));
        controleDeAcesso.exigirVisivel(a.getUnidade(), LeitoService.VER);
        return detalhe(a);
    }

    /**
     * Reserva um leito livre e compatível da própria unidade, a partir de dois dias antes da data prevista e até ela (422 fora da
     * janela). A reserva vale até o fim do dia previsto.
     */
    @Transactional
    public EletivaCensoDtos.Agendamento reservar(UUID uuid, EletivaLeitoRequestDto dto) {
        InternacaoAgendada a = travar(uuid);
        controleDeAcesso.exigir(InternacaoService.GERENCIAR, a.getUnidade());
        Instant agora = Instant.now();
        liberarSeVencida(a, agora);
        if (a.getStatus() != StatusInternacaoAgendada.AGENDADA) {
            throw new ResourceUnprocessableEntityException("Só eletiva agendada sem leito pode ser reservada; esta está " + a.getStatus() + ".");
        }
        LocalDate hoje = LocalDate.now();
        if (hoje.isBefore(a.getDataPrevista().minusDays(DIAS_ANTES_PARA_RESERVAR)) || hoje.isAfter(a.getDataPrevista())) {
            throw new ResourceUnprocessableEntityException("A reserva abre " + DIAS_ANTES_PARA_RESERVAR + " dias antes da data prevista ("
                    + DATA.format(a.getDataPrevista()) + ") e vai até ela.");
        }
        if (dto.getLeitoId() == null) {
            throw new ResourceBadRequestException("Informe o leito a reservar.");
        }
        Leito leito = leitoService.travar(dto.getLeitoId());
        leitoService.liberarReservaVencida(leito);
        if (!leito.getUnidade().getUuid().equals(a.getUnidade().getUuid())) {
            throw new ResourceUnprocessableEntityException("O leito precisa ser da unidade da internação eletiva.");
        }
        exigirCompativel(leito, a, agora);
        Profissional quem = profissional(dto.getProfissionalMatricula());
        Instant ate = a.getDataPrevista().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusSeconds(1);
        leito.setSituacao(SituacaoLeito.RESERVADO);
        leito.setReservadoAte(ate);
        leito.setReservaAgendamentoId(a.getUuid());
        leitoRepository.save(leito);
        a.setStatus(StatusInternacaoAgendada.RESERVADA);
        a.setLeitoReservado(leito);
        a.setReservadoAte(ate);
        registrar(a, TipoEventoInternacaoAgendada.RESERVA, "Leito " + leito.getIdentificacao(), quem);
        return detalhe(agendamentoRepository.save(a));
    }

    /** Na chegada: interna no leito reservado; sem reserva, num leito livre compatível da unidade (400 sem ele). */
    @Transactional
    public EletivaCensoDtos.Agendamento admitir(UUID uuid, EletivaLeitoRequestDto dto) {
        InternacaoAgendada a = travar(uuid);
        controleDeAcesso.exigir(InternacaoService.GERENCIAR, a.getUnidade());
        Instant agora = Instant.now();
        liberarSeVencida(a, agora);
        if (!ATIVOS.contains(a.getStatus())) {
            throw new ResourceUnprocessableEntityException("Não é possível admitir uma eletiva " + a.getStatus() + ".");
        }
        Leito leito;
        if (a.getStatus() == StatusInternacaoAgendada.RESERVADA) {
            leito = leitoService.travar(a.getLeitoReservado().getUuid());
        } else {
            if (dto.getLeitoId() == null) {
                throw new ResourceBadRequestException("Sem leito reservado: informe o leito livre onde o paciente fica.");
            }
            leito = leitoService.travar(dto.getLeitoId());
            leitoService.liberarReservaVencida(leito);
            if (!leito.getUnidade().getUuid().equals(a.getUnidade().getUuid())) {
                throw new ResourceUnprocessableEntityException("O leito precisa ser da unidade da internação eletiva.");
            }
        }
        InternacaoRequestDto internacao = new InternacaoRequestDto();
        internacao.setPacienteId(a.getPaciente().getUuid());
        internacao.setLeitoId(leito.getUuid());
        internacao.setMedicoMatricula(a.getMedico().getMatricula());
        internacao.setCid(a.getCid());
        internacao.setMotivo("Internação eletiva: " + a.getProcedimento());
        internacao.setCarater(CaraterInternacao.ELETIVA);
        internacao.setPrevisaoAlta(dto.getPrevisaoAlta() != null ? dto.getPrevisaoAlta()
                : a.getPrevisaoDias() != null ? LocalDate.now().plusDays(a.getPrevisaoDias()) : null);
        internacao.setJustificativaFaixaEtaria(dto.getJustificativaFaixaEtaria());
        Internacao nova = internacaoService.internarAgendada(internacao, leito, a.getUuid());
        Profissional quem = profissional(dto.getProfissionalMatricula());
        a.setStatus(StatusInternacaoAgendada.ADMITIDA);
        a.setInternacao(nova);
        registrar(a, TipoEventoInternacaoAgendada.ADMISSAO, "Leito " + leito.getIdentificacao(), quem);
        return detalhe(agendamentoRepository.save(a));
    }

    /** O paciente não veio: com motivo, a partir do dia previsto (422 antes). O leito reservado volta a livre. */
    @Transactional
    public EletivaCensoDtos.Agendamento registrarFalta(UUID uuid, EletivaLeitoRequestDto dto) {
        InternacaoAgendada a = encerrando(uuid, dto, "registrar a falta de");
        if (LocalDate.now().isBefore(a.getDataPrevista())) {
            throw new ResourceUnprocessableEntityException("A falta só é registrada a partir do dia previsto.");
        }
        return encerrar(a, StatusInternacaoAgendada.FALTA, TipoEventoInternacaoAgendada.FALTA, dto);
    }

    @Transactional
    public EletivaCensoDtos.Agendamento cancelar(UUID uuid, EletivaLeitoRequestDto dto) {
        return encerrar(encerrando(uuid, dto, "cancelar"), StatusInternacaoAgendada.CANCELADA, TipoEventoInternacaoAgendada.CANCELAMENTO, dto);
    }

    /** Nova data, com motivo: desfaz a reserva, se houver. A data não pode ser no passado (422). */
    @Transactional
    public EletivaCensoDtos.Agendamento remarcar(UUID uuid, EletivaLeitoRequestDto dto) {
        InternacaoAgendada a = encerrando(uuid, dto, "remarcar");
        if (dto.getDataPrevista() == null) {
            throw new ResourceBadRequestException("Informe a nova data prevista.");
        }
        if (dto.getDataPrevista().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("A nova data não pode ser no passado.");
        }
        Profissional quem = profissional(dto.getProfissionalMatricula());
        LocalDate antiga = a.getDataPrevista();
        desfazerReserva(a);
        a.setStatus(StatusInternacaoAgendada.AGENDADA);
        a.setDataPrevista(dto.getDataPrevista());
        registrar(a, TipoEventoInternacaoAgendada.REMARCACAO, "De " + DATA.format(antiga) + " para " + DATA.format(dto.getDataPrevista()) + ": "
                + dto.getMotivo().trim(), quem);
        return detalhe(agendamentoRepository.save(a));
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private InternacaoAgendada encerrando(UUID uuid, EletivaLeitoRequestDto dto, String acao) {
        InternacaoAgendada a = travar(uuid);
        controleDeAcesso.exigir(InternacaoService.GERENCIAR, a.getUnidade());
        if (!ATIVOS.contains(a.getStatus())) {
            throw new ResourceUnprocessableEntityException("Não é possível " + acao + " uma eletiva " + a.getStatus() + ".");
        }
        if (dto.getMotivo() == null || dto.getMotivo().isBlank()) {
            throw new ResourceBadRequestException("Informe o motivo.");
        }
        return a;
    }

    private EletivaCensoDtos.Agendamento encerrar(InternacaoAgendada a, StatusInternacaoAgendada status, TipoEventoInternacaoAgendada tipo,
                                                  EletivaLeitoRequestDto dto) {
        Profissional quem = profissional(dto.getProfissionalMatricula());
        desfazerReserva(a);
        a.setStatus(status);
        a.setMotivoEncerramento(dto.getMotivo().trim());
        registrar(a, tipo, dto.getMotivo().trim(), quem);
        return detalhe(agendamentoRepository.save(a));
    }

    /** Leito ativo, livre agora, do tipo pedido, com sexo e faixa etária do paciente (422 com o motivo). */
    private void exigirCompativel(Leito l, InternacaoAgendada a, Instant agora) {
        if (!l.isAtivo() || l.situacaoEm(agora) != SituacaoLeito.LIVRE) {
            throw new ResourceUnprocessableEntityException("O leito " + l.getIdentificacao() + " não está livre.");
        }
        if (l.getTipo() != a.getTipoLeito()) {
            throw new ResourceUnprocessableEntityException("O leito " + l.getIdentificacao() + " é " + l.getTipo() + "; a eletiva pede " + a.getTipoLeito() + ".");
        }
        if (l.getSexo() != SexoLeito.MISTO) {
            InternacaoService.exigirSexo(l, a.getPaciente());
        }
        InternacaoService.conferirFaixaEtaria(l, a.getPaciente(), null);
    }

    /** Reserva que passou do dia previsto: o leito volta a livre e o agendamento volta a agendado, aguardando o desfecho. */
    private void liberarSeVencida(InternacaoAgendada a, Instant agora) {
        if (a.reservaVencida(agora)) {
            desfazerReserva(a);
            a.setStatus(StatusInternacaoAgendada.AGENDADA);
            agendamentoRepository.save(a);
        }
    }

    private void desfazerReserva(InternacaoAgendada a) {
        if (a.getLeitoReservado() != null) {
            Leito l = leitoService.travar(a.getLeitoReservado().getUuid());
            if (l.reservadoPara(a.getUuid())) {
                l.setSituacao(SituacaoLeito.LIVRE);
                l.limparReserva();
                leitoRepository.save(l);
            }
        }
        a.setLeitoReservado(null);
        a.setReservadoAte(null);
    }

    private EletivaCensoDtos.Agendamento resumo(InternacaoAgendada a, Instant agora) {
        StatusInternacaoAgendada status = a.reservaVencida(agora) ? StatusInternacaoAgendada.AGENDADA : a.getStatus();
        LocalDate hoje = LocalDate.now();
        boolean ativo = ATIVOS.contains(a.getStatus());
        boolean aguardando = ativo && a.getDataPrevista().isBefore(hoje);
        boolean podeReservar = status == StatusInternacaoAgendada.AGENDADA && !hoje.isBefore(a.getDataPrevista().minusDays(DIAS_ANTES_PARA_RESERVAR))
                && !hoje.isAfter(a.getDataPrevista());
        return EletivaCensoDtos.Agendamento.from(a, status, aguardando, podeReservar, List.of());
    }

    private EletivaCensoDtos.Agendamento detalhe(InternacaoAgendada a) {
        ContextoAuditoria.paciente(a.getPaciente().getUuid());
        ContextoAuditoria.registro(a.getUuid());
        EletivaCensoDtos.Agendamento r = resumo(a, Instant.now());
        List<EventoInternacaoAgendada> eventos = eventoRepository.findByAgendamento_UuidOrderByOcorridoEmAsc(a.getUuid());
        return EletivaCensoDtos.Agendamento.from(a, r.status(), r.aguardandoDesfecho(), r.podeReservar(), eventos);
    }

    private void registrar(InternacaoAgendada a, TipoEventoInternacaoAgendada tipo, String texto, Profissional quem) {
        EventoInternacaoAgendada e = new EventoInternacaoAgendada();
        e.setAgendamento(a);
        e.setTipo(tipo);
        e.setTexto(texto != null && texto.length() > 1000 ? texto.substring(0, 1000) : texto);
        e.setProfissional(quem);
        e.setOcorridoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
    }

    private InternacaoAgendada travar(UUID uuid) {
        InternacaoAgendada a = agendamentoRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> naoEncontrado(uuid));
        ContextoAuditoria.paciente(a.getPaciente().getUuid());
        return a;
    }

    private Profissional profissional(String matricula) {
        return profissionalRepository.findByMatricula(matricula.trim()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + matricula.trim() + " em nossos registros."));
    }

    private static ResourceNotFoundException naoEncontrado(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar uma internação eletiva com o id " + uuid + " em nossos registros.");
    }
}
