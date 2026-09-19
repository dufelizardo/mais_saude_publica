package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.AdesaoBeneficio;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.TipoBeneficio;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AdesaoBeneficioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AdesaoBeneficioResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AdesaoBeneficioRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdesaoBeneficioService {

    @Autowired
    private AdesaoBeneficioRepository adesaoBeneficioRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private TipoBeneficioService tipoBeneficioService;

    public AdesaoBeneficioResponseDto criar(AdesaoBeneficioRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));
        TipoBeneficio tipoBeneficio = tipoBeneficioService.buscarEntidadePorId(dto.getTipoBeneficioId());

        AdesaoBeneficio adesaoBeneficio = new AdesaoBeneficio(profissional, tipoBeneficio, dto.getDataInicio(), dto.getQuantidadeDependentes());
        adesaoBeneficio = adesaoBeneficioRepository.save(adesaoBeneficio);
        return AdesaoBeneficioResponseDto.fromAdesaoBeneficio(adesaoBeneficio);
    }

    public List<AdesaoBeneficioResponseDto> listarHistorico(String matricula) {
        List<AdesaoBeneficioResponseDto> historico = adesaoBeneficioRepository.findByProfissional_MatriculaOrderByDataInicioDesc(matricula)
                .stream()
                .map(AdesaoBeneficioResponseDto::fromAdesaoBeneficio)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar adesões de benefício para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
