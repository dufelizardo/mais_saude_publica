package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.BlocoAgenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlocoAgendaRepository extends JpaRepository<BlocoAgenda, UUID> {
    List<BlocoAgenda> findByProfissional_UuidOrderByDiaSemanaAscHoraInicioAsc(UUID profissionalId);

    List<BlocoAgenda> findByProfissional_UuidAndUnidade_UuidOrderByDiaSemanaAscHoraInicioAsc(UUID profissionalId, UUID unidadeId);

    List<BlocoAgenda> findByUnidade_UuidOrderByDiaSemanaAscHoraInicioAsc(UUID unidadeId);
}
