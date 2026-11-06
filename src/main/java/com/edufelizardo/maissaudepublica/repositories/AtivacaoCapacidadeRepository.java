package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AtivacaoCapacidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AtivacaoCapacidadeRepository extends JpaRepository<AtivacaoCapacidade, UUID> {

    @Query("select a from AtivacaoCapacidade a join fetch a.capacidade c join fetch c.unidade where a.emergencia.uuid = ?1 order by a.inicio")
    List<AtivacaoCapacidade> findDaEmergencia(UUID emergenciaId);

    @Query("select coalesce(sum(a.quantidade), 0) from AtivacaoCapacidade a where a.capacidade.uuid = ?1 "
            + "and a.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoAtivacaoCapacidade.ATIVA")
    long somaAtivas(UUID capacidadeId);
}
