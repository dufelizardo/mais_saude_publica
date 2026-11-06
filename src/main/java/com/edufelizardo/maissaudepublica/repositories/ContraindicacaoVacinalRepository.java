package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ContraindicacaoVacinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContraindicacaoVacinalRepository extends JpaRepository<ContraindicacaoVacinal, UUID> {

    /** As do paciente, com o imunobiológico (a caderneta, ADR-0150). */
    @Query("select c from ContraindicacaoVacinal c join fetch c.imunobiologico where c.paciente.uuid = ?1 order by c.desde")
    List<ContraindicacaoVacinal> findDoPaciente(UUID pacienteId);

    Optional<ContraindicacaoVacinal> findByPaciente_UuidAndImunobiologico_Uuid(UUID pacienteId, UUID imunobiologicoId);
}
