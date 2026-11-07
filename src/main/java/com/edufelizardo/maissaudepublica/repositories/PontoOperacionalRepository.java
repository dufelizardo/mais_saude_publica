package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PontoOperacional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PontoOperacionalRepository extends JpaRepository<PontoOperacional, UUID> {

    List<PontoOperacional> findByUnidade_UuidOrderByNomeAsc(UUID unidadeId);
}
