package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AtivacaoPlano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AtivacaoPlanoRepository extends JpaRepository<AtivacaoPlano, UUID> {

    @Query("select a from AtivacaoPlano a join fetch a.plano where a.emergencia.uuid = ?1 order by a.ativadaEm")
    List<AtivacaoPlano> findDaEmergencia(UUID emergenciaId);
}
