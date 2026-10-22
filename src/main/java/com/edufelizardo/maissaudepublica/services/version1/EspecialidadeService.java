package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Especialidade;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EspecialidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EspecialidadeDto;
import com.edufelizardo.maissaudepublica.repositories.EspecialidadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Catálogo de especialidades (ADR-0134): cadastrável, para todas as especialidades do atendimento e da agenda. O código
 * não muda; a especialidade sai de uso desativada, sem apagar (os atendimentos antigos continuam apontando para ela).
 */
@Service
public class EspecialidadeService {

    @Autowired
    private EspecialidadeRepository repository;

    @Transactional(readOnly = true)
    public List<EspecialidadeDto> listar() {
        return repository.findAllByOrderByNomeAsc().stream().map(EspecialidadeService::dto).toList();
    }

    @Transactional
    public EspecialidadeDto criar(EspecialidadeRequestDto dto) {
        if (repository.existsByCodigo(dto.getCodigo())) {
            throw new ResourceConflictException("Já existe uma especialidade com o código " + dto.getCodigo() + ".");
        }
        Especialidade e = new Especialidade(null, dto.getCodigo(), dto.getNome().trim(), Boolean.TRUE.equals(dto.getOdontologica()),
                !Boolean.FALSE.equals(dto.getAtiva()));
        e = repository.save(e);
        ContextoAuditoria.registro(e.getUuid());
        return dto(e);
    }

    @Transactional
    public EspecialidadeDto atualizar(UUID uuid, EspecialidadeRequestDto dto) {
        Especialidade e = repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a especialidade " + uuid + " em nossos registros."));
        if (!e.getCodigo().equals(dto.getCodigo())) {
            throw new ResourceUnprocessableEntityException("O código da especialidade não muda (" + e.getCodigo() + ").");
        }
        e.setNome(dto.getNome().trim());
        e.setOdontologica(Boolean.TRUE.equals(dto.getOdontologica()));
        e.setAtiva(!Boolean.FALSE.equals(dto.getAtiva()));
        repository.save(e);
        ContextoAuditoria.registro(e.getUuid());
        return dto(e);
    }

    /** A especialidade informada, ativa (422) — para o atendimento, o bloco e a marcação. Nulo, nenhuma. */
    @Transactional(readOnly = true)
    public Especialidade ativa(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        Especialidade e = repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a especialidade " + uuid + " em nossos registros."));
        if (!e.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A especialidade " + e.getNome() + " está inativa.");
        }
        return e;
    }

    static EspecialidadeDto dto(Especialidade e) {
        return new EspecialidadeDto(e.getUuid(), e.getCodigo(), e.getNome(), e.isOdontologica(), e.isAtiva());
    }
}
