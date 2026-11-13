package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.FichaAph;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FichaAphRepository extends JpaRepository<FichaAph, UUID> {

    Optional<FichaAph> findByOcorrencia_Uuid(UUID ocorrenciaId);

    List<FichaAph> findByOcorrencia_UuidIn(Collection<UUID> ocorrenciaIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FichaAph f where f.uuid = :uuid")
    Optional<FichaAph> findByIdParaAtualizar(@Param("uuid") UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FichaAph f where f.ocorrencia.uuid = :ocorrencia")
    Optional<FichaAph> findByOcorrenciaParaAtualizar(@Param("ocorrencia") UUID ocorrenciaId);
}
