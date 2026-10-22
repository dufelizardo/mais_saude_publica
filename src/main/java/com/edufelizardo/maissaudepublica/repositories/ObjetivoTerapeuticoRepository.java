package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ObjetivoTerapeutico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ObjetivoTerapeuticoRepository extends JpaRepository<ObjetivoTerapeutico, UUID> {
    @Query("select o from ObjetivoTerapeutico o left join fetch o.responsavel where o.pts.uuid in ?1 order by o.ordem")
    List<ObjetivoTerapeutico> findDosPts(Collection<UUID> ptsIds);
}
