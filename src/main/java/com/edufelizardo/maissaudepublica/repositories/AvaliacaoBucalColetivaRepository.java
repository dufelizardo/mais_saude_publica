package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AvaliacaoBucalColetiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoBucalColetivaRepository extends JpaRepository<AvaliacaoBucalColetiva, UUID> {
    @Query("select a from AvaliacaoBucalColetiva a join fetch a.paciente where a.acao.uuid = ?1 order by a.paciente.nome")
    List<AvaliacaoBucalColetiva> findDaAcao(UUID acaoId);

    Optional<AvaliacaoBucalColetiva> findByAcao_UuidAndPaciente_Uuid(UUID acaoId, UUID pacienteId);

    /** As avaliações das ações da unidade, a mais recente primeiro. */
    @Query("select a from AvaliacaoBucalColetiva a join fetch a.paciente join fetch a.acao ac where ac.unidade.uuid = ?1 "
            + "order by a.registradoEm desc")
    List<AvaliacaoBucalColetiva> findDaUnidade(UUID unidadeId);
}
