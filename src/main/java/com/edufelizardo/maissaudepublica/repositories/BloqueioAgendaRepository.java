package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.BloqueioAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, UUID> {
    /** Bloqueios que tocam o período e valem para o profissional (ou para todos) na unidade (ou em todas). */
    @Query("""
            select b from BloqueioAgenda b
            where b.inicio < :ate and b.fim > :de
              and (b.profissional is null or b.profissional.uuid = :profissionalId)
              and (b.unidade is null or b.unidade.uuid = :unidadeId)
            order by b.inicio
            """)
    List<BloqueioAgenda> findAplicaveis(@Param("profissionalId") UUID profissionalId, @Param("unidadeId") UUID unidadeId,
                                        @Param("de") LocalDateTime de, @Param("ate") LocalDateTime ate);
}
