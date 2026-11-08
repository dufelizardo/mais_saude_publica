package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.OcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaUrgencia;
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

public interface OcorrenciaUrgenciaRepository extends JpaRepository<OcorrenciaUrgencia, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OcorrenciaUrgencia o where o.uuid = :uuid")
    Optional<OcorrenciaUrgencia> findByIdParaAtualizar(@Param("uuid") UUID uuid);

    @Query("select o from OcorrenciaUrgencia o join fetch o.central left join fetch o.paciente left join fetch o.destino "
            + "left join fetch o.unidadeSolicitante where o.situacao in ?1 order by o.recebidaEm")
    List<OcorrenciaUrgencia> findNasSituacoes(Collection<SituacaoOcorrenciaUrgencia> situacoes);

    @Query("select o from OcorrenciaUrgencia o join fetch o.central left join fetch o.paciente left join fetch o.destino "
            + "left join fetch o.unidadeSolicitante where o.recebidaEm >= ?1 order by o.recebidaEm desc")
    List<OcorrenciaUrgencia> findRecebidasDesde(Instant desde);

    @Query("select o from OcorrenciaUrgencia o join fetch o.central left join fetch o.destino left join fetch o.regulador "
            + "where o.paciente.uuid = ?1 order by o.recebidaEm desc")
    List<OcorrenciaUrgencia> findDoPaciente(UUID pacienteId);

    boolean existsByCrise_UuidAndSituacaoIn(UUID criseId, Collection<SituacaoOcorrenciaUrgencia> situacoes);

    @Query(value = "select nextval('sq_protocolo_urgencia')", nativeQuery = true)
    long proximoNumero();
}
