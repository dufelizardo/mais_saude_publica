package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AdendoFichaAph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdendoFichaAphRepository extends JpaRepository<AdendoFichaAph, UUID> {

    List<AdendoFichaAph> findByFicha_UuidOrderByRegistradoEmAsc(UUID fichaId);
}
