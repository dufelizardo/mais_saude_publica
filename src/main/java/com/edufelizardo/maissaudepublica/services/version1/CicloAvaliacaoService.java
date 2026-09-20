package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.CicloAvaliacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CicloAvaliacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CicloAvaliacaoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.CicloAvaliacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CicloAvaliacaoService {

    @Autowired
    private CicloAvaliacaoRepository cicloAvaliacaoRepository;

    public CicloAvaliacaoResponseDto criar(CicloAvaliacaoRequestDto dto) {
        CicloAvaliacao ciclo = new CicloAvaliacao(dto);
        ciclo = cicloAvaliacaoRepository.save(ciclo);
        return CicloAvaliacaoResponseDto.fromCicloAvaliacao(ciclo);
    }

    public List<CicloAvaliacaoResponseDto> listar() {
        return cicloAvaliacaoRepository.findAll()
                .stream()
                .map(CicloAvaliacaoResponseDto::fromCicloAvaliacao)
                .collect(Collectors.toList());
    }

    public CicloAvaliacaoResponseDto buscarPorId(UUID uuid) {
        return CicloAvaliacaoResponseDto.fromCicloAvaliacao(buscarEntidadePorId(uuid));
    }

    CicloAvaliacao buscarEntidadePorId(UUID uuid) {
        return cicloAvaliacaoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um ciclo de avaliação com o id " + uuid + " em nossos registros."));
    }
}
