package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TransferenciaEstoque;
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
public interface TransferenciaEstoqueRepository extends JpaRepository<TransferenciaEstoque, UUID> {

    @Query("select t from TransferenciaEstoque t join fetch t.origem join fetch t.destino order by t.enviadaEm desc")
    List<TransferenciaEstoque> findTodas();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TransferenciaEstoque t where t.uuid = :uuid")
    Optional<TransferenciaEstoque> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
