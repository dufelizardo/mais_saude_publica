package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RequisicaoEstoque;
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
public interface RequisicaoEstoqueRepository extends JpaRepository<RequisicaoEstoque, UUID> {

    @Query("select r from RequisicaoEstoque r join fetch r.unidade join fetch r.central order by r.criadaEm desc")
    List<RequisicaoEstoque> findTodas();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RequisicaoEstoque r where r.uuid = :uuid")
    Optional<RequisicaoEstoque> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
