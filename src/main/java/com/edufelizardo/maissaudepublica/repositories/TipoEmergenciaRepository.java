package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TipoEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TipoEmergenciaRepository extends JpaRepository<TipoEmergencia, UUID> {

    List<TipoEmergencia> findAllByOrderByNomeAsc();

    boolean existsByCodigoIgnoreCase(String codigo);
}
