package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CanalComunicacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CanalComunicacaoRepository extends JpaRepository<CanalComunicacao, UUID> {

    List<CanalComunicacao> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
