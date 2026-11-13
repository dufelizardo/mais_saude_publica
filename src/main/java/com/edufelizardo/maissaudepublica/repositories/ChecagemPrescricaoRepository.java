package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ChecagemPrescricao;
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
public interface ChecagemPrescricaoRepository extends JpaRepository<ChecagemPrescricao, UUID> {

    List<ChecagemPrescricao> findByItem_UuidInOrderByRealizadoEmAscRegistradoEmAsc(Collection<UUID> itemIds);

    List<ChecagemPrescricao> findByHorario_UuidInAndRetificadaFalse(Collection<UUID> horarioIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ChecagemPrescricao c where c.uuid = :uuid")
    Optional<ChecagemPrescricao> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
