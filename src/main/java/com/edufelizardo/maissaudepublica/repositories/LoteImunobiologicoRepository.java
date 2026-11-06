package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.LoteImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLoteImunobiologico;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoteImunobiologicoRepository extends JpaRepository<LoteImunobiologico, UUID> {

    @Query("select l from LoteImunobiologico l join fetch l.imunobiologico left join fetch l.camara where l.unidade.uuid = ?1 order by l.validade, l.numeroLote")
    List<LoteImunobiologico> findDaUnidade(UUID unidadeId);

    Optional<LoteImunobiologico> findByUnidade_UuidAndImunobiologico_UuidAndNumeroLote(UUID unidadeId, UUID imunobiologicoId, String numeroLote);

    List<LoteImunobiologico> findByCamara_UuidAndSituacao(UUID camaraId, SituacaoLoteImunobiologico situacao);

    long countByCamara_UuidAndSituacao(UUID camaraId, SituacaoLoteImunobiologico situacao);

    /** Trava o lote para a baixa (duas aplicações ao mesmo tempo não passam do saldo). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoteImunobiologico l where l.uuid = ?1")
    Optional<LoteImunobiologico> findComTrava(UUID uuid);
}
