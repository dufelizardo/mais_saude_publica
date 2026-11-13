package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CensoDiarioPaciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CensoDiarioPacienteRepository extends JpaRepository<CensoDiarioPaciente, UUID> {
    List<CensoDiarioPaciente> findByCenso_UuidOrderByLeitoIdentificacaoAsc(UUID censoId);
}
