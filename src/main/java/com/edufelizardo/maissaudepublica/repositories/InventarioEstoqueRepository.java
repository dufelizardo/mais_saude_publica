package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.InventarioEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoInventarioEstoque;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventarioEstoqueRepository extends JpaRepository<InventarioEstoque, UUID> {

    boolean existsByUnidade_UuidAndSituacao(UUID unidadeId, SituacaoInventarioEstoque situacao);

    List<InventarioEstoque> findByUnidade_UuidOrderByAbertoEmDesc(UUID unidadeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventarioEstoque i where i.uuid = :uuid")
    Optional<InventarioEstoque> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
