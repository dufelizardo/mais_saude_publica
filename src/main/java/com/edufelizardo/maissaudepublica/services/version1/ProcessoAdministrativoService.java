package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.CapacidadeAdministrativa;
import com.edufelizardo.maissaudepublica.models.ProcessoAdministrativo;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProcessoAdministrativoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcessoAdministrativoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ProcessoAdministrativoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProcessoAdministrativoService {

    @Autowired
    private ProcessoAdministrativoRepository processoAdministrativoRepository;

    @Autowired
    private CapacidadeAdministrativaService capacidadeAdministrativaService;

    public ProcessoAdministrativoResponseDto criar(ProcessoAdministrativoRequestDto dto) {
        CapacidadeAdministrativa capacidade = capacidadeAdministrativaService.buscarEntidadePorId(dto.getCapacidadeId());
        ProcessoAdministrativo processo = new ProcessoAdministrativo(
                capacidade, dto.getCodigo(), dto.getNome(), dto.getDescricao(), dto.getAtivo());
        processo = processoAdministrativoRepository.save(processo);
        return ProcessoAdministrativoResponseDto.fromProcessoAdministrativo(processo);
    }

    public List<ProcessoAdministrativoResponseDto> listar() {
        return processoAdministrativoRepository.findAll()
                .stream()
                .map(ProcessoAdministrativoResponseDto::fromProcessoAdministrativo)
                .collect(Collectors.toList());
    }

    public ProcessoAdministrativoResponseDto buscarPorId(UUID uuid) {
        return ProcessoAdministrativoResponseDto.fromProcessoAdministrativo(buscarEntidadePorId(uuid));
    }

    public ProcessoAdministrativoResponseDto atualizar(UUID uuid, ProcessoAdministrativoRequestDto dto) {
        ProcessoAdministrativo processo = buscarEntidadePorId(uuid);
        CapacidadeAdministrativa capacidade = capacidadeAdministrativaService.buscarEntidadePorId(dto.getCapacidadeId());
        processo.setCapacidade(capacidade);
        processo.setCodigo(dto.getCodigo());
        processo.setNome(dto.getNome());
        processo.setDescricao(dto.getDescricao());
        processo.setAtivo(dto.getAtivo());
        processo = processoAdministrativoRepository.save(processo);
        return ProcessoAdministrativoResponseDto.fromProcessoAdministrativo(processo);
    }

    ProcessoAdministrativo buscarEntidadePorId(UUID uuid) {
        return processoAdministrativoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um processo administrativo com o id " + uuid + " em nossos registros."));
    }
}
