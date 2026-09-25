package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.PerfilAdministrativo;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerfilAdministrativoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PerfilAdministrativoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.PerfilAdministrativoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PerfilAdministrativoService {

    @Autowired
    private PerfilAdministrativoRepository perfilAdministrativoRepository;

    public PerfilAdministrativoResponseDto criar(PerfilAdministrativoRequestDto dto) {
        PerfilAdministrativo perfil = new PerfilAdministrativo(
                dto.getCodigo(), dto.getNome(), dto.getDescricao(), dto.getAtivo());
        perfil = perfilAdministrativoRepository.save(perfil);
        return PerfilAdministrativoResponseDto.fromPerfilAdministrativo(perfil);
    }

    public List<PerfilAdministrativoResponseDto> listar() {
        return perfilAdministrativoRepository.findAll()
                .stream()
                .map(PerfilAdministrativoResponseDto::fromPerfilAdministrativo)
                .collect(Collectors.toList());
    }

    public PerfilAdministrativoResponseDto buscarPorId(UUID uuid) {
        return PerfilAdministrativoResponseDto.fromPerfilAdministrativo(buscarEntidadePorId(uuid));
    }

    public PerfilAdministrativoResponseDto atualizar(UUID uuid, PerfilAdministrativoRequestDto dto) {
        PerfilAdministrativo perfil = buscarEntidadePorId(uuid);
        perfil.setCodigo(dto.getCodigo());
        perfil.setNome(dto.getNome());
        perfil.setDescricao(dto.getDescricao());
        perfil.setAtivo(dto.getAtivo());
        perfil = perfilAdministrativoRepository.save(perfil);
        return PerfilAdministrativoResponseDto.fromPerfilAdministrativo(perfil);
    }

    PerfilAdministrativo buscarEntidadePorId(UUID uuid) {
        return perfilAdministrativoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um perfil administrativo com o id " + uuid + " em nossos registros."));
    }
}
