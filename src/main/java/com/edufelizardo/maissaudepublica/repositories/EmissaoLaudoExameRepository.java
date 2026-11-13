package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EmissaoLaudoExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmissaoLaudoExameRepository extends JpaRepository<EmissaoLaudoExame, UUID> {
    List<EmissaoLaudoExame> findByPedido_UuidOrderByVersaoDesc(UUID pedidoId);

    Optional<EmissaoLaudoExame> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
