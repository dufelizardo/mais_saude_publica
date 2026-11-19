package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CentroCustoRepository extends JpaRepository<CentroCusto, UUID> {

    @Query("select c from CentroCusto c join fetch c.unidade u left join fetch c.setor s order by u.nome, s.nome nulls first")
    List<CentroCusto> findTodos();

    @Query("select max(c.codigo) from CentroCusto c")
    String findMaiorCodigo();

    boolean existsBySetor_Uuid(UUID setorId);

    Optional<CentroCusto> findByUnidade_UuidAndSetorIsNull(UUID unidadeId);
}
