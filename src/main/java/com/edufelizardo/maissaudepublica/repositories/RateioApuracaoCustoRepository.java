package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RateioApuracaoCusto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/** RateioApuracaoCusto (ADR-0221). */
public interface RateioApuracaoCustoRepository extends JpaRepository<RateioApuracaoCusto, UUID> {

    @Query("select c from RateioApuracaoCusto c join fetch c.origem o join fetch o.unidade join fetch c.destino d where c.apuracao.uuid = ?1 order by c.ordem, d.codigo")
    List<RateioApuracaoCusto> findDaApuracao(UUID apuracaoId);

    @Modifying
    @Query("delete from RateioApuracaoCusto c where c.apuracao.uuid = ?1")
    void apagarDaApuracao(UUID apuracaoId);
}
