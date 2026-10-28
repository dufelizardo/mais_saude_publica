package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.LinhaDeCuidado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinhaDeCuidadoRepository extends JpaRepository<LinhaDeCuidado, UUID> {

    @Query("select l from LinhaDeCuidado l left join fetch l.programa order by l.nome")
    List<LinhaDeCuidado> findTodas();

    boolean existsByCodigoIgnoreCase(String codigo);

    Optional<LinhaDeCuidado> findByCodigo(String codigo);
}
