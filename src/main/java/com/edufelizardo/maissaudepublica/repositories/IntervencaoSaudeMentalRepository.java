package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.IntervencaoSaudeMental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IntervencaoSaudeMentalRepository extends JpaRepository<IntervencaoSaudeMental, UUID> {
    @Query("select i from IntervencaoSaudeMental i join fetch i.tipo left join fetch i.profissional left join fetch i.compartilhadoCom "
            + "left join fetch i.prevista left join fetch i.equipeMatriciada where i.acompanhamento.uuid = ?1 order by i.realizadaEm desc")
    List<IntervencaoSaudeMental> findDoAcompanhamento(UUID acompanhamentoId);
}
