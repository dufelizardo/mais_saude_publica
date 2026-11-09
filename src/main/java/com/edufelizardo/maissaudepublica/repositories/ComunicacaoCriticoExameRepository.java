package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ComunicacaoCriticoExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ComunicacaoCriticoExameRepository extends JpaRepository<ComunicacaoCriticoExame, UUID> {
    boolean existsByResultado_Uuid(UUID resultadoId);

    List<ComunicacaoCriticoExame> findByResultado_UuidIn(Collection<UUID> resultadoIds);

    List<ComunicacaoCriticoExame> findByItem_Pedido_UuidOrderByComunicadoEmAsc(UUID pedidoId);
}
