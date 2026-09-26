package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EvolucaoEnfermagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EvolucaoEnfermagemRepository extends JpaRepository<EvolucaoEnfermagem, UUID> {
    /**
     * Todas as evoluções de um atendimento — usado pela agregação do Prontuário (ver ADR-0045/ADR-0048).
     */
    List<EvolucaoEnfermagem> findByAtendimentoUuid(UUID atendimentoUuid);
}
