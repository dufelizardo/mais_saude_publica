package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TransferenciaFarmacia;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferenciaFarmaciaRepository extends JpaRepository<TransferenciaFarmacia, UUID> {
    List<TransferenciaFarmacia> findAllByOrderByRegistradoEmDesc();

    /** Trava a transferência: impede receber e cancelar ao mesmo tempo, ou receber duas vezes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TransferenciaFarmacia t where t.uuid = :uuid")
    Optional<TransferenciaFarmacia> findByIdParaAtualizar(@Param("uuid") UUID uuid);

    /** Transferências anteriores às duas etapas (ADR-0061), ainda sem status. */
    List<TransferenciaFarmacia> findByStatusIsNull();
}
