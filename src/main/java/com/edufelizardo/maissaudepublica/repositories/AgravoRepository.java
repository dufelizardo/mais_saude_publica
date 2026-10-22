package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Agravo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgravoRepository extends JpaRepository<Agravo, UUID> {
    List<Agravo> findAllByOrderByNomeAsc();

    Optional<Agravo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
