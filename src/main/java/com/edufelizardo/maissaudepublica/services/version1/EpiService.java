package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Epi;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EpiRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EpiResponseDto;
import com.edufelizardo.maissaudepublica.repositories.EpiRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EpiService {

    @Autowired
    private EpiRepository epiRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public EpiResponseDto criar(EpiRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));

        Epi epi = new Epi(profissional, dto.getTipo(), dto.getNumeroCA(), dto.getDataEntrega(), dto.getDataDevolucao());
        epi = epiRepository.save(epi);
        return EpiResponseDto.fromEpi(epi);
    }

    public List<EpiResponseDto> listarHistorico(String matricula) {
        List<EpiResponseDto> historico = epiRepository.findByProfissional_MatriculaOrderByDataEntregaDesc(matricula)
                .stream()
                .map(EpiResponseDto::fromEpi)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar registros de EPI para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
