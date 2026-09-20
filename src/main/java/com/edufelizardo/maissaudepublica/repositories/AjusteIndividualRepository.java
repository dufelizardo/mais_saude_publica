package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AjusteIndividual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AjusteIndividualRepository extends JpaRepository<AjusteIndividual, UUID> {
    List<AjusteIndividual> findByProfissional_MatriculaOrderByDataInicioDesc(String matricula);
}
