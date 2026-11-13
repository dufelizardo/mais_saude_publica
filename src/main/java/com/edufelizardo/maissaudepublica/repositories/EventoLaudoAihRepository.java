package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoLaudoAih;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventoLaudoAihRepository extends JpaRepository<EventoLaudoAih, UUID> {

    List<EventoLaudoAih> findByLaudo_UuidOrderByOcorridoEmAsc(UUID laudoId);
}
