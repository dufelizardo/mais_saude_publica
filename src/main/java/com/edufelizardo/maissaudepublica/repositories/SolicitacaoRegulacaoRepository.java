package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoRegulacaoRepository extends JpaRepository<SolicitacaoRegulacao, UUID> {
    List<SolicitacaoRegulacao> findAllByOrderBySolicitadoEmDesc();

    List<SolicitacaoRegulacao> findByStatus(StatusSolicitacaoRegulacao status);

    /** Quantas autorizações usam a cota (ADR-0179). */
    long countByCota_UuidAndStatusIn(UUID cotaId, java.util.Collection<StatusSolicitacaoRegulacao> status);

    /** As autorizações que consumiram oferta (com cota ou extracota) na executante, no procedimento e no período. */
    @org.springframework.data.jpa.repository.Query("select s from SolicitacaoRegulacao s left join fetch s.cota where s.unidadeExecutante.uuid = ?1 "
            + "and s.procedimento.uuid = ?2 and s.dataHoraPrevista >= ?3 and s.dataHoraPrevista < ?4 and s.status in ?5")
    List<SolicitacaoRegulacao> findAutorizadasNoPeriodo(UUID executanteId, UUID procedimentoId, java.time.LocalDateTime de, java.time.LocalDateTime ate,
                                                        java.util.Collection<StatusSolicitacaoRegulacao> status);

    List<SolicitacaoRegulacao> findByPaciente_Uuid(UUID pacienteId);

    boolean existsByPaciente_UuidAndProcedimento_UuidAndStatusIn(UUID pacienteId, UUID procedimentoId,
                                                                 Collection<StatusSolicitacaoRegulacao> status);

    /** Trava a solicitação: dois reguladores não decidem a mesma ao mesmo tempo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SolicitacaoRegulacao s where s.uuid = :uuid")
    Optional<SolicitacaoRegulacao> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
