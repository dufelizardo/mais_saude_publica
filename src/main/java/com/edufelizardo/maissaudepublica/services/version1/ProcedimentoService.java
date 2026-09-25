package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Consulta;
import com.edufelizardo.maissaudepublica.models.Procedimento;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProcedimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcedimentoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD do Procedimento (Assistência — ver ADR-0039/ADR-0044). {@code profissionalMatricula} é
 * resolvido para o {@link Profissional} real na fronteira da API, mesmo padrão do
 * {@code ConsultaService} (ver ADR-0034/ADR-0043).
 */
@Service
public class ProcedimentoService {

    @Autowired
    private ProcedimentoRepository procedimentoRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public ProcedimentoResponseDto criar(ProcedimentoRequestDto dto) {
        Consulta consulta = buscarConsultaPorId(dto.getConsultaId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Procedimento procedimento = new Procedimento(consulta, profissional, dto.getTipo(), dto.getDescricao(),
                dto.getDataRealizacao(), dto.getStatus());
        procedimento = procedimentoRepository.save(procedimento);
        return ProcedimentoResponseDto.fromProcedimento(procedimento);
    }

    public ProcedimentoResponseDto atualizar(UUID uuid, ProcedimentoRequestDto dto) {
        Procedimento procedimento = buscarEntidadePorId(uuid);
        Consulta consulta = buscarConsultaPorId(dto.getConsultaId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        procedimento.setConsulta(consulta);
        procedimento.setProfissional(profissional);
        procedimento.setTipo(dto.getTipo());
        procedimento.setDescricao(dto.getDescricao());
        procedimento.setDataRealizacao(dto.getDataRealizacao());
        procedimento.setStatus(dto.getStatus());
        procedimento = procedimentoRepository.save(procedimento);
        return ProcedimentoResponseDto.fromProcedimento(procedimento);
    }

    public List<ProcedimentoResponseDto> listar() {
        return procedimentoRepository.findAll()
                .stream()
                .map(ProcedimentoResponseDto::fromProcedimento)
                .collect(Collectors.toList());
    }

    public ProcedimentoResponseDto buscarPorId(UUID uuid) {
        return ProcedimentoResponseDto.fromProcedimento(buscarEntidadePorId(uuid));
    }

    private Procedimento buscarEntidadePorId(UUID uuid) {
        return procedimentoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um procedimento com o id " + uuid + " em nossos registros."));
    }

    private Consulta buscarConsultaPorId(UUID uuid) {
        return consultaRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma consulta com o id " + uuid + " em nossos registros."));
    }

    private Profissional buscarProfissionalPorMatricula(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }
}
