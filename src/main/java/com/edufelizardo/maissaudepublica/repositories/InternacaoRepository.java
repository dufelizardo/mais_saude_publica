package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InternacaoRepository extends JpaRepository<Internacao, UUID> {
    boolean existsByPaciente_UuidAndStatus(UUID pacienteId, StatusInternacao status);

    List<Internacao> findByPaciente_UuidOrderByAdmitidaEmDesc(UUID pacienteId);

    List<Internacao> findAllByOrderByAdmitidaEmDesc();

    /** Indicadores de qualidade (ADR-0205): as internações que tocam o intervalo, com a unidade e o paciente. */
    @Query("select i from Internacao i join fetch i.unidade join fetch i.paciente where i.admitidaEm < ?2 and (i.altaEm is null or i.altaEm >= ?1)")
    List<Internacao> findQueTocam(Instant inicio, Instant fim);

    /** As internações da unidade que tocam o período: admitidas antes do fim e sem alta até o começo (censo, ADR-0185). */
    @Query("select i from Internacao i where i.unidade.uuid = :unidade and i.admitidaEm < :fim and (i.altaEm is null or i.altaEm >= :inicio)")
    List<Internacao> findDaUnidadeNoPeriodo(@Param("unidade") UUID unidadeId, @Param("inicio") java.time.Instant inicio,
                                             @Param("fim") java.time.Instant fim);

    List<Internacao> findByStatusAndLeito_UuidIn(StatusInternacao status, Collection<UUID> leitoIds);

    List<Internacao> findByStatusAndAltaEmAfter(StatusInternacao status, Instant depoisDe);

    /** Internadas do caráter informado admitidas antes do instante (laudo de AIH pendente, ADR-0186). */
    List<Internacao> findByStatusAndCaraterAndAdmitidaEmBefore(StatusInternacao status,
            com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao carater, Instant antesDe);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Internacao i where i.uuid = :uuid")
    Optional<Internacao> findByIdParaAtualizar(@Param("uuid") UUID uuid);

    /** As internações da modalidade admitidas no intervalo (comunicação ao Ministério Público, ADR-0155). */
    @Query("select i from Internacao i join fetch i.unidade where i.modalidade = ?1 and i.admitidaEm >= ?2 and i.admitidaEm < ?3")
    List<Internacao> findDaModalidadeAdmitidasNoIntervalo(com.edufelizardo.maissaudepublica.models.enuns.ModalidadeInternacao modalidade,
                                                          Instant de, Instant ate);
}
