package com.edufelizardo.maissaudepublica.repositories;

import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import com.edufelizardo.maissaudepublica.models.EvolucaoEnfermagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EvolucaoEnfermagemRepository extends JpaRepository<EvolucaoEnfermagem, UUID> {
    /**
     * Todas as evoluções de um atendimento — usado pela agregação do Prontuário (ver ADR-0045/ADR-0048).
     */
    List<EvolucaoEnfermagem> findByAtendimentoUuid(UUID atendimentoUuid);

    /** Existe uma versão que corrige este registro? (ADR-0062) */
    boolean existsByRetificacaoDe_Uuid(UUID uuid);

    /** Trava o registro para retificar: impede duas retificações simultâneas da mesma versão. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from EvolucaoEnfermagem r where r.uuid = :uuid")
    Optional<EvolucaoEnfermagem> findByIdParaRetificar(@Param("uuid") UUID uuid);

    /** Pares (registro corrigido, versão que o corrige) — para marcar o que já foi retificado. */
    @Query("select r.retificacaoDe.uuid, r.uuid from EvolucaoEnfermagem r where r.retificacaoDe is not null")
    List<Object[]> findParesDeRetificacao();

    /** Quantos registros vigentes (não retificados) cada atendimento tem. */
    @Query("select e.atendimento.uuid, count(e) from EvolucaoEnfermagem e "
            + "where not exists (select r from EvolucaoEnfermagem r where r.retificacaoDe = e) group by e.atendimento.uuid")
    List<Object[]> contarVigentesPorAtendimento();
}
