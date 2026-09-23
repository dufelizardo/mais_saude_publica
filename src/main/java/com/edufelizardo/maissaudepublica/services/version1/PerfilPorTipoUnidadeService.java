package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.PerfilAdministrativo;
import com.edufelizardo.maissaudepublica.models.PerfilPorTipoUnidade;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerfilPorTipoUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PerfilPorTipoUnidadeResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.PerfilPorTipoUnidadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PerfilPorTipoUnidadeService {

    @Autowired
    private PerfilPorTipoUnidadeRepository perfilPorTipoUnidadeRepository;

    @Autowired
    private PerfilAdministrativoService perfilAdministrativoService;

    public PerfilPorTipoUnidadeResponseDto criar(PerfilPorTipoUnidadeRequestDto dto) {
        PerfilAdministrativo perfil = perfilAdministrativoService.buscarEntidadePorId(dto.getPerfilAdministrativoId());
        PerfilPorTipoUnidade associacao = new PerfilPorTipoUnidade(dto.getTipo(), perfil);
        associacao = perfilPorTipoUnidadeRepository.save(associacao);
        return PerfilPorTipoUnidadeResponseDto.fromPerfilPorTipoUnidade(associacao);
    }

    public List<PerfilPorTipoUnidadeResponseDto> listar() {
        return perfilPorTipoUnidadeRepository.findAll()
                .stream()
                .map(PerfilPorTipoUnidadeResponseDto::fromPerfilPorTipoUnidade)
                .collect(Collectors.toList());
    }

    public PerfilPorTipoUnidadeResponseDto buscarPorId(UUID uuid) {
        return PerfilPorTipoUnidadeResponseDto.fromPerfilPorTipoUnidade(buscarEntidadePorId(uuid));
    }

    public PerfilPorTipoUnidadeResponseDto buscarPorTipo(TipoUnidadeDeSaude tipo) {
        PerfilPorTipoUnidade associacao = perfilPorTipoUnidadeRepository.findByTipo(tipo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não há perfil administrativo configurado para o tipo " + tipo + "."));
        return PerfilPorTipoUnidadeResponseDto.fromPerfilPorTipoUnidade(associacao);
    }

    public PerfilPorTipoUnidadeResponseDto atualizar(UUID uuid, PerfilPorTipoUnidadeRequestDto dto) {
        PerfilPorTipoUnidade associacao = buscarEntidadePorId(uuid);
        PerfilAdministrativo perfil = perfilAdministrativoService.buscarEntidadePorId(dto.getPerfilAdministrativoId());
        associacao.setTipo(dto.getTipo());
        associacao.setPerfilAdministrativo(perfil);
        associacao = perfilPorTipoUnidadeRepository.save(associacao);
        return PerfilPorTipoUnidadeResponseDto.fromPerfilPorTipoUnidade(associacao);
    }

    PerfilPorTipoUnidade buscarEntidadePorId(UUID uuid) {
        return perfilPorTipoUnidadeRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma associação perfil/tipo de unidade com o id " + uuid + " em nossos registros."));
    }
}
