package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {
    @Query("select n from Notificacao n join fetch n.paciente join fetch n.agravo join fetch n.unidade left join fetch n.profissional "
            + "order by n.dataNotificacao desc, n.registradaEm desc")
    List<Notificacao> findTodas();

    List<Notificacao> findByPaciente_Uuid(UUID pacienteId);

    boolean existsByUnidade_UuidAndDataNotificacaoBetween(UUID unidadeId, LocalDate inicio, LocalDate fim);
}
