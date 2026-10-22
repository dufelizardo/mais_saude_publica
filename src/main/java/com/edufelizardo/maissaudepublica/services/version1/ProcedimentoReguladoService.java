package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CapacidadeAssistencial;
import com.edufelizardo.maissaudepublica.repositories.CapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.models.ProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProcedimentoReguladoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcedimentoReguladoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoReguladoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Catálogo de procedimentos regulados (ADR-0087), mantido pela Central de Regulação para a rede inteira. */
@Service
public class ProcedimentoReguladoService {

    @Autowired
    private ProcedimentoReguladoRepository repository;

    @Autowired
    private CapacidadeAssistencialRepository capacidadeRepository;

    @Transactional
    public ProcedimentoReguladoResponseDto criar(ProcedimentoReguladoRequestDto dto) {
        String nome = dto.getNome().trim();
        exigirNomeLivre(nome, null);
        ProcedimentoRegulado novo = new ProcedimentoRegulado(nome, dto.getTipo(), dto.getAtivo() == null || dto.getAtivo());
        novo.setCapacidadeExigida(capacidade(dto.getCapacidadeExigidaId()));
        ProcedimentoRegulado salvo = repository.save(novo);
        return ProcedimentoReguladoResponseDto.fromProcedimento(salvo);
    }

    @Transactional
    public ProcedimentoReguladoResponseDto atualizar(UUID uuid, ProcedimentoReguladoRequestDto dto) {
        ProcedimentoRegulado procedimento = buscar(uuid);
        String nome = dto.getNome().trim();
        exigirNomeLivre(nome, uuid);
        procedimento.setNome(nome);
        procedimento.setTipo(dto.getTipo());
        procedimento.setCapacidadeExigida(capacidade(dto.getCapacidadeExigidaId()));
        if (dto.getAtivo() != null) {
            procedimento.setAtivo(dto.getAtivo());
        }
        return ProcedimentoReguladoResponseDto.fromProcedimento(repository.save(procedimento));
    }

    /** Em ordem alfabética; {@code apenasAtivos} para o seletor da nova solicitação. */
    @Transactional(readOnly = true)
    public List<ProcedimentoReguladoResponseDto> listar(boolean apenasAtivos) {
        return repository.findAllByOrderByNomeAsc().stream()
                .filter(p -> !apenasAtivos || p.isAtivo())
                .map(ProcedimentoReguladoResponseDto::fromProcedimento)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcedimentoReguladoResponseDto buscarPorId(UUID uuid) {
        return ProcedimentoReguladoResponseDto.fromProcedimento(buscar(uuid));
    }

    /** A capacidade exigida da executante (ADR-0138): existente (404) e ativa (422). Nula, nenhuma. */
    private CapacidadeAssistencial capacidade(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        CapacidadeAssistencial c = capacidadeRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a capacidade assistencial " + uuid + " em nossos registros."));
        if (!c.isAtivo()) {
            throw new ResourceUnprocessableEntityException("A capacidade " + c.getNome() + " está inativa.");
        }
        return c;
    }

    ProcedimentoRegulado buscar(UUID uuid) {
        return repository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um procedimento regulado com o id " + uuid + " em nossos registros."));
    }

    private void exigirNomeLivre(String nome, UUID proprio) {
        repository.findByNomeIgnoreCase(nome)
                .filter(p -> !p.getUuid().equals(proprio))
                .ifPresent(p -> {
                    throw new ResourceConflictException("Já existe um procedimento regulado com o nome " + nome + ".");
                });
    }
}
