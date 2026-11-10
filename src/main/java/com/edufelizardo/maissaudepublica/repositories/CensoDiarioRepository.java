package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CensoDiario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CensoDiarioRepository extends JpaRepository<CensoDiario, UUID> {
    Optional<CensoDiario> findByUnidade_UuidAndDataAndReabertoFalse(UUID unidadeId, LocalDate data);

    List<CensoDiario> findByUnidade_UuidAndDataOrderByVersaoDesc(UUID unidadeId, LocalDate data);

    List<CensoDiario> findByUnidade_UuidAndDataBetweenOrderByDataDescVersaoDesc(UUID unidadeId, LocalDate de, LocalDate ate);
}
