package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcaoSaudeBucal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcaoSaudeBucalRepository extends JpaRepository<AcaoSaudeBucal, UUID> {
    Optional<AcaoSaudeBucal> findByAcao_Uuid(UUID acaoId);
}
