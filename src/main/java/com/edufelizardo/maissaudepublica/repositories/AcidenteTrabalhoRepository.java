package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcidenteTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AcidenteTrabalhoRepository extends JpaRepository<AcidenteTrabalho, UUID> {
    List<AcidenteTrabalho> findByProfissional_MatriculaOrderByDataHoraDesc(String matricula);
}
