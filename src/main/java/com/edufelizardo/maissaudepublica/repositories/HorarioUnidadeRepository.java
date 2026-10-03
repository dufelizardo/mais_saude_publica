package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.HorarioUnidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HorarioUnidadeRepository extends JpaRepository<HorarioUnidade, UUID> {
    List<HorarioUnidade> findByUnidade_UuidOrderByDiaSemanaAscAbreAsc(UUID unidadeId);

    void deleteByUnidade_Uuid(UUID unidadeId);
}
