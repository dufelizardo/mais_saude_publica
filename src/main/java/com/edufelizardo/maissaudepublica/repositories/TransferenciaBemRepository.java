package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TransferenciaBem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferenciaBemRepository extends JpaRepository<TransferenciaBem, UUID> {

    @Query("select t from TransferenciaBem t join fetch t.bem join fetch t.origem join fetch t.destino where t.bem.uuid in :bens order by t.registradoEm desc")
    List<TransferenciaBem> findDosBens(@Param("bens") Collection<UUID> bens);

    @Query("select t from TransferenciaBem t join fetch t.bem join fetch t.origem join fetch t.destino order by t.registradoEm desc")
    List<TransferenciaBem> findTodas();
}
