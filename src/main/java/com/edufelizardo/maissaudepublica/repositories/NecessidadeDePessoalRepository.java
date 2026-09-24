package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.NecessidadeDePessoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NecessidadeDePessoalRepository extends JpaRepository<NecessidadeDePessoal, UUID> {
}
