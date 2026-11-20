package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.SolicitacaoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SolicitacaoCompraRepository extends JpaRepository<SolicitacaoCompra, UUID> {

    @Query("select s from SolicitacaoCompra s join fetch s.unidade left join fetch s.setor order by s.registradoEm desc")
    List<SolicitacaoCompra> findTodas();

    long countByNumeroStartingWith(String prefixo);
}
