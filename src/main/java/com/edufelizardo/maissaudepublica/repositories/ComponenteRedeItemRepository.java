package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ComponenteRedeItem;
import com.edufelizardo.maissaudepublica.models.enuns.ComponenteRedeTematica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ComponenteRedeItemRepository extends JpaRepository<ComponenteRedeItem, UUID> {

    @Query("select i from ComponenteRedeItem i left join fetch i.perfil left join fetch i.capacidade")
    List<ComponenteRedeItem> findTodos();

    boolean existsByComponenteAndPerfil_Uuid(ComponenteRedeTematica componente, UUID perfilId);

    boolean existsByComponenteAndCapacidade_Uuid(ComponenteRedeTematica componente, UUID capacidadeId);
}
