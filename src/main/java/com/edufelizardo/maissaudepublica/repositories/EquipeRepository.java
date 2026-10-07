package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipeRepository extends JpaRepository<Equipe, UUID> {
    List<Equipe> findAllByOrderByNomeAsc();

    boolean existsByUnidade_UuidAndNomeIgnoreCase(UUID unidadeId, String nome);

    Optional<Equipe> findByIne(String ine);
}
