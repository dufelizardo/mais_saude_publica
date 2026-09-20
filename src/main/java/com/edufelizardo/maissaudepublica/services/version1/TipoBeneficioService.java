package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.TipoBeneficio;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TipoBeneficioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TipoBeneficioResponseDto;
import com.edufelizardo.maissaudepublica.repositories.TipoBeneficioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TipoBeneficioService {

    @Autowired
    private TipoBeneficioRepository tipoBeneficioRepository;

    public TipoBeneficioResponseDto criar(TipoBeneficioRequestDto dto) {
        TipoBeneficio tipoBeneficio = new TipoBeneficio(dto);
        tipoBeneficio = tipoBeneficioRepository.save(tipoBeneficio);
        return TipoBeneficioResponseDto.fromTipoBeneficio(tipoBeneficio);
    }

    public List<TipoBeneficioResponseDto> listar() {
        return tipoBeneficioRepository.findAll()
                .stream()
                .map(TipoBeneficioResponseDto::fromTipoBeneficio)
                .collect(Collectors.toList());
    }

    public TipoBeneficioResponseDto buscarPorId(UUID uuid) {
        return TipoBeneficioResponseDto.fromTipoBeneficio(buscarEntidadePorId(uuid));
    }

    TipoBeneficio buscarEntidadePorId(UUID uuid) {
        return tipoBeneficioRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um tipo de benefício com o id " + uuid + " em nossos registros."));
    }
}
