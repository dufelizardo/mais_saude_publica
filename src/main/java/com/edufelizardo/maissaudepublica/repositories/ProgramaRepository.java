package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgramaRepository extends JpaRepository<Programa, UUID> {
    @Query("select distinct p from Programa p join fetch p.categoria left join fetch p.coordenador")
    List<Programa> findAllComCategoria();

    Optional<Programa> findByCodigo(String codigo);
}
