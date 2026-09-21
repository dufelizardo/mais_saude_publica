package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RegraAnuenio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RegraAnuenioRepository extends JpaRepository<RegraAnuenio, UUID> {
    Optional<RegraAnuenio> findByCategoria_Uuid(UUID categoriaId);

    boolean existsByCategoria_Uuid(UUID categoriaId);
}
