package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EstoqueMinimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstoqueMinimoRepository extends JpaRepository<EstoqueMinimo, UUID> {

    Optional<EstoqueMinimo> findByMaterial_UuidAndUnidade_Uuid(UUID materialId, UUID unidadeId);

    @Query("select e from EstoqueMinimo e join fetch e.material join fetch e.unidade")
    List<EstoqueMinimo> findTodos();
}
