package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MaterialEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MaterialEstoqueRepository extends JpaRepository<MaterialEstoque, UUID> {

    List<MaterialEstoque> findAllByOrderByNomeAsc();

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByCodigoIgnoreCaseAndUuidNot(String codigo, UUID uuid);

    boolean existsByNomeIgnoreCaseAndUuidNot(String nome, UUID uuid);
}
