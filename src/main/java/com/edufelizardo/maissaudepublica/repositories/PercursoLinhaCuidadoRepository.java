package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PercursoLinhaCuidado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PercursoLinhaCuidadoRepository extends JpaRepository<PercursoLinhaCuidado, UUID> {

    @Query("select p from PercursoLinhaCuidado p join fetch p.paciente join fetch p.linha l left join fetch l.programa join fetch p.unidade "
            + "left join fetch p.equipe where p.paciente.uuid = ?1 order by p.inicio desc")
    List<PercursoLinhaCuidado> findDoPaciente(UUID pacienteId);

    @Query("select p from PercursoLinhaCuidado p join fetch p.paciente join fetch p.linha l left join fetch l.programa join fetch p.unidade "
            + "left join fetch p.equipe where p.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoPercursoLinha.EM_CURSO")
    List<PercursoLinhaCuidado> findEmCurso();

    boolean existsByPaciente_UuidAndLinha_UuidAndSituacao(UUID pacienteId, UUID linhaId,
                                                         com.edufelizardo.maissaudepublica.models.enuns.SituacaoPercursoLinha situacao);

    long countByLinha_UuidAndSituacao(UUID linhaId, com.edufelizardo.maissaudepublica.models.enuns.SituacaoPercursoLinha situacao);
}
