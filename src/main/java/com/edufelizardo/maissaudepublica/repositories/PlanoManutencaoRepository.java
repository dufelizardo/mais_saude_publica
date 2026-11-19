package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PlanoManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PlanoManutencaoRepository extends JpaRepository<PlanoManutencao, UUID> {

    @Query("select p from PlanoManutencao p join fetch p.bem b join fetch b.unidade where p.ativo = true order by p.proximaEm")
    List<PlanoManutencao> findAtivos();

    @Query("select p from PlanoManutencao p where p.bem.uuid = :bem order by p.ativo desc, p.proximaEm")
    List<PlanoManutencao> findDoBem(@Param("bem") UUID bem);
}
