package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.LaudoAih;
import com.edufelizardo.maissaudepublica.models.enuns.StatusLaudoAih;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LaudoAihRepository extends JpaRepository<LaudoAih, UUID> {

    List<LaudoAih> findByInternacao_UuidOrderBySolicitadoEmDesc(UUID internacaoId);

    List<LaudoAih> findByAgendamento_UuidOrderBySolicitadoEmDesc(UUID agendamentoId);

    List<LaudoAih> findByStatusOrderBySolicitadoEmAsc(StatusLaudoAih status);

    boolean existsByInternacao_UuidAndStatusIn(UUID internacaoId, Collection<StatusLaudoAih> status);

    boolean existsByAgendamento_UuidAndStatusIn(UUID agendamentoId, Collection<StatusLaudoAih> status);

    /** As internações (entre as informadas) que já têm laudo que não foi cancelado nem negado. */
    @Query("select distinct l.internacao.uuid from LaudoAih l where l.internacao.uuid in :ids and l.status not in :encerrados")
    List<UUID> findInternacoesComLaudo(@Param("ids") Collection<UUID> ids, @Param("encerrados") Collection<StatusLaudoAih> encerrados);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LaudoAih l where l.uuid = :uuid")
    Optional<LaudoAih> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
