package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PlantaoViatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PlantaoViaturaRepository extends JpaRepository<PlantaoViatura, UUID> {

    /** Os que cruzam o intervalo [de, ate). */
    @Query("select distinct p from PlantaoViatura p join fetch p.veiculo v join fetch v.unidade left join fetch p.tripulacao t left join fetch t.profissional "
            + "where p.inicio < ?2 and p.fim > ?1 order by p.inicio")
    List<PlantaoViatura> findNoIntervalo(Instant de, Instant ate);
}
