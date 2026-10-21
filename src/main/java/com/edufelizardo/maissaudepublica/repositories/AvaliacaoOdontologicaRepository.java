package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AvaliacaoOdontologica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AvaliacaoOdontologicaRepository extends JpaRepository<AvaliacaoOdontologica, UUID> {
    List<AvaliacaoOdontologica> findByAtendimento_UuidOrderByRegistradoEmAsc(UUID atendimentoId);

    boolean existsByRetificacaoDe_Uuid(UUID uuid);
}
