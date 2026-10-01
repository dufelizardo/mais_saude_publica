package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Dispensacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DispensacaoRepository extends JpaRepository<Dispensacao, UUID> {
}
