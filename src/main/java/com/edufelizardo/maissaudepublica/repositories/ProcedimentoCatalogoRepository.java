package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ProcedimentoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProcedimentoCatalogoRepository extends JpaRepository<ProcedimentoCatalogo, UUID> {
    @Query("select p from ProcedimentoCatalogo p left join fetch p.especialidade left join fetch p.condicaoResultante order by p.nome asc")
    List<ProcedimentoCatalogo> findTodos();

    boolean existsByCodigo(String codigo);
}
