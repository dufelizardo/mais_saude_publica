package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Microarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MicroareaRepository extends JpaRepository<Microarea, UUID> {
    List<Microarea> findAllByOrderByCodigoAsc();

    List<Microarea> findByArea_UuidOrderByCodigoAsc(UUID areaId);

    Optional<Microarea> findByArea_UuidAndCodigoIgnoreCase(UUID areaId, String codigo);
}
