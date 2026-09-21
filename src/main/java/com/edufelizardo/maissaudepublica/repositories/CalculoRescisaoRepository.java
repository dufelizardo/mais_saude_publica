package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CalculoRescisao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CalculoRescisaoRepository extends JpaRepository<CalculoRescisao, UUID> {
    List<CalculoRescisao> findByProfissional_MatriculaOrderByUuidDesc(String matricula);
}
