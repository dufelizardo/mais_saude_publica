package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoRegulacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoRegulacaoRepository extends JpaRepository<EventoRegulacao, UUID> {
    List<EventoRegulacao> findBySolicitacao_UuidOrderByOcorridoEmAsc(UUID solicitacaoId);
}
