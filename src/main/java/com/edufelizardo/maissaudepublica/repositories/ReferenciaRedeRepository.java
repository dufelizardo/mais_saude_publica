package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ReferenciaRede;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReferenciaRedeRepository extends JpaRepository<ReferenciaRede, UUID> {

    @Query("select r from ReferenciaRede r join fetch r.origem join fetch r.executante join fetch r.procedimento order by r.inicio desc")
    List<ReferenciaRede> findTodas();

    @Query("select r from ReferenciaRede r join fetch r.origem join fetch r.executante where r.procedimento.uuid = ?1")
    List<ReferenciaRede> findDoProcedimento(UUID procedimentoId);

    /** A aberta (sem fim) da origem e do procedimento, travada para a troca não correr duas vezes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReferenciaRede r where r.origem.uuid = ?1 and r.procedimento.uuid = ?2 and r.fim is null")
    Optional<ReferenciaRede> findAbertaParaAtualizar(UUID origemId, UUID procedimentoId);
}
