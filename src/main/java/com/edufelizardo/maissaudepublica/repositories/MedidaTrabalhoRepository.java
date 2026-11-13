package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MedidaTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import com.edufelizardo.maissaudepublica.models.MedidaTrabalho;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedidaTrabalhoRepository extends JpaRepository<MedidaTrabalho, UUID> {
    List<MedidaTrabalho> findByInvestigacao_UuidOrderByCriadaEmAsc(UUID investigacaoId);

    @Query("select m from MedidaTrabalho m join fetch m.investigacao i join fetch i.notificacao n join fetch n.unidade join fetch n.paciente join fetch n.agravo")
    List<MedidaTrabalho> findTodas();
}
