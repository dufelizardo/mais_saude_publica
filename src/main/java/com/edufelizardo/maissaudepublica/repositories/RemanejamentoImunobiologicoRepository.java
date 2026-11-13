package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RemanejamentoImunobiologico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RemanejamentoImunobiologicoRepository extends JpaRepository<RemanejamentoImunobiologico, UUID> {

    @Query("select r from RemanejamentoImunobiologico r join fetch r.loteOrigem l join fetch l.imunobiologico join fetch l.unidade "
            + "join fetch r.unidadeDestino where l.unidade.uuid = ?1 or r.unidadeDestino.uuid = ?1 order by r.enviadoEm desc")
    List<RemanejamentoImunobiologico> findDaUnidade(UUID unidadeId);

    @Query("select r from RemanejamentoImunobiologico r where r.uuid = ?1")
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<RemanejamentoImunobiologico> findComTrava(UUID uuid);
}
