package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.CapacidadeAdministrativa;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CapacidadeAdministrativaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CapacidadeAdministrativaResponseDto;
import com.edufelizardo.maissaudepublica.repositories.CapacidadeAdministrativaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CapacidadeAdministrativaService {

    @Autowired
    private CapacidadeAdministrativaRepository capacidadeAdministrativaRepository;

    public CapacidadeAdministrativaResponseDto criar(CapacidadeAdministrativaRequestDto dto) {
        CapacidadeAdministrativa capacidade = new CapacidadeAdministrativa(
                dto.getCodigo(), dto.getNome(), dto.getDescricao(), dto.getAtivo());
        capacidade = capacidadeAdministrativaRepository.save(capacidade);
        return CapacidadeAdministrativaResponseDto.fromCapacidadeAdministrativa(capacidade);
    }

    public List<CapacidadeAdministrativaResponseDto> listar() {
        return capacidadeAdministrativaRepository.findAll()
                .stream()
                .map(CapacidadeAdministrativaResponseDto::fromCapacidadeAdministrativa)
                .collect(Collectors.toList());
    }

    public CapacidadeAdministrativaResponseDto buscarPorId(UUID uuid) {
        return CapacidadeAdministrativaResponseDto.fromCapacidadeAdministrativa(buscarEntidadePorId(uuid));
    }

    public CapacidadeAdministrativaResponseDto atualizar(UUID uuid, CapacidadeAdministrativaRequestDto dto) {
        CapacidadeAdministrativa capacidade = buscarEntidadePorId(uuid);
        capacidade.setCodigo(dto.getCodigo());
        capacidade.setNome(dto.getNome());
        capacidade.setDescricao(dto.getDescricao());
        capacidade.setAtivo(dto.getAtivo());
        capacidade = capacidadeAdministrativaRepository.save(capacidade);
        return CapacidadeAdministrativaResponseDto.fromCapacidadeAdministrativa(capacidade);
    }

    CapacidadeAdministrativa buscarEntidadePorId(UUID uuid) {
        return capacidadeAdministrativaRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma capacidade administrativa com o id " + uuid + " em nossos registros."));
    }
}
