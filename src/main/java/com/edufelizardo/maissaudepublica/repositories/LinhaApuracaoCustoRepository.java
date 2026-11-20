package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinhaApuracaoCustoRepository extends JpaRepository<LinhaApuracaoCusto, UUID> {

    @Query("select l from LinhaApuracaoCusto l join fetch l.centro c join fetch c.unidade left join fetch c.setor where l.apuracao.uuid = ?1")
    List<LinhaApuracaoCusto> findDaApuracao(UUID apuracaoId);

    @Query("select l.apuracao.uuid, sum(l.valor) from LinhaApuracaoCusto l group by l.apuracao.uuid")
    List<Object[]> findTotais();

    @Modifying
    @Query("delete from LinhaApuracaoCusto l where l.apuracao.uuid = ?1")
    void apagarDaApuracao(UUID apuracaoId);
}
