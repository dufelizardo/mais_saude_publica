package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Vaga;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VagaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VagaResponseDto;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VagaService {

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private CargoService cargoService;

    public VagaResponseDto criar(VagaRequestDto dto) {
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        Cargo cargo = cargoService.buscarEntidadePorId(dto.getCargoId());

        Vaga vaga = new Vaga(unidade, cargo, dto.getQuantidade(), dto.getStatus());
        vaga = vagaRepository.save(vaga);
        return VagaResponseDto.fromVaga(vaga);
    }

    public VagaResponseDto atualizar(UUID uuid, VagaRequestDto dto) {
        Vaga vaga = buscarEntidadePorId(uuid);

        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        Cargo cargo = cargoService.buscarEntidadePorId(dto.getCargoId());

        vaga.setUnidade(unidade);
        vaga.setCargo(cargo);
        vaga.setQuantidade(dto.getQuantidade());
        vaga.setStatus(dto.getStatus());
        vaga = vagaRepository.save(vaga);
        return VagaResponseDto.fromVaga(vaga);
    }

    public List<VagaResponseDto> listar() {
        return vagaRepository.findAll()
                .stream()
                .map(VagaResponseDto::fromVaga)
                .collect(Collectors.toList());
    }

    public VagaResponseDto buscarPorId(UUID uuid) {
        return VagaResponseDto.fromVaga(buscarEntidadePorId(uuid));
    }

    Vaga buscarEntidadePorId(UUID uuid) {
        return vagaRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma vaga com o id " + uuid + " em nossos registros."));
    }
}
