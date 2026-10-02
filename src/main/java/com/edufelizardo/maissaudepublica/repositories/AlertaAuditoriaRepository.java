package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlertaAuditoria;
import org.springframework.data.repository.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Sem exclusão (ADR-0096): o alerta termina com parecer, não some. */
public interface AlertaAuditoriaRepository extends Repository<AlertaAuditoria, UUID> {

    AlertaAuditoria save(AlertaAuditoria alerta);

    Optional<AlertaAuditoria> findById(UUID uuid);

    List<AlertaAuditoria> findAllByOrderByDetectadoEmDesc();

    boolean existsByChave(String chave);

    /** O episódio aberto do mesmo padrão e do mesmo sujeito que ainda está "quente": a detecção soma nele. */
    Optional<AlertaAuditoria> findFirstByTipoAndSujeitoAndStatusAndUltimoEventoEmAfterOrderByUltimoEventoEmDesc(
            TipoAlertaAuditoria tipo, String sujeito, StatusAlertaAuditoria status, Instant depoisDe);
}
