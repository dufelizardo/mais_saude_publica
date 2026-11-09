package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CotaPpi;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CotaPpiRepository extends JpaRepository<CotaPpi, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CotaPpi c where c.uuid = :uuid")
    Optional<CotaPpi> findByIdParaAtualizar(@Param("uuid") UUID uuid);

    @Query("select c from CotaPpi c join fetch c.beneficiaria where c.oferta.uuid in ?1")
    List<CotaPpi> findDasOfertas(Collection<UUID> ofertas);

    boolean existsByOferta_UuidAndBeneficiaria_Uuid(UUID ofertaId, UUID beneficiariaId);
}
