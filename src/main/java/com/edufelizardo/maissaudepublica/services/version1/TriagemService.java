package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Triagem;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TriagemRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TriagemResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TriagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD da Triagem (Enfermagem — ver MAPA-DE-DOMINIOS.md #8, ADR-0047). {@code profissionalMatricula}
 * é resolvido para o {@link Profissional} real na fronteira da API, mesmo padrão do
 * {@code ConsultaService} (ver ADR-0034/ADR-0043).
 */
@Service
public class TriagemService {

    @Autowired
    private TriagemRepository triagemRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public TriagemResponseDto criar(TriagemRequestDto dto) {
        Atendimento atendimento = buscarAtendimentoPorId(dto.getAtendimentoId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Triagem triagem = new Triagem(atendimento, profissional, dto.getDataHora(), dto.getPressaoArterial(),
                dto.getTemperatura(), dto.getSaturacaoOxigenio(), dto.getFrequenciaCardiaca(), dto.getPeso(),
                dto.getClassificacaoRisco(), dto.getObservacoes());
        triagem = triagemRepository.save(triagem);
        return TriagemResponseDto.fromTriagem(triagem);
    }

    public TriagemResponseDto atualizar(UUID uuid, TriagemRequestDto dto) {
        Triagem triagem = buscarEntidadePorId(uuid);
        Atendimento atendimento = buscarAtendimentoPorId(dto.getAtendimentoId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        triagem.setAtendimento(atendimento);
        triagem.setProfissional(profissional);
        triagem.setDataHora(dto.getDataHora());
        triagem.setPressaoArterial(dto.getPressaoArterial());
        triagem.setTemperatura(dto.getTemperatura());
        triagem.setSaturacaoOxigenio(dto.getSaturacaoOxigenio());
        triagem.setFrequenciaCardiaca(dto.getFrequenciaCardiaca());
        triagem.setPeso(dto.getPeso());
        triagem.setClassificacaoRisco(dto.getClassificacaoRisco());
        triagem.setObservacoes(dto.getObservacoes());
        triagem = triagemRepository.save(triagem);
        return TriagemResponseDto.fromTriagem(triagem);
    }

    public List<TriagemResponseDto> listar() {
        return triagemRepository.findAll()
                .stream()
                .map(TriagemResponseDto::fromTriagem)
                .collect(Collectors.toList());
    }

    public TriagemResponseDto buscarPorId(UUID uuid) {
        return TriagemResponseDto.fromTriagem(buscarEntidadePorId(uuid));
    }

    private Triagem buscarEntidadePorId(UUID uuid) {
        return triagemRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma triagem com o id " + uuid + " em nossos registros."));
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
