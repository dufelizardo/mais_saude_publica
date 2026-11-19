package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.BaixaBem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BaixaBemRepository extends JpaRepository<BaixaBem, UUID> {

    @Query("select b from BaixaBem b join fetch b.bem x join fetch x.unidade order by b.registradoEm desc")
    List<BaixaBem> findTodas();

    @Query("select b from BaixaBem b where b.bem.uuid = :bem order by b.registradoEm desc")
    List<BaixaBem> findDoBem(@Param("bem") UUID bem);
}
