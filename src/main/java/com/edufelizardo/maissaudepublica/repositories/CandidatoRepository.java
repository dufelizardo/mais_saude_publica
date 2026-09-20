package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CandidatoRepository extends JpaRepository<Candidato, UUID> {
    List<Candidato> findByVaga_UuidOrderByNomeAsc(UUID vagaId);
}
