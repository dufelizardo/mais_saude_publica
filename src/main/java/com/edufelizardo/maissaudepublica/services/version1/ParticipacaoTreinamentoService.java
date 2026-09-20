package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.ParticipacaoTreinamento;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Treinamento;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ParticipacaoTreinamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ParticipacaoTreinamentoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoTreinamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParticipacaoTreinamentoService {

    @Autowired
    private ParticipacaoTreinamentoRepository participacaoTreinamentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private TreinamentoService treinamentoService;

    public ParticipacaoTreinamentoResponseDto criar(ParticipacaoTreinamentoRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));
        Treinamento treinamento = treinamentoService.buscarEntidadePorId(dto.getTreinamentoId());

        LocalDate dataValidade = treinamento.getValidadeMeses() != null
                ? dto.getDataConclusao().plusMonths(treinamento.getValidadeMeses())
                : null;

        ParticipacaoTreinamento participacao = new ParticipacaoTreinamento(profissional, treinamento,
                dto.getDataConclusao(), dataValidade, dto.getCertificadoUrl());
        participacao = participacaoTreinamentoRepository.save(participacao);
        return ParticipacaoTreinamentoResponseDto.fromParticipacaoTreinamento(participacao);
    }

    public List<ParticipacaoTreinamentoResponseDto> listarHistorico(String matricula) {
        List<ParticipacaoTreinamentoResponseDto> historico = participacaoTreinamentoRepository.findByProfissional_MatriculaOrderByDataConclusaoDesc(matricula)
                .stream()
                .map(ParticipacaoTreinamentoResponseDto::fromParticipacaoTreinamento)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar participações em treinamentos para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
