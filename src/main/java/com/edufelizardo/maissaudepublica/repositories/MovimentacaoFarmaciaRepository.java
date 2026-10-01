package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MovimentacaoFarmacia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovimentacaoFarmaciaRepository extends JpaRepository<MovimentacaoFarmacia, UUID> {

    List<MovimentacaoFarmacia> findByLote_UuidOrderByRegistradoEmAsc(UUID loteId);

    boolean existsByLote_Uuid(UUID loteId);
}
