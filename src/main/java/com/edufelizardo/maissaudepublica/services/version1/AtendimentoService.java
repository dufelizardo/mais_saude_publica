package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtendimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AtendimentoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD do Atendimento (Assistência — ver ADR-0039/ADR-0041). {@code profissionalMatricula} é
 * resolvido para o {@link Profissional} real na fronteira da API, mesmo padrão do
 * {@code SetorService} para {@code responsavel} (ver ADR-0034).
 */
@Service
public class AtendimentoService {

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    public AtendimentoResponseDto criar(AtendimentoRequestDto dto) {
        Paciente paciente = buscarPacientePorId(dto.getPacienteId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());
        UnidadeDeSaude unidade = buscarUnidadePorId(dto.getUnidadeId());
        Setor setor = buscarSetorSeInformado(dto.getSetorId());
        Agendamento agendamento = buscarAgendamentoSeInformado(dto.getAgendamentoId());

        Atendimento atendimento = new Atendimento(paciente, profissional, unidade, setor, agendamento,
                dto.getTipo(), dto.getStatus(), dto.getDataHora());
        atendimento = atendimentoRepository.save(atendimento);
        return AtendimentoResponseDto.fromAtendimento(atendimento);
    }

    public AtendimentoResponseDto atualizar(UUID uuid, AtendimentoRequestDto dto) {
        Atendimento atendimento = buscarEntidadePorId(uuid);
        Paciente paciente = buscarPacientePorId(dto.getPacienteId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());
        UnidadeDeSaude unidade = buscarUnidadePorId(dto.getUnidadeId());
        Setor setor = buscarSetorSeInformado(dto.getSetorId());
        Agendamento agendamento = buscarAgendamentoSeInformado(dto.getAgendamentoId());

        atendimento.setPaciente(paciente);
        atendimento.setProfissional(profissional);
        atendimento.setUnidade(unidade);
        atendimento.setSetor(setor);
        atendimento.setAgendamento(agendamento);
        atendimento.setTipo(dto.getTipo());
        atendimento.setStatus(dto.getStatus());
        atendimento.setDataHora(dto.getDataHora());
        atendimento = atendimentoRepository.save(atendimento);
        return AtendimentoResponseDto.fromAtendimento(atendimento);
    }

    public List<AtendimentoResponseDto> listar() {
        return atendimentoRepository.findAll()
                .stream()
                .map(AtendimentoResponseDto::fromAtendimento)
                .collect(Collectors.toList());
    }

    public AtendimentoResponseDto buscarPorId(UUID uuid) {
        return AtendimentoResponseDto.fromAtendimento(buscarEntidadePorId(uuid));
    }

    private Atendimento buscarEntidadePorId(UUID uuid) {
        return atendimentoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um atendimento com o id " + uuid + " em nossos registros."));
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

    private UnidadeDeSaude buscarUnidadePorId(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }

    private Setor buscarSetorSeInformado(UUID setorId) {
        if (setorId == null) {
            return null;
        }
        return setorRepository.findById(setorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um setor com o id " + setorId + " em nossos registros."));
    }

    private Agendamento buscarAgendamentoSeInformado(UUID agendamentoId) {
        if (agendamentoId == null) {
            return null;
        }
        return agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um agendamento com o id " + agendamentoId + " em nossos registros."));
    }
}
