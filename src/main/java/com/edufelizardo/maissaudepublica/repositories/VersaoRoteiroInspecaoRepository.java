package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.VersaoRoteiroInspecao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VersaoRoteiroInspecaoRepository extends JpaRepository<VersaoRoteiroInspecao, UUID> {

    Optional<VersaoRoteiroInspecao> findFirstByRoteiro_UuidOrderByVersaoDesc(UUID roteiroId);
}
