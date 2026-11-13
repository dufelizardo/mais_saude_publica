package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RiscoOcupacional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.edufelizardo.maissaudepublica.models.RiscoOcupacional;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RiscoOcupacionalRepository extends JpaRepository<RiscoOcupacional, UUID> {
    List<RiscoOcupacional> findByInvestigacao_UuidOrderByRegistradoEmAsc(UUID investigacaoId);

    @Query("select r from RiscoOcupacional r order by r.empregadorNome, r.registradoEm")
    List<RiscoOcupacional> findTodos();
}
