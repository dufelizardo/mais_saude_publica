package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApuracaoCustoRepository extends JpaRepository<ApuracaoCusto, UUID> {

    Optional<ApuracaoCusto> findByCompetencia(String competencia);

    List<ApuracaoCusto> findAllByOrderByCompetenciaDesc();
}
