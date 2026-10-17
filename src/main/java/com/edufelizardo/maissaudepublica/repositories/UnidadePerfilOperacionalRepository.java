package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.UnidadePerfilOperacional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UnidadePerfilOperacionalRepository extends JpaRepository<UnidadePerfilOperacional, UUID> {
    @Query("select a from UnidadePerfilOperacional a join fetch a.perfil where a.unidade.uuid = ?1 order by a.inicio")
    List<UnidadePerfilOperacional> findDaUnidade(UUID unidadeId);

    boolean existsByUnidade_UuidAndPerfil_UuidAndFimIsNull(UUID unidadeId, UUID perfilId);

    @Query("select a from UnidadePerfilOperacional a join fetch a.perfil where a.fim is null")
    List<UnidadePerfilOperacional> findVigentesComPerfil();
}
