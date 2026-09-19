package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TabelaSalarial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TabelaSalarialRepository extends JpaRepository<TabelaSalarial, UUID> {
    List<TabelaSalarial> findByCargo_UuidOrderByDataVigenciaDesc(UUID cargoId);

    Optional<TabelaSalarial> findFirstByCargo_UuidAndDataVigenciaLessThanEqualOrderByDataVigenciaDesc(
            UUID cargoId, LocalDate data);
}
