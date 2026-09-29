package com.edufelizardo.maissaudepublica.repositories;

import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import com.edufelizardo.maissaudepublica.models.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {
    /**
     * Todas as consultas de um atendimento — usado pela agregação do Prontuário (ver ADR-0045).
     */
    List<Consulta> findByAtendimentoUuid(UUID atendimentoUuid);

    /** Existe uma versão que corrige este registro? (ADR-0062) */
    boolean existsByRetificacaoDe_Uuid(UUID uuid);

    /** Trava o registro para retificar: impede duas retificações simultâneas da mesma versão. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Consulta r where r.uuid = :uuid")
    Optional<Consulta> findByIdParaRetificar(@Param("uuid") UUID uuid);

    /** Pares (registro corrigido, versão que o corrige) — para marcar o que já foi retificado. */
    @Query("select r.retificacaoDe.uuid, r.uuid from Consulta r where r.retificacaoDe is not null")
    List<Object[]> findParesDeRetificacao();

    /** Quantos registros vigentes (não retificados) cada atendimento tem. */
    @Query("select c.atendimento.uuid, count(c) from Consulta c "
            + "where not exists (select r from Consulta r where r.retificacaoDe = c) group by c.atendimento.uuid")
    List<Object[]> contarVigentesPorAtendimento();
}
