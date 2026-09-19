package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.CalculoRescisao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CalculoRescisaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CalculoRescisaoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.CalculoRescisaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalculoRescisaoService {

    @Autowired
    private CalculoRescisaoRepository calculoRescisaoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public CalculoRescisaoResponseDto criar(CalculoRescisaoRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));

        CalculoRescisao calculoRescisao = new CalculoRescisao(profissional, dto.getTipoDesligamento(), dto.getAvisoPrevio(),
                dto.getFeriasVencidas(), dto.getFeriasProporcionais(), dto.getDecimoTerceiroProporcional(),
                dto.getMultaFgts(), dto.getTotal(), dto.getDocumentoTrctUrl());
        calculoRescisao = calculoRescisaoRepository.save(calculoRescisao);
        return CalculoRescisaoResponseDto.fromCalculoRescisao(calculoRescisao);
    }

    public List<CalculoRescisaoResponseDto> listarHistorico(String matricula) {
        List<CalculoRescisaoResponseDto> historico = calculoRescisaoRepository.findByProfissional_MatriculaOrderByUuidDesc(matricula)
                .stream()
                .map(CalculoRescisaoResponseDto::fromCalculoRescisao)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar cálculos de rescisão para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
