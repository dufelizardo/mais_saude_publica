package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CondicaoOdontologica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CondicaoOdontologicaRepository extends JpaRepository<CondicaoOdontologica, UUID> {
    List<CondicaoOdontologica> findAllByOrderByNomeAsc();

    boolean existsByCodigo(String codigo);
}
