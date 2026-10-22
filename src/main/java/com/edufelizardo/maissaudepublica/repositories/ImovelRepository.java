package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Imovel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImovelRepository extends JpaRepository<Imovel, UUID> {
    List<Imovel> findByMicroarea_Area_Uuid(UUID areaId);

    /** Imóveis ativos por microárea: [microareaId, quantidade]. */
    @Query("select i.microarea.uuid, count(i) from Imovel i where i.ativo = true group by i.microarea.uuid")
    List<Object[]> contarPorMicroarea();
}
