package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.InvestigacaoEpidemiologica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface InvestigacaoEpidemiologicaRepository extends JpaRepository<InvestigacaoEpidemiologica, UUID> {
    Optional<InvestigacaoEpidemiologica> findByNotificacao_Uuid(UUID notificacaoId);
}
