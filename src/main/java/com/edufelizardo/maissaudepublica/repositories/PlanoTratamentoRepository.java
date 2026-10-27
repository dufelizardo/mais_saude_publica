package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PlanoTratamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoTratamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanoTratamentoRepository extends JpaRepository<PlanoTratamento, UUID> {
    boolean existsByPaciente_UuidAndSituacao(UUID pacienteId, SituacaoPlanoTratamento situacao);

    List<PlanoTratamento> findByPaciente_UuidOrderByAbertoEmDesc(UUID pacienteId);

    /** Planos abertos no intervalo (indicadores, ADR-0141). */
    @org.springframework.data.jpa.repository.Query("select p from PlanoTratamento p join fetch p.unidade where p.abertoEm >= ?1 and p.abertoEm < ?2")
    List<PlanoTratamento> findAbertosNoIntervalo(java.time.Instant de, java.time.Instant ate);
}
