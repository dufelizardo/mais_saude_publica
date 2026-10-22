package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AvaliacaoSaudeMental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AvaliacaoSaudeMentalRepository extends JpaRepository<AvaliacaoSaudeMental, UUID> {
    @Query("select a from AvaliacaoSaudeMental a left join fetch a.profissional where a.acompanhamento.uuid = ?1 order by a.realizadaEm desc")
    List<AvaliacaoSaudeMental> findDoAcompanhamento(UUID acompanhamentoId);

    /** A última avaliação de cada caso, linhas [acompanhamento, realizadaEm]. */
    @Query("select a.acompanhamento.uuid, max(a.realizadaEm) from AvaliacaoSaudeMental a where a.acompanhamento.uuid in ?1 "
            + "group by a.acompanhamento.uuid")
    List<Object[]> findUltimas(Collection<UUID> acompanhamentoIds);
}
