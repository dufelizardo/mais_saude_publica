package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EntregaResultadoExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EntregaResultadoExameRepository extends JpaRepository<EntregaResultadoExame, UUID> {
    List<EntregaResultadoExame> findByPedido_UuidOrderByEntregueEmAsc(UUID pedidoId);
}
