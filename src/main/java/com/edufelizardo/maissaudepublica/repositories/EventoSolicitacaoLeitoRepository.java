package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoSolicitacaoLeito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoSolicitacaoLeitoRepository extends JpaRepository<EventoSolicitacaoLeito, UUID> {
    List<EventoSolicitacaoLeito> findBySolicitacao_UuidOrderByOcorridoEmAsc(UUID solicitacaoId);
}
