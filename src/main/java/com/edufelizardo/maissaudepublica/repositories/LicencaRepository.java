package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Licenca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicencaRepository extends JpaRepository<Licenca, UUID> {
    boolean existsByAfastamento_Uuid(UUID afastamentoId);

    Optional<Licenca> findByAfastamento_Uuid(UUID afastamentoId);
}
