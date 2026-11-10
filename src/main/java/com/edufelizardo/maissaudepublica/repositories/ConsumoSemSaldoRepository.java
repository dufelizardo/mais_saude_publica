package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ConsumoSemSaldo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConsumoSemSaldoRepository extends JpaRepository<ConsumoSemSaldo, UUID> {
    List<ConsumoSemSaldo> findByUnidade_UuidAndConferidoEmIsNullOrderByOcorridoEmDesc(UUID unidadeId);
}
