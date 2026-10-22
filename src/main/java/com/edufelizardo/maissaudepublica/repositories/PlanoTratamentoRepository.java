package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PlanoTratamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoTratamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanoTratamentoRepository extends JpaRepository<PlanoTratamento, UUID> {
    boolean existsByPaciente_UuidAndSituacao(UUID pacienteId, SituacaoPlanoTratamento situacao);

    List<PlanoTratamento> findByPaciente_UuidOrderByAbertoEmDesc(UUID pacienteId);
}
