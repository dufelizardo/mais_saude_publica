package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RoteiroInspecao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoteiroInspecaoRepository extends JpaRepository<RoteiroInspecao, UUID> {

    Optional<RoteiroInspecao> findByCodigo(String codigo);

    List<RoteiroInspecao> findAllByOrderByNomeAsc();
}
