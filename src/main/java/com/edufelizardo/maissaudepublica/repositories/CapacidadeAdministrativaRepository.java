package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CapacidadeAdministrativa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CapacidadeAdministrativaRepository extends JpaRepository<CapacidadeAdministrativa, UUID> {
}
