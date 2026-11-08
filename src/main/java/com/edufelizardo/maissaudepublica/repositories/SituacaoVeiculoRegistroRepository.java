package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.SituacaoVeiculoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SituacaoVeiculoRegistroRepository extends JpaRepository<SituacaoVeiculoRegistro, UUID> {

    List<SituacaoVeiculoRegistro> findByVeiculo_UuidOrderByInicioDesc(UUID veiculoId);
}
