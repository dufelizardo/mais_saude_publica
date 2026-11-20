package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AlteracaoDotacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AlteracaoDotacaoRepository extends JpaRepository<AlteracaoDotacao, UUID> {

    @Query("select a from AlteracaoDotacao a where a.dotacao.uuid in :dotacoes order by a.data, a.registradoEm")
    List<AlteracaoDotacao> findDasDotacoes(@Param("dotacoes") Collection<UUID> dotacoes);
}
