package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.NaoConformidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NaoConformidadeRepository extends JpaRepository<NaoConformidade, UUID> {

    @Query("select n from NaoConformidade n join fetch n.unidade left join fetch n.setor join fetch n.responsavel order by n.registradoEm desc")
    List<NaoConformidade> findTodas();

    long countByNumeroStartingWith(String prefixo);
}
