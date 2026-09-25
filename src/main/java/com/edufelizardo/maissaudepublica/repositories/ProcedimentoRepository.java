package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Procedimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcedimentoRepository extends JpaRepository<Procedimento, UUID> {
}
