package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CicloAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CicloAvaliacaoRepository extends JpaRepository<CicloAvaliacao, UUID> {
}
