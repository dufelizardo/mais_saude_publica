package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ContatoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoContato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContatoEpidemiologicoRepository extends JpaRepository<ContatoEpidemiologico, UUID> {
    List<ContatoEpidemiologico> findByNotificacao_UuidOrderByAtualizadoEmDesc(UUID notificacaoId);

    List<ContatoEpidemiologico> findBySituacao(SituacaoContato situacao);
}
