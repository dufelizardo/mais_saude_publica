package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MovimentoEmpenho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MovimentoEmpenhoRepository extends JpaRepository<MovimentoEmpenho, UUID> {

    @Query("select m from MovimentoEmpenho m where m.empenho.uuid in :empenhos order by m.data, m.registradoEm")
    List<MovimentoEmpenho> findDosEmpenhos(@Param("empenhos") Collection<UUID> empenhos);
}
