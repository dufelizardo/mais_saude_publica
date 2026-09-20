package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CargoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CargoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CargoService {

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private CategoriaSalarialService categoriaSalarialService;

    public CargoResponseDto criar(CargoRequestDto dto) {
        CategoriaSalarial categoria = categoriaSalarialService.buscarEntidadePorId(dto.getCategoriaId());
        Cargo cargo = new Cargo(categoria, dto.getNome(), dto.getDescricao());
        cargo = cargoRepository.save(cargo);
        return CargoResponseDto.fromCargo(cargo);
    }

    public List<CargoResponseDto> listar() {
        return cargoRepository.findAll()
                .stream()
                .map(CargoResponseDto::fromCargo)
                .collect(Collectors.toList());
    }

    public CargoResponseDto buscarPorId(UUID uuid) {
        return CargoResponseDto.fromCargo(buscarEntidadePorId(uuid));
    }

    public CargoResponseDto atualizar(UUID uuid, CargoRequestDto dto) {
        Cargo cargo = buscarEntidadePorId(uuid);
        CategoriaSalarial categoria = categoriaSalarialService.buscarEntidadePorId(dto.getCategoriaId());
        cargo.setCategoria(categoria);
        cargo.setNome(dto.getNome());
        cargo.setDescricao(dto.getDescricao());
        cargo = cargoRepository.save(cargo);
        return CargoResponseDto.fromCargo(cargo);
    }

    Cargo buscarEntidadePorId(UUID uuid) {
        return cargoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um cargo com o id " + uuid + " em nossos registros."));
    }
}
