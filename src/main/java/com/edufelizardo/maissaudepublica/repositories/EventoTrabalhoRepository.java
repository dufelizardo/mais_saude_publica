package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface EventoTrabalhoRepository extends JpaRepository<EventoTrabalho, UUID> {
    Optional<EventoTrabalho> findByNotificacao_Uuid(UUID notificacaoId);

    Optional<EventoTrabalho> findByAcidenteTrabalho_Uuid(UUID acidenteId);

    boolean existsByAcidenteTrabalho_Uuid(UUID acidenteId);

    @Query("select e from EventoTrabalho e join fetch e.notificacao n join fetch n.agravo join fetch n.unidade join fetch n.paciente order by n.dataNotificacao desc")
    List<EventoTrabalho> findTodos();

    @Query("select e.acidenteTrabalho.uuid from EventoTrabalho e where e.acidenteTrabalho is not null")
    Set<UUID> findAcidentesNotificados();
}
