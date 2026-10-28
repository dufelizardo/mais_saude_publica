package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CamaraRefrigeracao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CamaraRefrigeracaoRepository extends JpaRepository<CamaraRefrigeracao, UUID> {

    List<CamaraRefrigeracao> findByUnidade_UuidOrderByNomeAsc(UUID unidadeId);

    boolean existsByUnidade_UuidAndNomeIgnoreCase(UUID unidadeId, String nome);
}
