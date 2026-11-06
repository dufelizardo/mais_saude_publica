package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TipoServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TipoServicoRepository extends JpaRepository<TipoServico, UUID> {

    Optional<TipoServico> findByCodigo(String codigo);

    @Query("select t from TipoServico t left join fetch t.capacidadeExigida order by lower(t.nome)")
    List<TipoServico> findTodos();
}
