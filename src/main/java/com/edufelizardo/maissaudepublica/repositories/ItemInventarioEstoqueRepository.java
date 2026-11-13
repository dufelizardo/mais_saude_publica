package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemInventarioEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemInventarioEstoqueRepository extends JpaRepository<ItemInventarioEstoque, UUID> {

    List<ItemInventarioEstoque> findByInventario_Uuid(UUID inventarioId);
}
