package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemRecebimentoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ItemRecebimentoCompraRepository extends JpaRepository<ItemRecebimentoCompra, UUID> {

    @Query("select i from ItemRecebimentoCompra i where i.recebimento.uuid in :ids")
    List<ItemRecebimentoCompra> findDosRecebimentos(@Param("ids") Collection<UUID> ids);
}
