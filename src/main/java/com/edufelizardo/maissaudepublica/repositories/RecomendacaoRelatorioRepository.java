package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RecomendacaoRelatorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecomendacaoRelatorioRepository extends JpaRepository<RecomendacaoRelatorio, UUID> {
}
