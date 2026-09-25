package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PerfilPorTipoUnidade;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerfilPorTipoUnidadeRepository extends JpaRepository<PerfilPorTipoUnidade, UUID> {
    Optional<PerfilPorTipoUnidade> findByTipo(TipoUnidadeDeSaude tipo);
}
