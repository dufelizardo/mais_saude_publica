package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
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

    @Transactional
    public ProcedimentoReguladoResponseDto criar(ProcedimentoReguladoRequestDto dto) {
        String nome = dto.getNome().trim();
        exigirNomeLivre(nome, null);
        ProcedimentoRegulado salvo = repository.save(new ProcedimentoRegulado(nome, dto.getTipo(),
                dto.getAtivo() == null || dto.getAtivo()));
        return ProcedimentoReguladoResponseDto.fromProcedimento(salvo);
    }

    @Transactional
    public ProcedimentoReguladoResponseDto atualizar(UUID uuid, ProcedimentoReguladoRequestDto dto) {
        ProcedimentoRegulado procedimento = buscar(uuid);
        String nome = dto.getNome().trim();
        exigirNomeLivre(nome, uuid);
        procedimento.setNome(nome);
        procedimento.setTipo(dto.getTipo());
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
