package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemContratacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ItemContratacaoRepository extends JpaRepository<ItemContratacao, UUID> {

    @Query("select i from ItemContratacao i left join fetch i.fornecedor where i.contratacao.uuid = :id order by i.ordem")
    List<ItemContratacao> findDaContratacao(@Param("id") UUID contratacaoId);
}
