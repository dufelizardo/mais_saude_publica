package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AreaGestao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AreaGestaoRepository extends JpaRepository<AreaGestao, UUID> {

    List<AreaGestao> findAllByOrderByNomeAsc();

    List<AreaGestao> findByRegional_UuidAndAtivaTrue(UUID regionalId);

    List<AreaGestao> findBySuperior_UuidAndAtivaTrue(UUID superiorId);
}
