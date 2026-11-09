package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TitularidadeAreaGestao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TitularidadeAreaGestaoRepository extends JpaRepository<TitularidadeAreaGestao, UUID> {

    @Query("select t from TitularidadeAreaGestao t join fetch t.profissional where t.area.uuid = ?1 order by t.inicio desc")
    List<TitularidadeAreaGestao> findDaArea(UUID areaId);

    @Query("select t from TitularidadeAreaGestao t join fetch t.profissional join fetch t.area")
    List<TitularidadeAreaGestao> findTodas();
}
