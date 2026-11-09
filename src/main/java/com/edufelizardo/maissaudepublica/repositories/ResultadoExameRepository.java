package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ResultadoExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResultadoExameRepository extends JpaRepository<ResultadoExame, UUID> {
    List<ResultadoExame> findByItem_UuidOrderByRegistradoEmAsc(UUID itemId);

    /** Todos os resultados dos itens, para saber quais foram retificados (evolução do exame, ADR-0180). */
    List<ResultadoExame> findByItem_UuidIn(java.util.Collection<UUID> itemIds);

    /** Paciente, data e valor do resultado vigente liberado do exame, no intervalo (indicadores, ADR-0141). */
    @org.springframework.data.jpa.repository.Query("select r.item.pedido.paciente.uuid, r.liberadoEm, r.valorNumerico from ResultadoExame r "
            + "where r.item.resultadoAtual = r and r.item.exame.uuid = ?1 and r.item.pedido.paciente.uuid in ?2 "
            + "and r.liberadoEm >= ?3 and r.liberadoEm < ?4 and r.valorNumerico is not null")
    List<Object[]> findValoresNoIntervalo(UUID exameId, java.util.Collection<UUID> pacienteIds, java.time.Instant de, java.time.Instant ate);
}
