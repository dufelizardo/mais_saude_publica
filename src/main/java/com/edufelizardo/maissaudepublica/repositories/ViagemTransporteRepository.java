package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ViagemTransporte;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViagemTransporteRepository extends JpaRepository<ViagemTransporte, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ViagemTransporte v where v.uuid = :uuid")
    Optional<ViagemTransporte> findByIdParaAtualizar(@Param("uuid") UUID uuid);

    /** As que cruzam o período (saída antes do fim e retorno depois do início). */
    @Query("select v from ViagemTransporte v join fetch v.veiculo vv join fetch vv.unidade where v.saidaPrevista < :ate and v.retornoPrevisto > :de "
            + "order by v.saidaPrevista")
    List<ViagemTransporte> findNoPeriodo(@Param("de") Instant de, @Param("ate") Instant ate);

    /** As concluídas com a chegada no período (a quilometragem conta o que rodou, não o previsto). */
    @Query("select v from ViagemTransporte v join fetch v.veiculo vv join fetch vv.unidade where v.situacao = "
            + "com.edufelizardo.maissaudepublica.models.enuns.SituacaoViagem.CONCLUIDA and v.chegadaEm >= :de and v.chegadaEm < :ate")
    List<ViagemTransporte> findConcluidasComChegadaEntre(@Param("de") Instant de, @Param("ate") Instant ate);

    @Query("select e.viagem from EmbarqueViagem e where e.solicitacao.uuid = :solicitacao")
    List<ViagemTransporte> findDaSolicitacao(@Param("solicitacao") UUID solicitacao);
}
