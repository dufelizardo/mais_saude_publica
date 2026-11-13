package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ReservaEstoque;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaEstoqueRepository extends JpaRepository<ReservaEstoque, UUID> {

    @Query("select r from ReservaEstoque r join fetch r.unidade join fetch r.material join fetch r.emergencia "
            + "where r.unidade.uuid = ?1 and r.material.uuid = ?2 "
            + "and r.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoReservaEstoque.ATIVA order by r.criadaEm")
    List<ReservaEstoque> findAtivas(UUID unidadeId, UUID materialId);

    @Query("select r from ReservaEstoque r join fetch r.material join fetch r.emergencia where r.unidade.uuid = ?1 "
            + "and r.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoReservaEstoque.ATIVA")
    List<ReservaEstoque> findAtivasDaUnidade(UUID unidadeId);

    @Query("select r from ReservaEstoque r join fetch r.unidade join fetch r.material join fetch r.emergencia order by r.criadaEm desc")
    List<ReservaEstoque> findTodas();

    @Query("select r from ReservaEstoque r join fetch r.unidade join fetch r.material where r.emergencia.uuid = ?1 order by r.criadaEm")
    List<ReservaEstoque> findDaEmergencia(UUID emergenciaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReservaEstoque r where r.uuid = ?1")
    Optional<ReservaEstoque> findByIdParaAtualizar(UUID uuid);
}
