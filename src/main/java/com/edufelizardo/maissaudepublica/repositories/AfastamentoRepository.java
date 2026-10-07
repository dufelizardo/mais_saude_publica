package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAfastamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;

import java.util.List;
import java.util.UUID;

public interface AfastamentoRepository extends JpaRepository<Afastamento, UUID> {
    List<Afastamento> findByProfissional_MatriculaOrderByDataInicioDesc(String matricula);

    /** Afastamentos em curso na data (período cobre a data e status em um dos informados) — ADR-0072. */
    @Query("select a from Afastamento a where a.status in :status and a.dataInicio <= :data and a.dataFim >= :data")
    List<Afastamento> findVigentesEm(LocalDate data, Collection<StatusAfastamento> status);

    /** Afastamentos que tocam o período [de, ate], com status em um dos informados — ADR-0105. */
    @Query("select a from Afastamento a join fetch a.profissional where a.status in :status and a.dataInicio <= :ate and a.dataFim >= :de")
    List<Afastamento> findQueTocam(LocalDate de, LocalDate ate, Collection<StatusAfastamento> status);
}
