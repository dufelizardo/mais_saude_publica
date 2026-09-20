package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Avaliacao;
import com.edufelizardo.maissaudepublica.models.CicloAvaliacao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AvaliacaoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvaliacaoService {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private CicloAvaliacaoService cicloAvaliacaoService;

    public AvaliacaoResponseDto criar(AvaliacaoRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));
        CicloAvaliacao ciclo = cicloAvaliacaoService.buscarEntidadePorId(dto.getCicloId());

        Avaliacao avaliacao = new Avaliacao(profissional, ciclo, dto.getAvaliador(), dto.getNota(), dto.getObservacao());
        avaliacao = avaliacaoRepository.save(avaliacao);
        return AvaliacaoResponseDto.fromAvaliacao(avaliacao);
    }

    public List<AvaliacaoResponseDto> listarHistorico(String matricula) {
        List<AvaliacaoResponseDto> historico = avaliacaoRepository.findByProfissional_MatriculaOrderByUuidDesc(matricula)
                .stream()
                .map(AvaliacaoResponseDto::fromAvaliacao)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar avaliações para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
