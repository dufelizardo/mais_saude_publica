package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ValorBeneficio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValorBeneficioRepository extends JpaRepository<ValorBeneficio, UUID> {
    List<ValorBeneficio> findByTipoBeneficio_UuidOrderByDataVigenciaDesc(UUID tipoBeneficioId);

    Optional<ValorBeneficio> findFirstByTipoBeneficio_UuidAndDataVigenciaLessThanEqualOrderByDataVigenciaDesc(
            UUID tipoBeneficioId, LocalDate data);
}
