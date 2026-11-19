package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Liquidacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LiquidacaoRepository extends JpaRepository<Liquidacao, UUID> {

    @Query("select l from Liquidacao l join fetch l.empenho e join fetch e.dotacao join fetch e.fornecedor left join fetch l.recebimento left join fetch l.ordemServico left join fetch l.diaria order by l.vencimento, l.registradoEm")
    List<Liquidacao> findTodas();

    @Query("select l from Liquidacao l where l.empenho.uuid in :empenhos")
    List<Liquidacao> findDosEmpenhos(@Param("empenhos") Collection<UUID> empenhos);
}
