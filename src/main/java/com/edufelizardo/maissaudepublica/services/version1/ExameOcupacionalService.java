package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.ExameOcupacional;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ExameOcupacionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ExameOcupacionalResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ExameOcupacionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExameOcupacionalService {

    @Autowired
    private ExameOcupacionalRepository exameOcupacionalRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public ExameOcupacionalResponseDto criar(ExameOcupacionalRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));

        ExameOcupacional exame = new ExameOcupacional(profissional, dto.getTipo(), dto.getDataRealizacao(),
                dto.getDataValidade(), dto.getResultado(), dto.getAsoUrl());
        exame = exameOcupacionalRepository.save(exame);
        return ExameOcupacionalResponseDto.fromExameOcupacional(exame);
    }

    public List<ExameOcupacionalResponseDto> listarHistorico(String matricula) {
        List<ExameOcupacionalResponseDto> historico = exameOcupacionalRepository.findByProfissional_MatriculaOrderByDataRealizacaoDesc(matricula)
                .stream()
                .map(ExameOcupacionalResponseDto::fromExameOcupacional)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar exames ocupacionais para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
