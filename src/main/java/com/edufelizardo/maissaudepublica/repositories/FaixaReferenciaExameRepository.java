package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.FaixaReferenciaExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FaixaReferenciaExameRepository extends JpaRepository<FaixaReferenciaExame, UUID> {
    List<FaixaReferenciaExame> findByExame_Uuid(UUID exameId);

    List<FaixaReferenciaExame> findByExame_UuidIn(Collection<UUID> exameIds);
}
