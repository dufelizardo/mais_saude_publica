package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Consulta;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConsultaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ConsultaResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD da Consulta (Assistência — ver ADR-0039/ADR-0043). {@code profissionalMatricula} é
 * resolvido para o {@link Profissional} real na fronteira da API, mesmo padrão do
 * {@code AtendimentoService} (ver ADR-0034/ADR-0041).
 */
@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public ConsultaResponseDto criar(ConsultaRequestDto dto) {
        Atendimento atendimento = buscarAtendimentoPorId(dto.getAtendimentoId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Consulta consulta = new Consulta(atendimento, profissional, dto.getDataHora(), dto.getTipoConsulta(),
                dto.getQueixaPrincipal(), dto.getDiagnostico(), dto.getReceituario(),
                dto.getExamesSolicitados(), dto.getRetorno());
        consulta = consultaRepository.save(consulta);
        return ConsultaResponseDto.fromConsulta(consulta);
    }

    public ConsultaResponseDto atualizar(UUID uuid, ConsultaRequestDto dto) {
        Consulta consulta = buscarEntidadePorId(uuid);
        Atendimento atendimento = buscarAtendimentoPorId(dto.getAtendimentoId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        consulta.setAtendimento(atendimento);
        consulta.setProfissional(profissional);
        consulta.setDataHora(dto.getDataHora());
        consulta.setTipoConsulta(dto.getTipoConsulta());
        consulta.setQueixaPrincipal(dto.getQueixaPrincipal());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setReceituario(dto.getReceituario());
        consulta.setExamesSolicitados(dto.getExamesSolicitados());
        consulta.setRetorno(dto.getRetorno());
        consulta = consultaRepository.save(consulta);
        return ConsultaResponseDto.fromConsulta(consulta);
    }

    public List<ConsultaResponseDto> listar() {
        return consultaRepository.findAll()
                .stream()
                .map(ConsultaResponseDto::fromConsulta)
                .collect(Collectors.toList());
    }

    public ConsultaResponseDto buscarPorId(UUID uuid) {
        return ConsultaResponseDto.fromConsulta(buscarEntidadePorId(uuid));
    }

    private Consulta buscarEntidadePorId(UUID uuid) {
        return consultaRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma consulta com o id " + uuid + " em nossos registros."));
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
