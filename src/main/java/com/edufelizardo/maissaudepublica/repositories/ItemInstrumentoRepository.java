package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemInstrumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ItemInstrumentoRepository extends JpaRepository<ItemInstrumento, UUID> {

    @Query("select i from ItemInstrumento i join fetch i.itemContratacao where i.instrumento.uuid in :ids")
    List<ItemInstrumento> findDosInstrumentos(@Param("ids") Collection<UUID> ids);
}
