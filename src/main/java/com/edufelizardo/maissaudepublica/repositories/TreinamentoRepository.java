package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Treinamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TreinamentoRepository extends JpaRepository<Treinamento, UUID> {

    /** Os treinamentos que usam o conteúdo (ADR-0173). */
    @org.springframework.data.jpa.repository.Query("select t from Treinamento t where ?1 member of t.conteudos order by t.nome")
    java.util.List<Treinamento> findQueUsamOConteudo(UUID grupoId);

}
