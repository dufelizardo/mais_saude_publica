package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MedicamentoAph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicamentoAphRepository extends JpaRepository<MedicamentoAph, UUID> {

    List<MedicamentoAph> findByFicha_UuidOrderByAdministradoEmAsc(UUID fichaId);
}
