package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.InstrumentoContratual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface InstrumentoContratualRepository extends JpaRepository<InstrumentoContratual, UUID> {

    @Query("select i from InstrumentoContratual i join fetch i.fornecedor join fetch i.gestor join fetch i.fiscal order by i.fim")
    List<InstrumentoContratual> findTodos();

    boolean existsByNumero(String numero);
}
