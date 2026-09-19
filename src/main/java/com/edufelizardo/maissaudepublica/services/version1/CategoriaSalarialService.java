package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CategoriaSalarialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CategoriaSalarialResponseDto;
import com.edufelizardo.maissaudepublica.repositories.CategoriaSalarialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoriaSalarialService {

    @Autowired
    private CategoriaSalarialRepository categoriaSalarialRepository;

    public CategoriaSalarialResponseDto criar(CategoriaSalarialRequestDto dto) {
        CategoriaSalarial categoria = new CategoriaSalarial(dto);
        categoria = categoriaSalarialRepository.save(categoria);
        return CategoriaSalarialResponseDto.fromCategoriaSalarial(categoria);
    }

    public List<CategoriaSalarialResponseDto> listar() {
        return categoriaSalarialRepository.findAll()
                .stream()
                .map(CategoriaSalarialResponseDto::fromCategoriaSalarial)
                .collect(Collectors.toList());
    }

    public CategoriaSalarialResponseDto buscarPorId(UUID uuid) {
        return CategoriaSalarialResponseDto.fromCategoriaSalarial(buscarEntidadePorId(uuid));
    }

    CategoriaSalarial buscarEntidadePorId(UUID uuid) {
        return categoriaSalarialRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma categoria salarial com o id " + uuid + " em nossos registros."));
    }
}
