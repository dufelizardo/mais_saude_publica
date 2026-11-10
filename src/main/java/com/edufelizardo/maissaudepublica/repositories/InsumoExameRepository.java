package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.InsumoExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface InsumoExameRepository extends JpaRepository<InsumoExame, UUID> {
    List<InsumoExame> findByExame_Uuid(UUID exameId);

    List<InsumoExame> findByExame_UuidIn(Collection<UUID> exameIds);
}
