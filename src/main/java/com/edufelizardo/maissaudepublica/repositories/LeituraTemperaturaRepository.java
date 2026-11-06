package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.LeituraTemperatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeituraTemperaturaRepository extends JpaRepository<LeituraTemperatura, UUID> {

    /** As leituras das câmaras da unidade no intervalo (ADR-0148). */
    @Query("select l from LeituraTemperatura l join fetch l.camara c where c.unidade.uuid = ?1 and l.lidaEm >= ?2 and l.lidaEm < ?3 order by l.lidaEm")
    List<LeituraTemperatura> findDaUnidadeEntre(UUID unidadeId, LocalDateTime de, LocalDateTime ate);

    Optional<LeituraTemperatura> findFirstByCamara_UuidOrderByLidaEmDesc(UUID camaraId);
}
