package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ConteudoEducativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ConteudoEducativoRepository extends JpaRepository<ConteudoEducativo, UUID> {

    List<ConteudoEducativo> findAllByOrderByTituloAscVersaoDesc();

    List<ConteudoEducativo> findByGrupoId(UUID grupoId);

    /** Os conteúdos que já passaram pela revisão técnica (ADR-0173). */
    List<ConteudoEducativo> findBySituacaoIn(java.util.Collection<com.edufelizardo.maissaudepublica.models.enuns.SituacaoConteudo> situacoes);

    @Query("select distinct c from ConteudoEducativo c join c.canais k where c.situacao = 'PUBLICADO' and c.publico = 'POPULACAO' "
            + "and k.tipo = 'PORTAL' and c.validade >= ?1 order by c.titulo")
    List<ConteudoEducativo> findNoPortal(LocalDate hoje);
}
