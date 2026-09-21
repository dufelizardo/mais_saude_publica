package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AdesaoBeneficio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdesaoBeneficioRepository extends JpaRepository<AdesaoBeneficio, UUID> {
    List<AdesaoBeneficio> findByProfissional_MatriculaOrderByDataInicioDesc(String matricula);
}
