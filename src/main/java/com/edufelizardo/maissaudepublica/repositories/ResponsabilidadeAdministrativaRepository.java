package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ResponsabilidadeAdministrativa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResponsabilidadeAdministrativaRepository extends JpaRepository<ResponsabilidadeAdministrativa, UUID> {
    List<ResponsabilidadeAdministrativa> findByProfissional_MatriculaOrderByDataInicioDesc(String matricula);
}
