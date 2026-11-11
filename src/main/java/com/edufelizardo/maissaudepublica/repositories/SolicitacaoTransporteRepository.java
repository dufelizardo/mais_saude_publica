package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.SolicitacaoTransporte;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoTransporteRepository extends JpaRepository<SolicitacaoTransporte, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SolicitacaoTransporte s where s.uuid = :uuid")
    Optional<SolicitacaoTransporte> findByIdParaAtualizar(@Param("uuid") UUID uuid);

    @Query("select s from SolicitacaoTransporte s join fetch s.paciente join fetch s.unidade where s.dataHora >= :de and s.dataHora < :ate "
            + "order by s.dataHora")
    List<SolicitacaoTransporte> findNoPeriodo(@Param("de") LocalDateTime de, @Param("ate") LocalDateTime ate);

    @Query("select s from SolicitacaoTransporte s join fetch s.paciente join fetch s.unidade where s.paciente.uuid = :paciente order by s.dataHora desc")
    List<SolicitacaoTransporte> findDoPaciente(@Param("paciente") UUID paciente);

    List<SolicitacaoTransporte> findByRecorrenciaIdOrderByDataHora(UUID recorrenciaId);

    List<SolicitacaoTransporte> findByPaciente_UuidAndDataHora(UUID paciente, LocalDateTime dataHora);
}
