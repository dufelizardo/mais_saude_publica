package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.TipoBeneficio;
import com.edufelizardo.maissaudepublica.models.ValorBeneficio;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ValorBeneficioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ValorBeneficioResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ValorBeneficioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValorBeneficioService {

    @Autowired
    private ValorBeneficioRepository valorBeneficioRepository;

    @Autowired
    private TipoBeneficioService tipoBeneficioService;

    public ValorBeneficioResponseDto criar(ValorBeneficioRequestDto dto) {
        TipoBeneficio tipoBeneficio = tipoBeneficioService.buscarEntidadePorId(dto.getTipoBeneficioId());
        ValorBeneficio valorBeneficio = new ValorBeneficio(tipoBeneficio, dto.getValor(), dto.getDataVigencia(), dto.getMotivo());
        valorBeneficio = valorBeneficioRepository.save(valorBeneficio);
        return ValorBeneficioResponseDto.fromValorBeneficio(valorBeneficio);
    }

    public List<ValorBeneficioResponseDto> listarPorTipo(UUID tipoBeneficioId) {
        List<ValorBeneficioResponseDto> historico = valorBeneficioRepository.findByTipoBeneficio_UuidOrderByDataVigenciaDesc(tipoBeneficioId)
                .stream()
                .map(ValorBeneficioResponseDto::fromValorBeneficio)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar valores para o tipo de benefício de id " + tipoBeneficioId + " em nossos registros.");
        }
        return historico;
    }

    public ValorBeneficioResponseDto buscarVigente(UUID tipoBeneficioId, LocalDate data) {
        LocalDate dataConsulta = data != null ? data : LocalDate.now();
        return valorBeneficioRepository
                .findFirstByTipoBeneficio_UuidAndDataVigenciaLessThanEqualOrderByDataVigenciaDesc(tipoBeneficioId, dataConsulta)
                .map(ValorBeneficioResponseDto::fromValorBeneficio)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um valor vigente para o tipo de benefício de id " + tipoBeneficioId
                                + " na data " + dataConsulta + "."));
    }
}
