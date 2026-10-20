package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EncaminhamentoSaudeMental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EncaminhamentoSaudeMentalRepository extends JpaRepository<EncaminhamentoSaudeMental, UUID> {
    @Query("select e from EncaminhamentoSaudeMental e join fetch e.solicitacao s join fetch s.procedimento left join fetch s.unidadeExecutante "
            + "where e.acompanhamento.uuid = ?1 order by s.solicitadoEm desc")
    List<EncaminhamentoSaudeMental> findDoAcompanhamento(UUID acompanhamentoId);

    boolean existsByAcompanhamento_UuidAndSolicitacao_Uuid(UUID acompanhamentoId, UUID solicitacaoId);
}
