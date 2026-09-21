package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Treinamento;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TreinamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TreinamentoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.TreinamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TreinamentoService {

    @Autowired
    private TreinamentoRepository treinamentoRepository;

    public TreinamentoResponseDto criar(TreinamentoRequestDto dto) {
        Treinamento treinamento = new Treinamento(dto);
        treinamento = treinamentoRepository.save(treinamento);
        return TreinamentoResponseDto.fromTreinamento(treinamento);
    }

    public List<TreinamentoResponseDto> listar() {
        return treinamentoRepository.findAll()
                .stream()
                .map(TreinamentoResponseDto::fromTreinamento)
                .collect(Collectors.toList());
    }

    public TreinamentoResponseDto buscarPorId(UUID uuid) {
        return TreinamentoResponseDto.fromTreinamento(buscarEntidadePorId(uuid));
    }

    Treinamento buscarEntidadePorId(UUID uuid) {
        return treinamentoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um treinamento com o id " + uuid + " em nossos registros."));
    }
}
