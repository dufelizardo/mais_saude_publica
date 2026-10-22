package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.IntervencaoPrevista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IntervencaoPrevistaRepository extends JpaRepository<IntervencaoPrevista, UUID> {
    @Query("select i from IntervencaoPrevista i join fetch i.tipo left join fetch i.responsavel where i.pts.uuid in ?1 order by i.ordem")
    List<IntervencaoPrevista> findDosPts(Collection<UUID> ptsIds);
}
