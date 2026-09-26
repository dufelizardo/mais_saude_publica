package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Consulta;
import com.edufelizardo.maissaudepublica.models.Dispensacao;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DispensacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DispensacaoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.DispensacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD (só criação e leitura, ver ADR-0051) da Dispensação (Farmácia — MAPA-DE-DOMINIOS.md #9).
 * Criar uma Dispensação decrementa {@code Lote.quantidade} como efeito colateral, validando estoque
 * suficiente (422, não 400 — é uma regra de negócio, não um erro de payload).
 */
@Service
public class DispensacaoService {

    @Autowired
    private DispensacaoRepository dispensacaoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Transactional
    public DispensacaoResponseDto criar(DispensacaoRequestDto dto) {
        Lote lote = buscarLotePorId(dto.getLoteId());
        Paciente paciente = buscarPacientePorId(dto.getPacienteId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());
        Consulta consulta = buscarConsultaSeInformada(dto.getConsultaId());

        if (lote.getQuantidade() < dto.getQuantidade()) {
            throw new ResourceUnprocessableEntityException(
                    "Estoque insuficiente no lote " + lote.getNumeroLote() + ": disponível " + lote.getQuantidade()
                            + ", solicitado " + dto.getQuantidade() + ".");
        }
        lote.setQuantidade(lote.getQuantidade() - dto.getQuantidade());
        loteRepository.save(lote);

        Dispensacao dispensacao = new Dispensacao(lote, paciente, profissional, consulta, dto.getQuantidade(),
                dto.getDataHora());
        dispensacao = dispensacaoRepository.save(dispensacao);
        return DispensacaoResponseDto.fromDispensacao(dispensacao);
    }

    public List<DispensacaoResponseDto> listar() {
        return dispensacaoRepository.findAll()
                .stream()
                .map(DispensacaoResponseDto::fromDispensacao)
                .collect(Collectors.toList());
    }

    public DispensacaoResponseDto buscarPorId(UUID uuid) {
        return DispensacaoResponseDto.fromDispensacao(buscarEntidadePorId(uuid));
    }

    private Dispensacao buscarEntidadePorId(UUID uuid) {
        return dispensacaoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma dispensação com o id " + uuid + " em nossos registros."));
    }

    private Lote buscarLotePorId(UUID uuid) {
        return loteRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um lote com o id " + uuid + " em nossos registros."));
    }

    private Paciente buscarPacientePorId(UUID uuid) {
        return pacienteRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um paciente com o id " + uuid + " em nossos registros."));
    }

    private Profissional buscarProfissionalPorMatricula(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    private Consulta buscarConsultaSeInformada(UUID consultaId) {
        if (consultaId == null) {
            return null;
        }
        return consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma consulta com o id " + consultaId + " em nossos registros."));
    }
}
