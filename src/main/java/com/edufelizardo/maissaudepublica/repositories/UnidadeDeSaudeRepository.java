package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UnidadeDeSaudeRepository extends JpaRepository<UnidadeDeSaude, UUID> {
    Optional<UnidadeDeSaude> findByNome(String nome);

    Optional<UnidadeDeSaude> findByTipo(int tipo);

    Optional<UnidadeDeSaude> findByNomeAndTipo(String nome, int tipo);
}
