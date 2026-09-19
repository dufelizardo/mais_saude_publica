package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RegistroPonto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, UUID> {
    List<RegistroPonto> findByProfissional_MatriculaOrderByDataHoraDesc(String matricula);
}
