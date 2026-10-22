package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ResponsabilidadeTerritorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResponsabilidadeTerritorialRepository extends JpaRepository<ResponsabilidadeTerritorial, UUID> {
    List<ResponsabilidadeTerritorial> findByFimIsNull();

    Optional<ResponsabilidadeTerritorial> findByArea_UuidAndFimIsNull(UUID areaId);

    Optional<ResponsabilidadeTerritorial> findByEquipe_UuidAndFimIsNull(UUID equipeId);

    List<ResponsabilidadeTerritorial> findByArea_UuidOrderByInicioDesc(UUID areaId);
}
