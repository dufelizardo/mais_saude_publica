package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.InternacaoAgendada;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacaoAgendada;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InternacaoAgendadaRepository extends JpaRepository<InternacaoAgendada, UUID> {
    List<InternacaoAgendada> findByDataPrevistaBetweenOrderByDataPrevistaAscAgendadaEmAsc(LocalDate de, LocalDate ate);

    List<InternacaoAgendada> findByStatusInAndDataPrevistaBeforeOrderByDataPrevistaAsc(Collection<StatusInternacaoAgendada> status, LocalDate antesDe);

    boolean existsByPaciente_UuidAndStatusIn(UUID pacienteId, Collection<StatusInternacaoAgendada> status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from InternacaoAgendada a where a.uuid = :uuid")
    Optional<InternacaoAgendada> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
