package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.InvestigacaoTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import com.edufelizardo.maissaudepublica.models.InvestigacaoTrabalho;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestigacaoTrabalhoRepository extends JpaRepository<InvestigacaoTrabalho, UUID> {
    Optional<InvestigacaoTrabalho> findByNotificacao_Uuid(UUID notificacaoId);

    @Query("select i from InvestigacaoTrabalho i join fetch i.notificacao n join fetch n.unidade join fetch n.agravo join fetch n.paciente")
    List<InvestigacaoTrabalho> findTodas();
}
