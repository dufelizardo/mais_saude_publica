package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.SinaisVitaisAph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SinaisVitaisAphRepository extends JpaRepository<SinaisVitaisAph, UUID> {

    List<SinaisVitaisAph> findByFicha_UuidOrderByMedidoEmAsc(UUID fichaId);
}
