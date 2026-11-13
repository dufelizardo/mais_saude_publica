package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcaoQualidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AcaoQualidadeRepository extends JpaRepository<AcaoQualidade, UUID> {

    @Query("select a from AcaoQualidade a join fetch a.responsavel where a.naoConformidade.uuid = ?1 order by a.ciclo, a.prazo, a.registradoEm")
    List<AcaoQualidade> findDaNaoConformidade(UUID naoConformidadeId);

    @Query("select a from AcaoQualidade a join fetch a.responsavel r join fetch a.naoConformidade n join fetch n.unidade")
    List<AcaoQualidade> findTodas();
}
