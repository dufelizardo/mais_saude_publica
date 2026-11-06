package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcaoControleVetorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AcaoControleVetorialRepository extends JpaRepository<AcaoControleVetorial, UUID> {

    @Query("select a from AcaoControleVetorial a order by a.dataPrevista desc, a.registradoEm desc")
    List<AcaoControleVetorial> findTodas();

    @Query("select a.notificacao.uuid from AcaoControleVetorial a where a.notificacao is not null "
            + "and a.situacao <> com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoControleVetorial.CANCELADA")
    List<UUID> findNotificacoesComAcao();
}
