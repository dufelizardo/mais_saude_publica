package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TipoIntervencaoSaudeMental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TipoIntervencaoSaudeMentalRepository extends JpaRepository<TipoIntervencaoSaudeMental, UUID> {
    List<TipoIntervencaoSaudeMental> findAllByOrderByOrdemAsc();
}
