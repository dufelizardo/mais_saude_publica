package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MedicamentoRepository extends JpaRepository<Medicamento, UUID> {
}
