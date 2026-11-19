package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RecebimentoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RecebimentoCompraRepository extends JpaRepository<RecebimentoCompra, UUID> {

    @Query("select r from RecebimentoCompra r where r.ordem.uuid in :ids order by r.recebidoEm, r.registradoEm")
    List<RecebimentoCompra> findDasOrdens(@Param("ids") Collection<UUID> ids);

    @Query("select r from RecebimentoCompra r join fetch r.ordem o join fetch o.instrumento i join fetch i.fornecedor join fetch o.unidade left join fetch o.empenhoRef where r.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoRecebimentoCompra.DEFINITIVO")
    List<RecebimentoCompra> findDefinitivos();
}
