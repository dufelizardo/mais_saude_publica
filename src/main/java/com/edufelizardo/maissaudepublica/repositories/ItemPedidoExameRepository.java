package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.enuns.StatusItemExame;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemPedidoExameRepository extends JpaRepository<ItemPedidoExame, UUID> {
    List<ItemPedidoExame> findByPedido_Uuid(UUID pedidoId);

    List<ItemPedidoExame> findByPedido_UuidIn(java.util.Collection<UUID> pedidoIds);

    List<ItemPedidoExame> findByStatus(StatusItemExame status);

    List<ItemPedidoExame> findByAmostra_Uuid(UUID amostraId);

    /** Trava o item: dois profissionais não registram ou liberam o mesmo exame ao mesmo tempo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from ItemPedidoExame i where i.uuid = :uuid")
    Optional<ItemPedidoExame> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
