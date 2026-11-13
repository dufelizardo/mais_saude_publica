package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemPrescricao;
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
public interface ItemPrescricaoRepository extends JpaRepository<ItemPrescricao, UUID> {

    List<ItemPrescricao> findByPrescricao_UuidOrderByOrdemAsc(UUID prescricaoId);

    List<ItemPrescricao> findByPrescricao_UuidInOrderByOrdemAsc(Collection<UUID> prescricaoIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from ItemPrescricao i where i.uuid = :uuid")
    Optional<ItemPrescricao> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
