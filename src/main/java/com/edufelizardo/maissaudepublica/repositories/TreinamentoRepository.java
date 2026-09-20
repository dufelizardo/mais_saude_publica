package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Treinamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TreinamentoRepository extends JpaRepository<Treinamento, UUID> {
}
