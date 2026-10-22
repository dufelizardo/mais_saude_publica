package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TratamentoProtetico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TratamentoProteticoRepository extends JpaRepository<TratamentoProtetico, UUID> {
    List<TratamentoProtetico> findByPaciente_UuidOrderByAbertoEmDesc(UUID pacienteId);
}
