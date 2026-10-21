package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RegistroOdontograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RegistroOdontogramaRepository extends JpaRepository<RegistroOdontograma, UUID> {
    @Query("select r from RegistroOdontograma r join fetch r.condicao where r.paciente.uuid = ?1 order by r.data asc, r.registradoEm asc")
    List<RegistroOdontograma> findDoPaciente(UUID pacienteId);
}
