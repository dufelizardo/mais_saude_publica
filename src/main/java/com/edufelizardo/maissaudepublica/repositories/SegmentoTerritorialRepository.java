package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.SegmentoTerritorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SegmentoTerritorialRepository extends JpaRepository<SegmentoTerritorial, UUID> {
    List<SegmentoTerritorial> findAllByOrderByNomeAsc();

    Optional<SegmentoTerritorial> findByCodigoIgnoreCase(String codigo);
}
