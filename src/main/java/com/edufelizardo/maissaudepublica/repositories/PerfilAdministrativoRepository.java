package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PerfilAdministrativo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PerfilAdministrativoRepository extends JpaRepository<PerfilAdministrativo, UUID> {
}
