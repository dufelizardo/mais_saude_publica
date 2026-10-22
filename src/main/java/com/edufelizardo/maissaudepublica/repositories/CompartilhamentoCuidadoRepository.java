package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CompartilhamentoCuidado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CompartilhamentoCuidadoRepository extends JpaRepository<CompartilhamentoCuidado, UUID> {
    @Query("select c from CompartilhamentoCuidado c join fetch c.equipe e join fetch e.unidade where c.acompanhamento.uuid = ?1 "
            + "order by c.inicio desc")
    List<CompartilhamentoCuidado> findDoAcompanhamento(UUID acompanhamentoId);

    /** Os compartilhamentos vigentes dos casos vigentes do paciente: base do vínculo da equipe parceira (ADR-0122). */
    @Query("select c from CompartilhamentoCuidado c join fetch c.equipe join c.acompanhamento a "
            + "where a.paciente.uuid = ?1 and a.fim is null and c.fim is null")
    List<CompartilhamentoCuidado> findVigentesDoPaciente(UUID pacienteId);
}
