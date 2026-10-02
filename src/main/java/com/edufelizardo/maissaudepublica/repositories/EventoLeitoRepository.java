package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoLeito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoLeitoRepository extends JpaRepository<EventoLeito, UUID> {
    List<EventoLeito> findByInternacao_UuidOrderByOcorridoEmAsc(UUID internacaoId);

    List<EventoLeito> findByLeito_UuidOrderByOcorridoEmDesc(UUID leitoId);
}
