package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TransferenciaCuidado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferenciaCuidadoRepository extends JpaRepository<TransferenciaCuidado, UUID> {
    @Query("select t from TransferenciaCuidado t join fetch t.acompanhamento a join fetch a.paciente join fetch a.unidade "
            + "join fetch t.unidadeDestino where t.situacao = com.edufelizardo.maissaudepublica.models.enuns.SituacaoTransferenciaCuidado.PROPOSTA "
            + "order by t.propostaEm")
    List<TransferenciaCuidado> findPendentes();

    @Query("select t from TransferenciaCuidado t join fetch t.unidadeDestino left join fetch t.casoDestino where t.acompanhamento.uuid = ?1 "
            + "order by t.propostaEm desc")
    List<TransferenciaCuidado> findDoAcompanhamento(UUID acompanhamentoId);

    Optional<TransferenciaCuidado> findByCasoDestino_Uuid(UUID casoDestinoId);
}
