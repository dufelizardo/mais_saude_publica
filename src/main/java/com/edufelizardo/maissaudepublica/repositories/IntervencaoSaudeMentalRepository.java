package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.IntervencaoSaudeMental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IntervencaoSaudeMentalRepository extends JpaRepository<IntervencaoSaudeMental, UUID> {
    @Query("select i from IntervencaoSaudeMental i join fetch i.tipo left join fetch i.profissional left join fetch i.compartilhadoCom "
            + "left join fetch i.prevista left join fetch i.equipeMatriciada where i.acompanhamento.uuid = ?1 order by i.realizadaEm desc")
    List<IntervencaoSaudeMental> findDoAcompanhamento(UUID acompanhamentoId);

    /** As realizadas desde o instante, com o tipo e a unidade do caso (o painel da RAPS, ADR-0153). */
    @Query("select i from IntervencaoSaudeMental i join fetch i.tipo join fetch i.acompanhamento a join fetch a.unidade where i.realizadaEm >= ?1 "
            + "and i.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoIntervencaoSaudeMental.REALIZADA")
    List<IntervencaoSaudeMental> findRealizadasDesde(java.time.Instant desde);
}
