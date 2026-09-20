package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ExameOcupacional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExameOcupacionalRepository extends JpaRepository<ExameOcupacional, UUID> {
    List<ExameOcupacional> findByProfissional_MatriculaOrderByDataRealizacaoDesc(String matricula);
}
