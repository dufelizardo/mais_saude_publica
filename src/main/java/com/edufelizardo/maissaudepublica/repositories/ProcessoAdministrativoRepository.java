package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ProcessoAdministrativo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessoAdministrativoRepository extends JpaRepository<ProcessoAdministrativo, UUID> {
}
