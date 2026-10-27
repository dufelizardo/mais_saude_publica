package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcaoSaudeBucal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcaoSaudeBucalRepository extends JpaRepository<AcaoSaudeBucal, UUID> {
    Optional<AcaoSaudeBucal> findByAcao_Uuid(UUID acaoId);

    /** O detalhe das ações coletivas realizadas no intervalo (indicadores, ADR-0141). */
    @org.springframework.data.jpa.repository.Query("select d from AcaoSaudeBucal d join fetch d.acao a join fetch a.unidade "
            + "where a.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica.REALIZADA and a.realizadaEm >= ?1 and a.realizadaEm <= ?2")
    java.util.List<AcaoSaudeBucal> findRealizadasNoIntervalo(java.time.LocalDate de, java.time.LocalDate ate);
}
