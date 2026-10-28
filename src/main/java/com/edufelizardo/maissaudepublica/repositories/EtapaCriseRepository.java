package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EtapaCrise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EtapaCriseRepository extends JpaRepository<EtapaCrise, UUID> {
    @Query("select e from EtapaCrise e left join fetch e.profissional where e.crise.uuid = ?1 order by e.ocorridaEm")
    List<EtapaCrise> findDaCrise(UUID criseId);

    @Query("select e.crise.uuid, min(e.ocorridaEm) from EtapaCrise e where e.crise.uuid in ?1 and e.etapa = ?2 group by e.crise.uuid")
    List<Object[]> findPrimeirasDaEtapa(java.util.Collection<UUID> criseIds, com.edufelizardo.maissaudepublica.models.enuns.EtapaCriseSaudeMental etapa);
}
