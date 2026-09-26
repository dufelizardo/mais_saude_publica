package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.EvolucaoEnfermagem;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EvolucaoEnfermagemRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EvolucaoEnfermagemResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EvolucaoEnfermagemRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD da Evolução de Enfermagem (Enfermagem — ver MAPA-DE-DOMINIOS.md #8, ADR-0048).
 * {@code profissionalMatricula} é resolvido para o {@link Profissional} real na fronteira da API,
 * mesmo padrão do {@code TriagemService} (ver ADR-0034/ADR-0047).
 */
@Service
public class EvolucaoEnfermagemService {

    @Autowired
    private EvolucaoEnfermagemRepository evolucaoEnfermagemRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public EvolucaoEnfermagemResponseDto criar(EvolucaoEnfermagemRequestDto dto) {
        Atendimento atendimento = buscarAtendimentoPorId(dto.getAtendimentoId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        EvolucaoEnfermagem evolucao = new EvolucaoEnfermagem(atendimento, profissional, dto.getDataHora(),
                dto.getDescricao());
        evolucao = evolucaoEnfermagemRepository.save(evolucao);
        return EvolucaoEnfermagemResponseDto.fromEvolucaoEnfermagem(evolucao);
    }

    public EvolucaoEnfermagemResponseDto atualizar(UUID uuid, EvolucaoEnfermagemRequestDto dto) {
        EvolucaoEnfermagem evolucao = buscarEntidadePorId(uuid);
        Atendimento atendimento = buscarAtendimentoPorId(dto.getAtendimentoId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        evolucao.setAtendimento(atendimento);
        evolucao.setProfissional(profissional);
        evolucao.setDataHora(dto.getDataHora());
        evolucao.setDescricao(dto.getDescricao());
        evolucao = evolucaoEnfermagemRepository.save(evolucao);
        return EvolucaoEnfermagemResponseDto.fromEvolucaoEnfermagem(evolucao);
    }

    public List<EvolucaoEnfermagemResponseDto> listar() {
        return evolucaoEnfermagemRepository.findAll()
                .stream()
                .map(EvolucaoEnfermagemResponseDto::fromEvolucaoEnfermagem)
                .collect(Collectors.toList());
    }

    public EvolucaoEnfermagemResponseDto buscarPorId(UUID uuid) {
        return EvolucaoEnfermagemResponseDto.fromEvolucaoEnfermagem(buscarEntidadePorId(uuid));
    }

    private EvolucaoEnfermagem buscarEntidadePorId(UUID uuid) {
        return evolucaoEnfermagemRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma evolução de enfermagem com o id " + uuid + " em nossos registros."));
    }

    private Atendimento buscarAtendimentoPorId(UUID uuid) {
        return atendimentoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um atendimento com o id " + uuid + " em nossos registros."));
    }

    private Profissional buscarProfissionalPorMatricula(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }
}
