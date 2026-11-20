package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CertidaoFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CertidaoFornecedorRepository extends JpaRepository<CertidaoFornecedor, UUID> {

    List<CertidaoFornecedor> findByFornecedor_UuidOrderByValidaAteDesc(UUID fornecedorId);

    @Query("select c from CertidaoFornecedor c join fetch c.fornecedor")
    List<CertidaoFornecedor> findTodas();
}
