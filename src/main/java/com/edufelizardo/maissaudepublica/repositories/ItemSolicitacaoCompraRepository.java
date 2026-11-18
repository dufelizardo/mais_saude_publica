package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemSolicitacaoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ItemSolicitacaoCompraRepository extends JpaRepository<ItemSolicitacaoCompra, UUID> {

    @Query("select i from ItemSolicitacaoCompra i left join fetch i.material left join fetch i.medicamento where i.solicitacao.uuid in :ids order by i.ordem")
    List<ItemSolicitacaoCompra> findDasSolicitacoes(@Param("ids") Collection<UUID> ids);
}
