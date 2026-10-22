package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Adscricao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdscricaoRepository extends JpaRepository<Adscricao, UUID> {
    Optional<Adscricao> findByPaciente_UuidAndFimIsNull(UUID pacienteId);

    List<Adscricao> findByPaciente_UuidOrderByInicioDesc(UUID pacienteId);

    List<Adscricao> findByEquipe_UuidAndFimIsNull(UUID equipeId);
}
