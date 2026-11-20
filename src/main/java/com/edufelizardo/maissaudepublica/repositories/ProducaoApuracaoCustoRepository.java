package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProducaoApuracaoCustoRepository extends JpaRepository<ProducaoApuracaoCusto, UUID> {

    @Query("select p from ProducaoApuracaoCusto p join fetch p.unidade where p.apuracao.uuid = ?1")
    List<ProducaoApuracaoCusto> findDaApuracao(UUID apuracaoId);

    @Modifying
    @Query("delete from ProducaoApuracaoCusto p where p.apuracao.uuid = ?1")
    void apagarDaApuracao(UUID apuracaoId);
}
