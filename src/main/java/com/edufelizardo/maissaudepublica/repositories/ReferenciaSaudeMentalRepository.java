package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ReferenciaSaudeMental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReferenciaSaudeMentalRepository extends JpaRepository<ReferenciaSaudeMental, UUID> {
    @Query("select r from ReferenciaSaudeMental r join fetch r.equipe join fetch r.tecnico where r.acompanhamento.uuid in ?1 "
            + "order by r.inicio desc")
    List<ReferenciaSaudeMental> findDosAcompanhamentos(Collection<UUID> acompanhamentoIds);

    /** As referências vigentes dos casos vigentes do paciente: base do vínculo da equipe de referência (ADR-0120). */
    @Query("select r from ReferenciaSaudeMental r join fetch r.equipe join fetch r.tecnico join r.acompanhamento a "
            + "where a.paciente.uuid = ?1 and a.fim is null and r.fim is null")
    List<ReferenciaSaudeMental> findVigentesDoPaciente(UUID pacienteId);
}
