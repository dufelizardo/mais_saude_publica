package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InternacaoRepository extends JpaRepository<Internacao, UUID> {
    boolean existsByPaciente_UuidAndStatus(UUID pacienteId, StatusInternacao status);

    List<Internacao> findByPaciente_UuidOrderByAdmitidaEmDesc(UUID pacienteId);

    List<Internacao> findAllByOrderByAdmitidaEmDesc();

    List<Internacao> findByStatusAndLeito_UuidIn(StatusInternacao status, Collection<UUID> leitoIds);

    List<Internacao> findByStatusAndAltaEmAfter(StatusInternacao status, Instant depoisDe);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Internacao i where i.uuid = :uuid")
    Optional<Internacao> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
