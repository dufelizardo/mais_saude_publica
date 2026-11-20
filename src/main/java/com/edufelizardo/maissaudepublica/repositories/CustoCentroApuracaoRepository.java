package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CustoCentroApuracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/** CustoCentroApuracao (ADR-0221). */
public interface CustoCentroApuracaoRepository extends JpaRepository<CustoCentroApuracao, UUID> {

    @Query("select c from CustoCentroApuracao c join fetch c.centro x join fetch x.unidade left join fetch x.setor where c.apuracao.uuid = ?1")
    List<CustoCentroApuracao> findDaApuracao(UUID apuracaoId);

    @Modifying
    @Query("delete from CustoCentroApuracao c where c.apuracao.uuid = ?1")
    void apagarDaApuracao(UUID apuracaoId);
}
