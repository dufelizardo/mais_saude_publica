package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.NivelEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NivelEmergenciaRepository extends JpaRepository<NivelEmergencia, UUID> {

    List<NivelEmergencia> findAllByOrderByOrdemAsc();

    boolean existsByOrdem(int ordem);
}
