package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Leito;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeitoRepository extends JpaRepository<Leito, UUID> {
    List<Leito> findByUnidade_UuidOrderByIdentificacaoAsc(UUID unidadeId);

    List<Leito> findAllByOrderByIdentificacaoAsc();

    boolean existsByUnidade_UuidAndIdentificacaoIgnoreCase(UUID unidadeId, String identificacao);

    /** Trava o leito: duas admissões não ocupam o mesmo leito ao mesmo tempo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Leito l where l.uuid = :uuid")
    Optional<Leito> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
