package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.OrdemFornecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrdemFornecimentoRepository extends JpaRepository<OrdemFornecimento, UUID> {

    @Query("select o from OrdemFornecimento o join fetch o.instrumento i join fetch i.fornecedor join fetch o.unidade order by o.emitidaEm desc, o.numero desc")
    List<OrdemFornecimento> findTodas();

    long countByNumeroStartingWith(String prefixo);

    @Query("select o from OrdemFornecimento o where o.empenhoRef.uuid in :empenhos")
    List<OrdemFornecimento> findDosEmpenhos(@Param("empenhos") Collection<UUID> empenhos);
}
