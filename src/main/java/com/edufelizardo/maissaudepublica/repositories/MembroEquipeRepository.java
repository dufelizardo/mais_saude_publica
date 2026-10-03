package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MembroEquipeRepository extends JpaRepository<MembroEquipe, UUID> {
    List<MembroEquipe> findByEquipe_UuidOrderByInicioAsc(UUID equipeId);

    List<MembroEquipe> findByEquipe_UuidInAndFimIsNull(Collection<UUID> equipeIds);

    List<MembroEquipe> findByProfissional_UuidAndFimIsNull(UUID profissionalId);
}
