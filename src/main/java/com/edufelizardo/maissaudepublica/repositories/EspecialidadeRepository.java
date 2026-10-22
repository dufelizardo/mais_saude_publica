package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EspecialidadeRepository extends JpaRepository<Especialidade, UUID> {
    List<Especialidade> findAllByOrderByNomeAsc();

    boolean existsByCodigo(String codigo);
}
