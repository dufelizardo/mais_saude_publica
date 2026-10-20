package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Territorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TerritorioRepository extends JpaRepository<Territorio, UUID> {
    List<Territorio> findAllByOrderByNomeAsc();

    boolean existsBySegmento_UuidAndNomeIgnoreCase(UUID segmentoId, String nome);
}
