package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AchadoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AchadoInventarioRepository extends JpaRepository<AchadoInventario, UUID> {

    @Query("select a from AchadoInventario a where a.inventario.uuid in :inventarios order by a.registradoEm")
    List<AchadoInventario> findDosInventarios(@Param("inventarios") Collection<UUID> inventarios);
}
