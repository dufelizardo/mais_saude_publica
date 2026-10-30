package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Desmobilizacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DesmobilizacaoRepository extends JpaRepository<Desmobilizacao, UUID> {

    Optional<Desmobilizacao> findByEmergencia_Uuid(UUID emergenciaId);
}
