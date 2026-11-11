package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.HorarioPrescricao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoHorarioPrescricao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HorarioPrescricaoRepository extends JpaRepository<HorarioPrescricao, UUID> {

    List<HorarioPrescricao> findByItem_UuidInOrderByPrevistoParaAsc(Collection<UUID> itemIds);

    List<HorarioPrescricao> findByItem_UuidAndSituacao(UUID itemId, SituacaoHorarioPrescricao situacao);

    List<HorarioPrescricao> findByItem_Prescricao_UuidAndSituacao(UUID prescricaoId, SituacaoHorarioPrescricao situacao);

    /** Horários das internações em curso entre dois instantes (o posto de enfermagem). */
    @Query("select h from HorarioPrescricao h join fetch h.item i join fetch i.prescricao p join fetch p.internacao n "
            + "where n.status = :status and h.previstoPara >= :de and h.previstoPara < :ate order by h.previstoPara asc")
    List<HorarioPrescricao> findDoPosto(@Param("status") StatusInternacao status, @Param("de") Instant de, @Param("ate") Instant ate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from HorarioPrescricao h where h.uuid = :uuid")
    Optional<HorarioPrescricao> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
