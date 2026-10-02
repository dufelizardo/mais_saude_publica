package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoSituacaoUnidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoSituacaoUnidadeRepository extends JpaRepository<EventoSituacaoUnidade, UUID> {
    List<EventoSituacaoUnidade> findByUnidade_UuidOrderByOcorridoEmDesc(UUID unidadeId);
}
