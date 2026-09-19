package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TipoBeneficio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TipoBeneficioRepository extends JpaRepository<TipoBeneficio, UUID> {
}
