package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.LocalCampanha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LocalCampanhaRepository extends JpaRepository<LocalCampanha, UUID> {
    List<LocalCampanha> findByCampanha_Uuid(UUID campanhaId);
}
