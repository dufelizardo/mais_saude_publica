package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcolhimentoSaudeMental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AcolhimentoSaudeMentalRepository extends JpaRepository<AcolhimentoSaudeMental, UUID> {
    @Query("select a from AcolhimentoSaudeMental a join fetch a.unidade left join fetch a.profissional left join fetch a.acompanhamento "
            + "where a.paciente.uuid = ?1 order by a.realizadoEm desc")
    List<AcolhimentoSaudeMental> findDoPaciente(UUID pacienteId);
}
