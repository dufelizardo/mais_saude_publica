package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.IncidenteSeguranca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IncidenteSegurancaRepository extends JpaRepository<IncidenteSeguranca, UUID> {

    @Query("select i from IncidenteSeguranca i join fetch i.unidade left join fetch i.paciente order by i.ocorridoEm desc")
    List<IncidenteSeguranca> findTodos();

    long countByNumeroStartingWith(String prefixo);
}
