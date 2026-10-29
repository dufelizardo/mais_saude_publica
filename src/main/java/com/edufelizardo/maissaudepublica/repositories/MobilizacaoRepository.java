package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Mobilizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MobilizacaoRepository extends JpaRepository<Mobilizacao, UUID> {

    @Query("select m from Mobilizacao m join fetch m.destino left join fetch m.origem left join fetch m.profissional left join fetch m.transferencia "
            + "where m.emergencia.uuid = ?1 order by m.registradaEm")
    List<Mobilizacao> findDaEmergencia(UUID emergenciaId);

    /** A exceção da Escala (ADR-0158): o profissional mobilizado, em andamento, para a unidade, na data, com a emergência ativa. */
    @Query("select m from Mobilizacao m join fetch m.emergencia e where m.profissional.uuid = ?1 and m.destino.uuid = ?2 "
            + "and m.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoMobilizacao.EM_ANDAMENTO "
            + "and e.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia.ATIVA "
            + "and m.inicio <= ?3 and (m.fim is null or m.fim >= ?3)")
    List<Mobilizacao> findQueAutorizam(UUID profissionalId, UUID unidadeId, java.time.LocalDate dia);

    /** Os profissionais mobilizados para a unidade com a exceção valendo em algum dia do intervalo (a semana da Escala). */
    @Query("select m from Mobilizacao m join fetch m.profissional join fetch m.emergencia e where m.destino.uuid = ?1 "
            + "and m.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoMobilizacao.EM_ANDAMENTO "
            + "and e.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia.ATIVA "
            + "and m.inicio <= ?3 and (m.fim is null or m.fim >= ?2)")
    List<Mobilizacao> findQueAutorizamNaUnidade(UUID unidadeId, java.time.LocalDate de, java.time.LocalDate ate);
}
