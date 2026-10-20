package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.FichaAgravo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface FichaAgravoRepository extends JpaRepository<FichaAgravo, UUID> {
    Optional<FichaAgravo> findFirstByAgravo_UuidOrderByVersaoDesc(UUID agravoId);

    Optional<FichaAgravo> findByAgravo_UuidAndVersao(UUID agravoId, int versao);
}
