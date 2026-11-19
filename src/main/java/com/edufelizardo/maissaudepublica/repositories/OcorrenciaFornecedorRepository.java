package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.OcorrenciaFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OcorrenciaFornecedorRepository extends JpaRepository<OcorrenciaFornecedor, UUID> {

    @Query("select o from OcorrenciaFornecedor o join fetch o.fornecedor left join fetch o.ordem order by o.registradaEm desc, o.registradoEm desc")
    List<OcorrenciaFornecedor> findTodas();
}
