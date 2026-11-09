package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.OfertaAssistencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfertaAssistencialRepository extends JpaRepository<OfertaAssistencial, UUID> {

    @Query("select o from OfertaAssistencial o join fetch o.executante join fetch o.procedimento where o.competencia = ?1")
    List<OfertaAssistencial> findDaCompetencia(LocalDate competencia);

    Optional<OfertaAssistencial> findByExecutante_UuidAndProcedimento_UuidAndCompetencia(UUID executanteId, UUID procedimentoId, LocalDate competencia);

    boolean existsByExecutante_UuidAndProcedimento_UuidAndCompetencia(UUID executanteId, UUID procedimentoId, LocalDate competencia);
}
