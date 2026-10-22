package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TratamentoProtetico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TratamentoProteticoRepository extends JpaRepository<TratamentoProtetico, UUID> {
    List<TratamentoProtetico> findByPaciente_UuidOrderByAbertoEmDesc(UUID pacienteId);

    /** Próteses entregues no intervalo (indicadores, ADR-0141). */
    @org.springframework.data.jpa.repository.Query("select t from TratamentoProtetico t join fetch t.unidade where t.dataEntrega >= ?1 and t.dataEntrega <= ?2")
    List<TratamentoProtetico> findEntreguesNoIntervalo(java.time.LocalDate de, java.time.LocalDate ate);
}
