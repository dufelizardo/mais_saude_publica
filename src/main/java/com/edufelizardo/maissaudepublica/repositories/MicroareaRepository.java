package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Microarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MicroareaRepository extends JpaRepository<Microarea, UUID> {
    List<Microarea> findAllByOrderByCodigoAsc();

    List<Microarea> findByArea_UuidOrderByCodigoAsc(UUID areaId);

    /** As microáreas dos territórios, para a cobertura da campanha (ADR-0196). */
    @org.springframework.data.jpa.repository.Query("select m from Microarea m join fetch m.area a join fetch a.territorio where a.territorio.uuid in ?1")
    List<Microarea> findDosTerritorios(java.util.Collection<UUID> territorioIds);

    Optional<Microarea> findByArea_UuidAndCodigoIgnoreCase(UUID areaId, String codigo);
}
