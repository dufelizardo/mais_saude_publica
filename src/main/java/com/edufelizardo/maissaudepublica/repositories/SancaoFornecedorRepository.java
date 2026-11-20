package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.SancaoFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SancaoFornecedorRepository extends JpaRepository<SancaoFornecedor, UUID> {

    List<SancaoFornecedor> findByFornecedor_UuidOrderByInicioDesc(UUID fornecedorId);

    @Query("select s from SancaoFornecedor s join fetch s.fornecedor")
    List<SancaoFornecedor> findTodas();
}
