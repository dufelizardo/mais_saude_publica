package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemRequisicaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRequisicaoEstoqueRepository extends JpaRepository<ItemRequisicaoEstoque, UUID> {

    List<ItemRequisicaoEstoque> findByRequisicao_Uuid(UUID requisicaoId);

    List<ItemRequisicaoEstoque> findByRequisicao_UuidIn(Collection<UUID> requisicaoIds);
}
