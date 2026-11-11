package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PacienteDeslocado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PacienteDeslocadoRepository extends JpaRepository<PacienteDeslocado, UUID> {

    @Query("select d from PacienteDeslocado d join fetch d.paciente left join fetch d.destinoUnidade left join fetch d.destinoInstituicao "
            + "left join fetch d.origemUnidade left join fetch d.solicitacaoTransporte where d.emergencia.uuid = ?1 order by d.saidaEm")
    List<PacienteDeslocado> findDaEmergencia(UUID emergenciaId);

    @Query("select d from PacienteDeslocado d join fetch d.emergencia left join fetch d.destinoUnidade left join fetch d.destinoInstituicao "
            + "where d.paciente.uuid = ?1 and d.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoDeslocamento.DESLOCADO")
    List<PacienteDeslocado> findAbertosDoPaciente(UUID pacienteId);
}
