package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Contratacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ContratacaoRepository extends JpaRepository<Contratacao, UUID> {

    @Query("select c from Contratacao c order by c.registradoEm desc")
    List<Contratacao> findTodas();

    boolean existsByNumeroProcesso(String numeroProcesso);
}
