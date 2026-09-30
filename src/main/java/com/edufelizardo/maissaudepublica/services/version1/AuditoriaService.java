package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.EventoAuditoria;
import com.edufelizardo.maissaudepublica.repositories.EventoAuditoriaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Grava a trilha de auditoria (ADR-0070). Chamado depois que a requisição terminou — a operação já foi
 * confirmada no banco —, com a transação própria do {@code save}: uma falha aqui não desfaz nem derruba nada.
 */
@Service
@Slf4j
public class AuditoriaService {

    @Autowired
    private EventoAuditoriaRepository repository;

    public void registrar(EventoAuditoria evento) {
        repository.save(evento);
    }

    /** Versão que nunca propaga erro — usada depois que a resposta já foi decidida. */
    public void registrarSemFalhar(EventoAuditoria evento) {
        try {
            registrar(evento);
        } catch (RuntimeException ex) {
            log.error("Falha ao gravar evento de auditoria {} {} {}: {}", evento.getAcao(), evento.getMetodo(),
                    evento.getRota(), ex.getMessage());
        }
    }
}
