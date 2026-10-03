package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PedidoExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoExameRepository extends JpaRepository<PedidoExame, UUID> {
    List<PedidoExame> findAllByOrderBySolicitadoEmDesc();

    List<PedidoExame> findByPaciente_UuidOrderBySolicitadoEmDesc(UUID pacienteId);
}
