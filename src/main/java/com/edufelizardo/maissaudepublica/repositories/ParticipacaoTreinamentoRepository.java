package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ParticipacaoTreinamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParticipacaoTreinamentoRepository extends JpaRepository<ParticipacaoTreinamento, UUID> {
    List<ParticipacaoTreinamento> findByProfissional_MatriculaOrderByDataConclusaoDesc(String matricula);
}
