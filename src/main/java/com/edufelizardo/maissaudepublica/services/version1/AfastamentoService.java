package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AfastamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AfastamentoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AfastamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AfastamentoService {

    @Autowired
    private AfastamentoRepository afastamentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public AfastamentoResponseDto criar(AfastamentoRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));

        Afastamento afastamento = new Afastamento(profissional, dto.getTipo(), dto.getDataInicio(),
                dto.getDataFim(), dto.getStatus(), dto.getObservacao());
        afastamento = afastamentoRepository.save(afastamento);
        return AfastamentoResponseDto.fromAfastamento(afastamento);
    }

    public List<AfastamentoResponseDto> listarHistorico(String matricula) {
        List<AfastamentoResponseDto> historico = afastamentoRepository.findByProfissional_MatriculaOrderByDataInicioDesc(matricula)
                .stream()
                .map(AfastamentoResponseDto::fromAfastamento)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar afastamentos para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
