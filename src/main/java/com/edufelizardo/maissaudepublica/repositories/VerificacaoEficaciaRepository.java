package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.VerificacaoEficacia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface VerificacaoEficaciaRepository extends JpaRepository<VerificacaoEficacia, UUID> {

    List<VerificacaoEficacia> findByNaoConformidade_UuidOrderByCicloAsc(UUID naoConformidadeId);

    @Query("select v from VerificacaoEficacia v join fetch v.naoConformidade")
    List<VerificacaoEficacia> findTodas();
}
