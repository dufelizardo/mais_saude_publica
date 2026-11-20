package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AditivoContratual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AditivoContratualRepository extends JpaRepository<AditivoContratual, UUID> {

    @Query("select a from AditivoContratual a where a.instrumento.uuid in :ids order by a.numero")
    List<AditivoContratual> findDosInstrumentos(@Param("ids") Collection<UUID> ids);
}
