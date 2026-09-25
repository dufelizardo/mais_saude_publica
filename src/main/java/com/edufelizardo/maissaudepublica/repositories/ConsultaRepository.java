package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {
    /**
     * Todas as consultas de um atendimento — usado pela agregação do Prontuário (ver ADR-0045).
     */
    List<Consulta> findByAtendimentoUuid(UUID atendimentoUuid);
}
