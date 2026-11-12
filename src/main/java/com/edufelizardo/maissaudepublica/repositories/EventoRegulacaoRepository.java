package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoRegulacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoRegulacaoRepository extends JpaRepository<EventoRegulacao, UUID> {
    List<EventoRegulacao> findBySolicitacao_UuidOrderByOcorridoEmAsc(UUID solicitacaoId);

    /** Os eventos de um tipo nos procedimentos e no período, com a solicitação (o tempo até a autorização, ADR-0198). */
    @org.springframework.data.jpa.repository.Query("select e from EventoRegulacao e join fetch e.solicitacao s where e.tipo = ?1 "
            + "and s.procedimento.uuid in ?2 and e.ocorridoEm >= ?3 and e.ocorridoEm < ?4")
    List<EventoRegulacao> findDoTipoNosProcedimentos(com.edufelizardo.maissaudepublica.models.enuns.TipoEventoRegulacao tipo,
                                                     java.util.Collection<UUID> procedimentos, java.time.Instant de, java.time.Instant ate);
}
