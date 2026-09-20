package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Afastamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AfastamentoRepository extends JpaRepository<Afastamento, UUID> {
    List<Afastamento> findByProfissional_MatriculaOrderByDataInicioDesc(String matricula);
}
