package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Consulta;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AtendimentoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ConsultaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcedimentoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProntuarioAtendimentoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EvolucaoEnfermagemResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProntuarioConsultaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProntuarioResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TriagemResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.EvolucaoEnfermagemRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.TriagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Monta o Prontuário de um Paciente sob demanda, agregando Atendimento/Consulta/Procedimento (ver
 * ADR-0039 decisão 6, ADR-0045) — leitura pura, nenhum dado é persistido aqui.
 */
@Service
public class ProntuarioService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ProcedimentoRepository procedimentoRepository;

    @Autowired
    private TriagemRepository triagemRepository;

    @Autowired
    private EvolucaoEnfermagemRepository evolucaoEnfermagemRepository;

    public ProntuarioResponseDto buscarPorPacienteId(UUID pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros."));

        List<ProntuarioAtendimentoDto> atendimentos = atendimentoRepository.findByPacienteUuid(pacienteId)
                .stream()
                .map(this::montarAtendimento)
                .collect(Collectors.toList());

        ProntuarioResponseDto response = new ProntuarioResponseDto();
        response.setPacienteUuid(paciente.getUuid());
        response.setPacienteNome(paciente.getNome());
        response.setAtendimentos(atendimentos);
        return response;
    }

    private ProntuarioAtendimentoDto montarAtendimento(Atendimento atendimento) {
        AtendimentoResponseDto atendimentoDto = AtendimentoResponseDto.fromAtendimento(atendimento);

        List<TriagemResponseDto> triagens = triagemRepository.findByAtendimentoUuid(atendimento.getUuid())
                .stream()
                .map(TriagemResponseDto::fromTriagem)
                .collect(Collectors.toList());

        List<EvolucaoEnfermagemResponseDto> evolucoes = evolucaoEnfermagemRepository.findByAtendimentoUuid(atendimento.getUuid())
                .stream()
                .map(EvolucaoEnfermagemResponseDto::fromEvolucaoEnfermagem)
                .collect(Collectors.toList());

        List<ProntuarioConsultaDto> consultas = consultaRepository.findByAtendimentoUuid(atendimento.getUuid())
                .stream()
                .map(this::montarConsulta)
                .collect(Collectors.toList());

        ProntuarioAtendimentoDto dto = new ProntuarioAtendimentoDto();
        dto.setAtendimento(atendimentoDto);
        dto.setTriagens(triagens);
        dto.setEvolucoes(evolucoes);
        dto.setConsultas(consultas);
        return dto;
    }

    private ProntuarioConsultaDto montarConsulta(Consulta consulta) {
        ConsultaResponseDto consultaDto = ConsultaResponseDto.fromConsulta(consulta);

        List<ProcedimentoResponseDto> procedimentos = procedimentoRepository.findByConsultaUuid(consulta.getUuid())
                .stream()
                .map(ProcedimentoResponseDto::fromProcedimento)
                .collect(Collectors.toList());

        ProntuarioConsultaDto dto = new ProntuarioConsultaDto();
        dto.setConsulta(consultaDto);
        dto.setProcedimentos(procedimentos);
        return dto;
    }
}
