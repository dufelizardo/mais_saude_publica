package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemTransferenciaEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ItemTransferenciaEstoqueRepository extends JpaRepository<ItemTransferenciaEstoque, UUID> {

    List<ItemTransferenciaEstoque> findByTransferencia_Uuid(UUID transferenciaId);

    List<ItemTransferenciaEstoque> findByTransferencia_UuidIn(Collection<UUID> transferenciaIds);
}
