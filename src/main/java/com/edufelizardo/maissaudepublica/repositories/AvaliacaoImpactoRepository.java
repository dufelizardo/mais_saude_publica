package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AvaliacaoImpacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AvaliacaoImpactoRepository extends JpaRepository<AvaliacaoImpacto, UUID> {

    @Query("select a from AvaliacaoImpacto a where a.emergencia.uuid = ?1 order by a.avaliadaEm desc")
    List<AvaliacaoImpacto> findDaEmergencia(UUID emergenciaId);
}
