package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ProcedimentoRegulado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcedimentoReguladoRepository extends JpaRepository<ProcedimentoRegulado, UUID> {
    List<ProcedimentoRegulado> findAllByOrderByNomeAsc();

    Optional<ProcedimentoRegulado> findByNomeIgnoreCase(String nome);
}
