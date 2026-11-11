package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, UUID> {

    @Query("select m from MovimentacaoEstoque m join fetch m.lote l join fetch l.material join fetch l.unidade left join fetch m.setor "
            + "left join fetch m.profissional where m.registradoEm >= ?1 order by m.registradoEm desc")
    List<MovimentacaoEstoque> findDesde(Instant desde);

    /** O consumo extraordinário da emergência (ADR-0191). */
    @Query("select m from MovimentacaoEstoque m join fetch m.lote l join fetch l.material join fetch l.unidade where m.emergenciaId = ?1 "
            + "order by m.registradoEm")
    List<MovimentacaoEstoque> findDaEmergencia(java.util.UUID emergenciaId);
}
