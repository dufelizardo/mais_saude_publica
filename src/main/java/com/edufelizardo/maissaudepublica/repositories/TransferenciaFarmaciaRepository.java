package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TransferenciaFarmacia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferenciaFarmaciaRepository extends JpaRepository<TransferenciaFarmacia, UUID> {
    List<TransferenciaFarmacia> findAllByOrderByRegistradoEmDesc();
}
