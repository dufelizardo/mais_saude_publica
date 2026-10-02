package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ResultadoExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResultadoExameRepository extends JpaRepository<ResultadoExame, UUID> {
    List<ResultadoExame> findByItem_UuidOrderByRegistradoEmAsc(UUID itemId);
}
