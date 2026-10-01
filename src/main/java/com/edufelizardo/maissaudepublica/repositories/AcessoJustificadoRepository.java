package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcessoJustificado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcessoJustificadoRepository extends JpaRepository<AcessoJustificado, UUID> {

    /** O acesso justificado ainda válido deste usuário para este paciente, o que vence por último. */
    Optional<AcessoJustificado> findFirstByUsuarioCpfAndPaciente_UuidAndExpiraEmAfterOrderByExpiraEmDesc(
            String usuarioCpf, UUID pacienteId, Instant agora);

    List<AcessoJustificado> findByPaciente_Uuid(UUID pacienteId);
}
