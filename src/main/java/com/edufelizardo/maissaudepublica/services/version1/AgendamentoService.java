package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AgendamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AgendamentoResponseDto;
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
 */
@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public AgendamentoResponseDto criar(AgendamentoRequestDto dto) {
        Paciente paciente = buscarPacientePorId(dto.getPacienteId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Agendamento agendamento = new Agendamento(paciente, profissional, dto.getDataHora(),
                dto.getStatus(), dto.getTipo(), dto.getObservacao());
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
        agendamento = agendamentoRepository.save(agendamento);
        return AgendamentoResponseDto.fromAgendamento(agendamento);
    }

    public List<AgendamentoResponseDto> listar() {
        return agendamentoRepository.findAll()
                .stream()
                .map(AgendamentoResponseDto::fromAgendamento)
                .collect(Collectors.toList());
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
