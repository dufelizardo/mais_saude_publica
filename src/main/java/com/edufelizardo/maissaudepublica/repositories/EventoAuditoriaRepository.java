package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoAuditoria;
import org.springframework.data.repository.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Só inclusão e leitura (ADR-0070): de propósito não estende {@code JpaRepository}, para não oferecer
 * {@code delete} nem {@code deleteAll} à trilha de auditoria.
 */
public interface EventoAuditoriaRepository extends Repository<EventoAuditoria, UUID> {

    EventoAuditoria save(EventoAuditoria evento);

    List<EventoAuditoria> findByUsuarioCpfAndOcorridoEmGreaterThanEqualOrderByOcorridoEmAsc(String usuarioCpf, Instant desde);

    List<EventoAuditoria> findByPacienteIdOrderByOcorridoEmDesc(UUID pacienteId);

    List<EventoAuditoria> findByRegistroIdOrderByOcorridoEmDesc(UUID registroId);
}
