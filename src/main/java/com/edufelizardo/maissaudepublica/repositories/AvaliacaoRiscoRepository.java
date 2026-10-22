package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AvaliacaoRisco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AvaliacaoRiscoRepository extends JpaRepository<AvaliacaoRisco, UUID> {
    @Query("select a from AvaliacaoRisco a left join fetch a.profissional where a.acompanhamento.uuid = ?1 order by a.avaliadaEm desc")
    List<AvaliacaoRisco> findDoAcompanhamento(UUID acompanhamentoId);

    @Query("select a from AvaliacaoRisco a where a.acompanhamento.uuid in ?1 order by a.avaliadaEm desc")
    List<AvaliacaoRisco> findDosAcompanhamentos(Collection<UUID> acompanhamentoIds);
}
