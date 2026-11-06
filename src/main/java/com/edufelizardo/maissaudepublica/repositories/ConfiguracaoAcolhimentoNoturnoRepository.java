package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ConfiguracaoAcolhimentoNoturno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConfiguracaoAcolhimentoNoturnoRepository extends JpaRepository<ConfiguracaoAcolhimentoNoturno, UUID> {

    Optional<ConfiguracaoAcolhimentoNoturno> findByUnidade_Uuid(UUID unidadeId);
}
