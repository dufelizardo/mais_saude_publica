package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcompanhamentoProgramatico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AcompanhamentoProgramaticoRepository extends JpaRepository<AcompanhamentoProgramatico, UUID> {
    @Query("select a from AcompanhamentoProgramatico a join fetch a.programa join fetch a.unidade left join fetch a.equipe "
            + "left join fetch a.profissional where a.paciente.uuid = ?1 order by a.inicio desc")
    List<AcompanhamentoProgramatico> findDoPaciente(UUID pacienteId);

    @Query("select a from AcompanhamentoProgramatico a join fetch a.paciente join fetch a.unidade left join fetch a.equipe "
            + "left join fetch a.profissional where a.programa.uuid = ?1 and a.fim is null")
    List<AcompanhamentoProgramatico> findVigentesDoPrograma(UUID programaId);

    @Query("select a from AcompanhamentoProgramatico a join fetch a.programa where a.fim is null")
    List<AcompanhamentoProgramatico> findVigentes();

    boolean existsByPaciente_UuidAndPrograma_UuidAndFimIsNull(UUID pacienteId, UUID programaId);
}
