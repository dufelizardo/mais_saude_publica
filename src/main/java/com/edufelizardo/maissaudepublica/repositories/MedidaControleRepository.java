package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MedidaControle;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaControle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MedidaControleRepository extends JpaRepository<MedidaControle, UUID> {
    List<MedidaControle> findBySurto_UuidOrderByCriadaEmAsc(UUID surtoId);

    List<MedidaControle> findByNotificacao_UuidOrderByCriadaEmAsc(UUID notificacaoId);

    List<MedidaControle> findBySituacaoIn(Collection<SituacaoMedidaControle> situacoes);

    boolean existsBySurto_UuidAndSituacaoIn(UUID surtoId, Collection<SituacaoMedidaControle> situacoes);
}
