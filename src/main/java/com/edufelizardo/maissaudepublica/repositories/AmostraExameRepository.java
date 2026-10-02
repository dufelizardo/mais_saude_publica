package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AmostraExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AmostraExameRepository extends JpaRepository<AmostraExame, UUID> {
    List<AmostraExame> findByPedido_UuidOrderByColetadaEmAsc(UUID pedidoId);

    boolean existsByCodigo(String codigo);
}
