package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.TabelaSalarial;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TabelaSalarialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TabelaSalarialResponseDto;
import com.edufelizardo.maissaudepublica.repositories.TabelaSalarialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TabelaSalarialService {

    @Autowired
    private TabelaSalarialRepository tabelaSalarialRepository;

    @Autowired
    private CargoService cargoService;

    public TabelaSalarialResponseDto criar(TabelaSalarialRequestDto dto) {
        Cargo cargo = cargoService.buscarEntidadePorId(dto.getCargoId());
        TabelaSalarial tabelaSalarial = new TabelaSalarial(cargo, dto.getValorBase(), dto.getDataVigencia(), dto.getMotivo());
        tabelaSalarial = tabelaSalarialRepository.save(tabelaSalarial);
        return TabelaSalarialResponseDto.fromTabelaSalarial(tabelaSalarial);
    }

    public List<TabelaSalarialResponseDto> listarPorCargo(UUID cargoId) {
        List<TabelaSalarialResponseDto> historico = tabelaSalarialRepository.findByCargo_UuidOrderByDataVigenciaDesc(cargoId)
                .stream()
                .map(TabelaSalarialResponseDto::fromTabelaSalarial)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar valores de tabela salarial para o cargo de id " + cargoId + " em nossos registros.");
        }
        return historico;
    }

    public TabelaSalarialResponseDto buscarVigente(UUID cargoId, LocalDate data) {
        LocalDate dataConsulta = data != null ? data : LocalDate.now();
        return tabelaSalarialRepository
                .findFirstByCargo_UuidAndDataVigenciaLessThanEqualOrderByDataVigenciaDesc(cargoId, dataConsulta)
                .map(TabelaSalarialResponseDto::fromTabelaSalarial)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um valor vigente de tabela salarial para o cargo de id " + cargoId
                                + " na data " + dataConsulta + "."));
    }
}
