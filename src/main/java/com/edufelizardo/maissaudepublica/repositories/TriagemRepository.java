package com.edufelizardo.maissaudepublica.repositories;

import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import com.edufelizardo.maissaudepublica.models.Triagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TriagemRepository extends JpaRepository<Triagem, UUID> {
    /**
     * Todas as triagens de um atendimento — usado pela agregação do Prontuário (ver ADR-0045/ADR-0047).
     */
    List<Triagem> findByAtendimentoUuid(UUID atendimentoUuid);

    /** Existe uma versão que corrige este registro? (ADR-0062) */
    boolean existsByRetificacaoDe_Uuid(UUID uuid);

    /** Trava o registro para retificar: impede duas retificações simultâneas da mesma versão. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Triagem r where r.uuid = :uuid")
    Optional<Triagem> findByIdParaRetificar(@Param("uuid") UUID uuid);

    /** Pares (registro corrigido, versão que o corrige) — para marcar o que já foi retificado. */
    @Query("select r.retificacaoDe.uuid, r.uuid from Triagem r where r.retificacaoDe is not null")
    List<Object[]> findParesDeRetificacao();

    /** Quantos registros vigentes (não retificados) cada atendimento tem. */
    @Query("select t.atendimento.uuid, count(t) from Triagem t "
            + "where not exists (select r from Triagem r where r.retificacaoDe = t) group by t.atendimento.uuid")
    List<Object[]> contarVigentesPorAtendimento();

    /** Classificação de risco das triagens vigentes, da mais recente para a mais antiga. */
    @Query("select t.atendimento.uuid, t.classificacaoRisco from Triagem t "
            + "where not exists (select r from Triagem r where r.retificacaoDe = t) order by t.dataHora desc")
    List<Object[]> riscosVigentesDoMaisRecente();
}
