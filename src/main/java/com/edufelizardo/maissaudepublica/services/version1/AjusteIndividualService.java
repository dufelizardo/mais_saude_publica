package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.AjusteIndividual;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AjusteIndividualRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AjusteIndividualResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AjusteIndividualRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AjusteIndividualService {

    @Autowired
    private AjusteIndividualRepository ajusteIndividualRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public AjusteIndividualResponseDto criar(AjusteIndividualRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));

        AjusteIndividual ajusteIndividual = new AjusteIndividual(profissional, dto.getValor(), dto.getDataInicio(),
                dto.getDataFim(), dto.getMotivo(), dto.getReferencia());
        ajusteIndividual = ajusteIndividualRepository.save(ajusteIndividual);
        return AjusteIndividualResponseDto.fromAjusteIndividual(ajusteIndividual);
    }

    public List<AjusteIndividualResponseDto> listarHistorico(String matricula) {
        List<AjusteIndividualResponseDto> historico = ajusteIndividualRepository.findByProfissional_MatriculaOrderByDataInicioDesc(matricula)
                .stream()
                .map(AjusteIndividualResponseDto::fromAjusteIndividual)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar ajustes individuais para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
