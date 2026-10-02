package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AgendamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AgendamentoResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD do Agendamento (Assistência — ver ADR-0039/ADR-0042). {@code profissionalMatricula} é
 * resolvido para o {@link Profissional} real na fronteira da API, mesmo padrão do
 * {@code AtendimentoService} (ver ADR-0034/ADR-0041).
 *
 * <p>Com {@code unidadeId}, a marcação segue a agenda do profissional na unidade (ADR-0091): vaga livre ou encaixe,
 * nunca em bloqueio nem com o profissional afastado. Sem unidade, segue como antes da agenda.
 */
@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private AgendaService agendaService;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    public AgendamentoResponseDto criar(AgendamentoRequestDto dto) {
        Paciente paciente = buscarPacientePorId(dto.getPacienteId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Agendamento agendamento = new Agendamento(paciente, profissional, dto.getDataHora(),
                dto.getStatus(), dto.getTipo(), dto.getObservacao());
        aplicarAgenda(agendamento, dto, null);
        agendamento = agendamentoRepository.save(agendamento);
        return AgendamentoResponseDto.fromAgendamento(agendamento);
    }

    public AgendamentoResponseDto atualizar(UUID uuid, AgendamentoRequestDto dto) {
        Agendamento agendamento = buscarEntidadePorId(uuid);
        Paciente paciente = buscarPacientePorId(dto.getPacienteId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setDataHora(dto.getDataHora());
        agendamento.setStatus(dto.getStatus());
        agendamento.setTipo(dto.getTipo());
        agendamento.setObservacao(dto.getObservacao());
        aplicarAgenda(agendamento, dto, uuid);
        agendamento = agendamentoRepository.save(agendamento);
        return AgendamentoResponseDto.fromAgendamento(agendamento);
    }

    public List<AgendamentoResponseDto> listar() {
        return agendamentoRepository.findAll()
                .stream()
                .map(AgendamentoResponseDto::fromAgendamento)
                .collect(Collectors.toList());
    }

    /**
     * Todos os agendamentos do paciente, do mais antigo ao mais recente — quem consome decide o
     * recorte (a tela de Pacientes, por exemplo, mostra só os futuros ainda não cancelados).
     */
    public List<AgendamentoResponseDto> listarPorPaciente(UUID pacienteId) {
        List<AgendamentoResponseDto> agendamentos = agendamentoRepository.findByPaciente_UuidOrderByDataHoraAsc(pacienteId)
                .stream()
                .map(AgendamentoResponseDto::fromAgendamento)
                .collect(Collectors.toList());
        if (agendamentos.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar agendamentos para o paciente de id " + pacienteId + " em nossos registros.");
        }
        return agendamentos;
    }

    /** O paciente não compareceu: a marcação fica registrada como falta (ADR-0091). */
    public AgendamentoResponseDto registrarFalta(UUID uuid) {
        Agendamento agendamento = buscarEntidadePorId(uuid);
        if (agendamento.getUnidade() != null) {
            controleDeAcesso.exigir("AGENDAMENTO.GERENCIAR", agendamento.getUnidade());
        }
        if (agendamento.getStatus() != StatusAgendamento.AGENDADO && agendamento.getStatus() != StatusAgendamento.CONFIRMADO) {
            throw new ResourceUnprocessableEntityException("Só é possível registrar falta num agendamento agendado ou confirmado; este está "
                    + agendamento.getStatus() + ".");
        }
        agendamento.setStatus(StatusAgendamento.FALTOU);
        return AgendamentoResponseDto.fromAgendamento(agendamentoRepository.save(agendamento));
    }

    /** Com unidade, a marcação precisa caber na agenda: vaga livre, ou encaixe. Cancelada não ocupa vaga. */
    private void aplicarAgenda(Agendamento agendamento, AgendamentoRequestDto dto, UUID proprio) {
        boolean encaixe = Boolean.TRUE.equals(dto.getEncaixe());
        agendamento.setEncaixe(encaixe);
        if (dto.getUnidadeId() == null) {
            agendamento.setUnidade(null);
            return;
        }
        UnidadeDeSaude unidade = agendaService.buscarUnidade(dto.getUnidadeId());
        controleDeAcesso.exigir("AGENDAMENTO.GERENCIAR", unidade);
        agendamento.setUnidade(unidade);
        if (dto.getStatus() != StatusAgendamento.CANCELADO) {
            agendaService.exigirMarcacaoPossivel(agendamento.getProfissional(), unidade, dto.getDataHora(), encaixe, proprio);
        }
    }

    public AgendamentoResponseDto buscarPorId(UUID uuid) {
        return AgendamentoResponseDto.fromAgendamento(buscarEntidadePorId(uuid));
    }

    private Agendamento buscarEntidadePorId(UUID uuid) {
        return agendamentoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um agendamento com o id " + uuid + " em nossos registros."));
    }

    private Paciente buscarPacientePorId(UUID uuid) {
        return pacienteRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um paciente com o id " + uuid + " em nossos registros."));
    }

    private Profissional buscarProfissionalPorMatricula(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }
}
