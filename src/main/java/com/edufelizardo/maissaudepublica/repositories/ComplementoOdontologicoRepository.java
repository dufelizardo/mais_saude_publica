package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ComplementoOdontologico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComplementoOdontologicoRepository extends JpaRepository<ComplementoOdontologico, UUID> {
    Optional<ComplementoOdontologico> findByProcedimento_Uuid(UUID procedimentoId);

    List<ComplementoOdontologico> findByProcedimento_UuidIn(Collection<UUID> procedimentoIds);
}
