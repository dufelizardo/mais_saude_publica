package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoteRepository extends JpaRepository<Lote, UUID> {
    /**
     * Todos os lotes de um medicamento (em qualquer unidade) — útil pra uma futura tela "estoque
     * deste medicamento por unidade" (ver ADR-0050).
     */
    List<Lote> findByMedicamentoUuid(UUID medicamentoUuid);
}
