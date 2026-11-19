package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Empenho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EmpenhoRepository extends JpaRepository<Empenho, UUID> {

    @Query("select e from Empenho e join fetch e.dotacao join fetch e.fornecedor left join fetch e.instrumento order by e.data desc, e.numero desc")
    List<Empenho> findTodos();

    boolean existsByExercicioAndNumero(int exercicio, String numero);
}
