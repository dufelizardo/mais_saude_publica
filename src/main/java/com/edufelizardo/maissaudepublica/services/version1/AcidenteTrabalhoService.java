package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.AcidenteTrabalho;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcidenteTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcidenteTrabalhoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AcidenteTrabalhoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcidenteTrabalhoService {

    @Autowired
    private AcidenteTrabalhoRepository acidenteTrabalhoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public AcidenteTrabalhoResponseDto criar(AcidenteTrabalhoRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));

        AcidenteTrabalho acidente = new AcidenteTrabalho(profissional, dto.getDataHora(), dto.getDescricao(),
                dto.getCatEmitida(), dto.getCatUrl(), dto.getDiasAfastamento());
        acidente = acidenteTrabalhoRepository.save(acidente);
        return AcidenteTrabalhoResponseDto.fromAcidenteTrabalho(acidente);
    }

    public List<AcidenteTrabalhoResponseDto> listarHistorico(String matricula) {
        List<AcidenteTrabalhoResponseDto> historico = acidenteTrabalhoRepository.findByProfissional_MatriculaOrderByDataHoraDesc(matricula)
                .stream()
                .map(AcidenteTrabalhoResponseDto::fromAcidenteTrabalho)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar acidentes de trabalho para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
