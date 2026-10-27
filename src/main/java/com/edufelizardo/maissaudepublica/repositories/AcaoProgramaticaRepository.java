package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAcaoProgramatica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AcaoProgramaticaRepository extends JpaRepository<AcaoProgramatica, UUID> {
    @Query("select a from AcaoProgramatica a left join fetch a.programa left join fetch a.campanha join fetch a.unidade left join fetch a.paciente "
            + "order by a.dataPrevista asc, a.criadaEm asc")
    List<AcaoProgramatica> findTodas();

    List<AcaoProgramatica> findByPaciente_UuidOrderByDataPrevistaDesc(UUID pacienteId);

    List<AcaoProgramatica> findByCampanha_Uuid(UUID campanhaId);

    /** As ações do programa previstas no intervalo (indicadores, ADR-0141). */
    @Query("select a from AcaoProgramatica a join fetch a.unidade where a.programa.uuid = ?1 and a.dataPrevista >= ?2 and a.dataPrevista <= ?3")
    List<AcaoProgramatica> findDoProgramaNoIntervalo(UUID programaId, java.time.LocalDate de, java.time.LocalDate ate);

    boolean existsByLocalCampanha_Uuid(UUID localId);

    boolean existsByAcompanhamento_UuidAndTipoAndSituacao(UUID acompanhamentoId, TipoAcaoProgramatica tipo, SituacaoAcaoProgramatica situacao);
}
