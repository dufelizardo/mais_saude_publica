package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MudancaNivelEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MudancaNivelEmergenciaRepository extends JpaRepository<MudancaNivelEmergencia, UUID> {

    @Query("select m from MudancaNivelEmergencia m left join fetch m.nivelAnterior join fetch m.nivelNovo where m.emergencia.uuid = ?1 order by m.mudadoEm desc")
    List<MudancaNivelEmergencia> findDaEmergencia(UUID emergenciaId);
}
