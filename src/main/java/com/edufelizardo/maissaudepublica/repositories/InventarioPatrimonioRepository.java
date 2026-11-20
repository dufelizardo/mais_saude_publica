package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.InventarioPatrimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface InventarioPatrimonioRepository extends JpaRepository<InventarioPatrimonio, UUID> {

    @Query("select i from InventarioPatrimonio i join fetch i.unidade order by i.abertoEm desc, i.numero desc")
    List<InventarioPatrimonio> findTodos();

    boolean existsByUnidade_UuidAndSituacao(UUID unidadeId, com.edufelizardo.maissaudepublica.models.enuns.SituacaoInventarioPatrimonio situacao);

    @Query("select max(i.numero) from InventarioPatrimonio i where i.numero like concat(:prefixo, '%')")
    String findMaiorNumero(@Param("prefixo") String prefixo);
}
