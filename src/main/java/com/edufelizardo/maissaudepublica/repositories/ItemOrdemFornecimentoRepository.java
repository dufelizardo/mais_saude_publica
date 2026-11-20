package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemOrdemFornecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ItemOrdemFornecimentoRepository extends JpaRepository<ItemOrdemFornecimento, UUID> {

    @Query("select i from ItemOrdemFornecimento i join fetch i.itemInstrumento ii join fetch ii.itemContratacao where i.ordem.uuid in :ids")
    List<ItemOrdemFornecimento> findDasOrdens(@Param("ids") Collection<UUID> ids);
}
