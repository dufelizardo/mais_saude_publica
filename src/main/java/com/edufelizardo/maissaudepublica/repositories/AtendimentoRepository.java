package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AtendimentoRepository extends JpaRepository<Atendimento, UUID> {
    /**
     * Todos os atendimentos de um paciente — usado pela agregação do Prontuário (ver ADR-0045).
     */
    List<Atendimento> findByPacienteUuid(UUID pacienteUuid);
}
