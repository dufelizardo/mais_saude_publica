package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.FaixaAih;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaixaAihRepository extends JpaRepository<FaixaAih, UUID> {

    List<FaixaAih> findAllByOrderByAnoDescInicioAsc();

    /** Faixas que se sobrepõem ao intervalo informado. */
    @Query("select f from FaixaAih f where f.inicio <= :fim and f.fim >= :inicio")
    List<FaixaAih> findSobrepostas(@Param("inicio") long inicio, @Param("fim") long fim);

    /** As faixas ativas do ano com número livre, travadas para dar o próximo número sem repetir. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FaixaAih f where f.ano = :ano and f.ativa = true and f.proximo <= f.fim order by f.inicio asc")
    List<FaixaAih> findVigentesParaNumerar(@Param("ano") int ano);
}
