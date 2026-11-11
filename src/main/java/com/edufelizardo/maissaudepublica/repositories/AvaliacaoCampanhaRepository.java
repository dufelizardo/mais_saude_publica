package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AvaliacaoCampanha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoCampanhaRepository extends JpaRepository<AvaliacaoCampanha, UUID> {

    Optional<AvaliacaoCampanha> findByCampanha_Uuid(UUID campanhaId);
}
