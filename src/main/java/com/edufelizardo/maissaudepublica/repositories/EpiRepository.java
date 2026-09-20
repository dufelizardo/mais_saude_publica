package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Epi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EpiRepository extends JpaRepository<Epi, UUID> {
    List<Epi> findByProfissional_MatriculaOrderByDataEntregaDesc(String matricula);
}
