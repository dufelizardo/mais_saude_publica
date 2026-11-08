package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MaterialEducativo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MaterialEducativoRepository extends JpaRepository<MaterialEducativo, UUID> {

    List<MaterialEducativo> findByConteudoGrupoIdOrderByTituloAsc(UUID grupoId);

    List<MaterialEducativo> findByConteudoGrupoIdInAndRetiradoEmIsNullOrderByTituloAsc(Collection<UUID> grupos);
}
