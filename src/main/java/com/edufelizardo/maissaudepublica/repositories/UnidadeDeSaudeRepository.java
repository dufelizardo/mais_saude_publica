package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnidadeDeSaudeRepository extends JpaRepository<UnidadeDeSaude, UUID> {
    Optional<UnidadeDeSaude> findByNome(String nome);

    List<UnidadeDeSaude> findByTipo(TipoUnidadeDeSaude tipo);

    List<UnidadeDeSaude> findByNomeAndTipo(String nome, TipoUnidadeDeSaude tipo);
}
