package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.DotacaoOrcamentaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DotacaoOrcamentariaRepository extends JpaRepository<DotacaoOrcamentaria, UUID> {

    @Query("select d from DotacaoOrcamentaria d order by d.exercicio desc, d.programaTrabalho, d.naturezaDespesa")
    List<DotacaoOrcamentaria> findTodas();

    boolean existsByExercicioAndProgramaTrabalhoAndNaturezaDespesaAndFonte(int exercicio, String programaTrabalho, String naturezaDespesa,
                                                                          com.edufelizardo.maissaudepublica.models.enuns.FonteRecursoSaude fonte);
}
