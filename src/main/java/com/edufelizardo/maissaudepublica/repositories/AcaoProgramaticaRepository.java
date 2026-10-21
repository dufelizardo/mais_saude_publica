package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAcaoProgramatica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AcaoProgramaticaRepository extends JpaRepository<AcaoProgramatica, UUID> {
    @Query("select a from AcaoProgramatica a join fetch a.programa join fetch a.unidade left join fetch a.paciente "
            + "order by a.dataPrevista asc, a.criadaEm asc")
    List<AcaoProgramatica> findTodas();

    List<AcaoProgramatica> findByPaciente_UuidOrderByDataPrevistaDesc(UUID pacienteId);

    boolean existsByAcompanhamento_UuidAndTipoAndSituacao(UUID acompanhamentoId, TipoAcaoProgramatica tipo, SituacaoAcaoProgramatica situacao);
}
