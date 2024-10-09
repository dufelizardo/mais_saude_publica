package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UnidadeDeSaudeRepository extends JpaRepository<UnidadeDeSaude, UUID> {
    Optional<UnidadeDeSaude> findByNome(String nome);
}
