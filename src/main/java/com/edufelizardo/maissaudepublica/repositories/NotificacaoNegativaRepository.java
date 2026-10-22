package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.NotificacaoNegativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificacaoNegativaRepository extends JpaRepository<NotificacaoNegativa, UUID> {
    boolean existsByUnidade_UuidAndAnoAndSemana(UUID unidadeId, int ano, int semana);

    @Query("select n from NotificacaoNegativa n join fetch n.unidade left join fetch n.responsavel order by n.ano desc, n.semana desc")
    List<NotificacaoNegativa> findTodas();
}
