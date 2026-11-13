package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MetaQualidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MetaQualidadeRepository extends JpaRepository<MetaQualidade, UUID> {

    @Query("select m from MetaQualidade m join fetch m.unidade left join fetch m.checklist order by m.vigenteDesde desc, m.registradoEm desc")
    List<MetaQualidade> findTodas();
}
