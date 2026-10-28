package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Imunobiologico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImunobiologicoRepository extends JpaRepository<Imunobiologico, UUID> {

    Optional<Imunobiologico> findByCodigo(String codigo);

    List<Imunobiologico> findAllByOrderByNomeAsc();
}
