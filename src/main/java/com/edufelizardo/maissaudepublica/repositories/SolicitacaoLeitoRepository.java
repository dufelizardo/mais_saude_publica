package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.SolicitacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoLeito;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoLeitoRepository extends JpaRepository<SolicitacaoLeito, UUID> {
    List<SolicitacaoLeito> findByStatusInOrderBySolicitadaEmAsc(Collection<StatusSolicitacaoLeito> status);

    boolean existsByPaciente_UuidAndStatusIn(UUID pacienteId, Collection<StatusSolicitacaoLeito> status);

    List<SolicitacaoLeito> findByPaciente_UuidOrderBySolicitadaEmDesc(UUID pacienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SolicitacaoLeito s where s.uuid = :uuid")
    Optional<SolicitacaoLeito> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
