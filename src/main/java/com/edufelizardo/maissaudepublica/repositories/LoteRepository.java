package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Lote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoteRepository extends JpaRepository<Lote, UUID> {
    /**
     * Trava a linha do lote até o fim da transação (SELECT ... FOR UPDATE). Toda movimentação de
     * estoque busca o lote por aqui, para duas operações simultâneas não lerem o mesmo saldo e
     * deixarem o estoque errado (ver ADR-0057).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lote l where l.uuid = :uuid")
    Optional<Lote> findByIdParaMovimentar(@Param("uuid") UUID uuid);

    /**
     * Todos os lotes de um medicamento (em qualquer unidade) — útil pra uma futura tela "estoque
     * deste medicamento por unidade" (ver ADR-0050).
     */
    List<Lote> findByMedicamentoUuid(UUID medicamentoUuid);

    /**
     * Ids dos lotes da mesma remessa (medicamento, número e validade) de {@code origemId} que já
     * existem na unidade de destino — alvo de uma transferência entre unidades (ADR-0059). Devolve só
     * ids, sem carregar entidades, para que origem e destino sejam travados depois por
     * {@link #findByIdParaMovimentar} sempre na mesma ordem (evita impasse entre transferências
     * cruzadas A→B e B→A).
     */
    @Query("select d.uuid from Lote d, Lote o where o.uuid = :origemId and d.unidade.uuid = :unidadeDestinoId "
            + "and d.medicamento = o.medicamento and d.numeroLote = o.numeroLote and d.validade = o.validade "
            + "order by d.uuid")
    List<UUID> findIdsDaMesmaRemessaNaUnidade(@Param("origemId") UUID origemId,
                                               @Param("unidadeDestinoId") UUID unidadeDestinoId);
}
