package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Triagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TriagemRepository extends JpaRepository<Triagem, UUID> {
    /**
     * Todas as triagens de um atendimento — usado pela agregação do Prontuário (ver ADR-0045/ADR-0047).
     */
    List<Triagem> findByAtendimentoUuid(UUID atendimentoUuid);
}
