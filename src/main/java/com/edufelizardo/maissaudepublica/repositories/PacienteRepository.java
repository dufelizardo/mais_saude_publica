package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {
    /**
     * CPF não é único (ver ADR-0017, mesmo raciocínio aplicado ao Paciente) — pode haver mais de
     * um registro com o mesmo CPF, cabe a quem consome decidir qual usar.
     */
    List<Paciente> findByCpf(String cpf);

    List<Paciente> findByCartaoSus(String cartaoSus);
}
