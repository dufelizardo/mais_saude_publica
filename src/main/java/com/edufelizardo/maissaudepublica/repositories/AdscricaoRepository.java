package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Adscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdscricaoRepository extends JpaRepository<Adscricao, UUID> {
    Optional<Adscricao> findByPaciente_UuidAndFimIsNull(UUID pacienteId);

    List<Adscricao> findByPaciente_UuidOrderByInicioDesc(UUID pacienteId);

    List<Adscricao> findByEquipe_UuidAndFimIsNull(UUID equipeId);

    /** As adscrições vigentes com o paciente, a equipe, a unidade e a microárea (a cobertura vacinal, ADR-0149). */
    @Query("select a from Adscricao a join fetch a.paciente join fetch a.equipe e join fetch e.unidade left join fetch a.microarea "
            + "where a.fim is null")
    List<Adscricao> findVigentesCompletas();
}
