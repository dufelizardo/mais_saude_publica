package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.Licenca;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LicencaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LicencaResponseDto;
import com.edufelizardo.maissaudepublica.repositories.LicencaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LicencaService {

    @Autowired
    private LicencaRepository licencaRepository;

    @Autowired
    private AfastamentoService afastamentoService;

    public List<LicencaResponseDto> listarPorProfissional(String matricula) {
        return licencaRepository.findByAfastamento_Profissional_MatriculaOrderByAfastamento_DataInicioDesc(matricula)
                .stream()
                .map(LicencaResponseDto::fromLicenca)
                .collect(Collectors.toList());
    }

    public LicencaResponseDto criar(LicencaRequestDto dto) {
        Afastamento afastamento = afastamentoService.buscarEntidadePorId(dto.getAfastamentoId());

        if (licencaRepository.existsByAfastamento_Uuid(dto.getAfastamentoId())) {
            throw new ResourceConflictException(
                    "Já existe uma licença cadastrada para o afastamento de id " + dto.getAfastamentoId() + ".");
        }

        Licenca licenca = new Licenca(afastamento, dto.getTipoLegal(), dto.getResponsavelPagamento(), dto.getDocumentoUrl());
        licenca = licencaRepository.save(licenca);
        return LicencaResponseDto.fromLicenca(licenca);
    }

    public LicencaResponseDto buscarPorAfastamento(UUID afastamentoId) {
        return licencaRepository.findByAfastamento_Uuid(afastamentoId)
                .map(LicencaResponseDto::fromLicenca)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma licença para o afastamento de id " + afastamentoId + " em nossos registros."));
    }
}
