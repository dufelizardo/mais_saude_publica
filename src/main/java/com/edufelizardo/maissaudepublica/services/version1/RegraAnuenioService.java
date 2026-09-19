package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.RegraAnuenio;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegraAnuenioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RegraAnuenioResponseDto;
import com.edufelizardo.maissaudepublica.repositories.RegraAnuenioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RegraAnuenioService {

    @Autowired
    private RegraAnuenioRepository regraAnuenioRepository;

    @Autowired
    private CategoriaSalarialService categoriaSalarialService;

    public RegraAnuenioResponseDto criar(RegraAnuenioRequestDto dto) {
        CategoriaSalarial categoria = categoriaSalarialService.buscarEntidadePorId(dto.getCategoriaId());

        if (regraAnuenioRepository.existsByCategoria_Uuid(dto.getCategoriaId())) {
            throw new ResourceConflictException(
                    "Já existe uma regra de anuênio cadastrada para a categoria " + categoria.getNome() + ".");
        }

        RegraAnuenio regraAnuenio = new RegraAnuenio(categoria, dto.getPercentualPorAno(), dto.getTetoAnos());
        regraAnuenio = regraAnuenioRepository.save(regraAnuenio);
        return RegraAnuenioResponseDto.fromRegraAnuenio(regraAnuenio);
    }

    public RegraAnuenioResponseDto buscarPorCategoria(UUID categoriaId) {
        return regraAnuenioRepository.findByCategoria_Uuid(categoriaId)
                .map(RegraAnuenioResponseDto::fromRegraAnuenio)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma regra de anuênio para a categoria de id " + categoriaId + " em nossos registros."));
    }
}
