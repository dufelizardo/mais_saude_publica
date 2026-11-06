package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.InstituicaoExterna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InstituicaoExternaRepository extends JpaRepository<InstituicaoExterna, UUID> {

    List<InstituicaoExterna> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndUuidNot(String nome, UUID uuid);
}
