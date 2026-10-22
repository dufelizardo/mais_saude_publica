package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CategoriaPrograma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaProgramaRepository extends JpaRepository<CategoriaPrograma, UUID> {
    List<CategoriaPrograma> findAllByOrderByOrdemAscNomeAsc();

    Optional<CategoriaPrograma> findByNomeIgnoreCase(String nome);
}
