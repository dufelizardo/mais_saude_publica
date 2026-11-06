package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.BoletimEstrutura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BoletimEstruturaRepository extends JpaRepository<BoletimEstrutura, UUID> {

    @Query("select b from BoletimEstrutura b where b.estrutura.emergencia.uuid = ?1 order by b.data desc, b.registradoEm desc")
    List<BoletimEstrutura> findDaEmergencia(UUID emergenciaId);
}
