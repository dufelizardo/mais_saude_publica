package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.FolhaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FolhaPagamentoRepository extends JpaRepository<FolhaPagamento, UUID> {
    boolean existsByProfissional_MatriculaAndCompetencia(String matricula, String competencia);

    List<FolhaPagamento> findByProfissional_MatriculaOrderByCompetenciaDesc(String matricula);

    List<FolhaPagamento> findByCompetenciaOrderByProfissional_NomeAsc(String competencia);
}
