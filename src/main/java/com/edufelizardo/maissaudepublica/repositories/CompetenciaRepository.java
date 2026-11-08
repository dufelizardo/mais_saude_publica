package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Competencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetenciaRepository extends JpaRepository<Competencia, UUID> {

    List<Competencia> findAllByOrderByNomeAsc();

    Optional<Competencia> findByCodigo(String codigo);
}
