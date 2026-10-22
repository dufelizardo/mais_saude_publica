package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcompanhamentoSaudeMental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcompanhamentoSaudeMentalRepository extends JpaRepository<AcompanhamentoSaudeMental, UUID> {
    @Query("select a from AcompanhamentoSaudeMental a join fetch a.paciente join fetch a.unidade join fetch a.acompanhamentoProgramatico "
            + "where a.paciente.uuid = ?1 and a.fim is null")
    Optional<AcompanhamentoSaudeMental> findVigenteDoPaciente(UUID pacienteId);

    @Query("select a from AcompanhamentoSaudeMental a join fetch a.paciente join fetch a.unidade join fetch a.acompanhamentoProgramatico "
            + "where a.fim is null")
    List<AcompanhamentoSaudeMental> findVigentes();

    @Query("select a from AcompanhamentoSaudeMental a join fetch a.paciente join fetch a.unidade join fetch a.acompanhamentoProgramatico "
            + "where a.paciente.uuid = ?1 order by a.inicio desc")
    List<AcompanhamentoSaudeMental> findDoPaciente(UUID pacienteId);

    boolean existsByAcompanhamentoProgramatico_UuidAndFimIsNull(UUID acompanhamentoProgramaticoId);
}
