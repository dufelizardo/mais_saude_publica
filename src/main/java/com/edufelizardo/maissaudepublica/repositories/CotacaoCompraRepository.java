package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CotacaoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CotacaoCompraRepository extends JpaRepository<CotacaoCompra, UUID> {

    @Query("select c from CotacaoCompra c left join fetch c.fornecedor where c.item.uuid in :ids order by c.registradoEm")
    List<CotacaoCompra> findDosItens(@Param("ids") Collection<UUID> ids);
}
