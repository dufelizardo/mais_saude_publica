package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AreaRepository extends JpaRepository<Area, UUID> {
    List<Area> findAllByOrderByCodigoAsc();

    Optional<Area> findByTerritorio_UuidAndCodigoIgnoreCase(UUID territorioId, String codigo);
}
