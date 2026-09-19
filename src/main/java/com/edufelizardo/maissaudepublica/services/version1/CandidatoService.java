package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Candidato;
import com.edufelizardo.maissaudepublica.models.Vaga;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CandidatoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CandidatoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.CandidatoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CandidatoService {

    @Autowired
    private CandidatoRepository candidatoRepository;

    @Autowired
    private VagaService vagaService;

    public CandidatoResponseDto criar(CandidatoRequestDto dto) {
        Vaga vaga = vagaService.buscarEntidadePorId(dto.getVagaId());

        Candidato candidato = new Candidato(vaga, dto.getNome(), dto.getCpf(), dto.getCurriculoUrl(), dto.getStatus());
        candidato = candidatoRepository.save(candidato);
        return CandidatoResponseDto.fromCandidato(candidato);
    }

    public List<CandidatoResponseDto> listarPorVaga(UUID vagaId) {
        List<CandidatoResponseDto> candidatos = candidatoRepository.findByVaga_UuidOrderByNomeAsc(vagaId)
                .stream()
                .map(CandidatoResponseDto::fromCandidato)
                .collect(Collectors.toList());
        if (candidatos.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar candidatos para a vaga de id " + vagaId + " em nossos registros.");
        }
        return candidatos;
    }
}
