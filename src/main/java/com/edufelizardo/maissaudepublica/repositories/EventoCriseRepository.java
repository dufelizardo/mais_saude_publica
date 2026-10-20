package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoCrise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EventoCriseRepository extends JpaRepository<EventoCrise, UUID> {
    @Query("select c from EventoCrise c join fetch c.paciente join fetch c.unidade left join fetch c.acompanhamento left join fetch c.profissional "
            + "order by c.identificadaEm desc")
    List<EventoCrise> findTodas();

    @Query("select c from EventoCrise c join fetch c.unidade left join fetch c.profissional where c.paciente.uuid = ?1 order by c.identificadaEm desc")
    List<EventoCrise> findDoPaciente(UUID pacienteId);
}
