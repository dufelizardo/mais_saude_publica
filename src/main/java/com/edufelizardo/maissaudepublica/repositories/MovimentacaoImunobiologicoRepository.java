package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MovimentacaoImunobiologico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MovimentacaoImunobiologicoRepository extends JpaRepository<MovimentacaoImunobiologico, UUID> {

    List<MovimentacaoImunobiologico> findByLote_UuidOrderByRegistradoEmAsc(UUID loteId);

    /** Os lançamentos dos lotes da unidade desde o instante (as perdas e aplicações do mês). */
    @Query("select m from MovimentacaoImunobiologico m join fetch m.lote l where l.unidade.uuid = ?1 and m.registradoEm >= ?2")
    List<MovimentacaoImunobiologico> findDaUnidadeDesde(UUID unidadeId, Instant desde);
}
