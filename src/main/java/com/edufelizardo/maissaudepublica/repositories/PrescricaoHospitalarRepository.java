package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PrescricaoHospitalar;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescricaoHospitalarRepository extends JpaRepository<PrescricaoHospitalar, UUID> {

    List<PrescricaoHospitalar> findByInternacao_UuidOrderByInicioDesc(UUID internacaoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PrescricaoHospitalar p where p.internacao.uuid = :internacao order by p.inicio desc")
    List<PrescricaoHospitalar> findDaInternacaoParaAtualizar(@Param("internacao") UUID internacaoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PrescricaoHospitalar p where p.uuid = :uuid")
    Optional<PrescricaoHospitalar> findByIdParaAtualizar(@Param("uuid") UUID uuid);
}
