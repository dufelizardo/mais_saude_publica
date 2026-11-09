package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RealizacaoExameImagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RealizacaoExameImagemRepository extends JpaRepository<RealizacaoExameImagem, UUID> {
    Optional<RealizacaoExameImagem> findByItem_Uuid(UUID itemId);

    List<RealizacaoExameImagem> findByItem_Pedido_UuidOrderByRealizadaEmAsc(UUID pedidoId);

    List<RealizacaoExameImagem> findByItem_UuidIn(Collection<UUID> itemIds);
}
