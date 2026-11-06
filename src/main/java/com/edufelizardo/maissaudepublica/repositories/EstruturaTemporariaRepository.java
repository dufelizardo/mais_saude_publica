package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EstruturaTemporaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EstruturaTemporariaRepository extends JpaRepository<EstruturaTemporaria, UUID> {

    @Query("select e from EstruturaTemporaria e join fetch e.unidadeReferencia where e.emergencia.uuid = ?1 order by lower(e.nome)")
    List<EstruturaTemporaria> findDaEmergencia(UUID emergenciaId);
}
