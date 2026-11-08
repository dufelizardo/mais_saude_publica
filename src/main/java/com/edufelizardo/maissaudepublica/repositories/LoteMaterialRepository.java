package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.LoteMaterial;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoteMaterialRepository extends JpaRepository<LoteMaterial, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoteMaterial l where l.uuid = :uuid")
    Optional<LoteMaterial> findByIdParaAtualizar(@Param("uuid") UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoteMaterial l where l.material.uuid = :material and l.unidade.uuid = :unidade order by l.validade nulls last, l.criadoEm")
    List<LoteMaterial> findDoMaterialNaUnidadeParaAtualizar(@Param("material") UUID material, @Param("unidade") UUID unidade);

    @Query("select l from LoteMaterial l join fetch l.material join fetch l.unidade where l.unidade.uuid = ?1")
    List<LoteMaterial> findDaUnidade(UUID unidadeId);

    @Query("select l from LoteMaterial l join fetch l.material join fetch l.unidade")
    List<LoteMaterial> findTodos();

    boolean existsByMaterial_Uuid(UUID materialId);
}
