package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RotaProgramada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RotaProgramadaRepository extends JpaRepository<RotaProgramada, UUID> {

    List<RotaProgramada> findByUnidade_UuidOrderByNomeAsc(UUID unidadeId);
}
