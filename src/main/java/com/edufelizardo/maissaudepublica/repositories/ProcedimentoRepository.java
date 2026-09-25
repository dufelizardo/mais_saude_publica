package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Procedimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcedimentoRepository extends JpaRepository<Procedimento, UUID> {
    /**
     * Todos os procedimentos de uma consulta — usado pela agregação do Prontuário (ver ADR-0045).
     */
    List<Procedimento> findByConsultaUuid(UUID consultaUuid);
}
