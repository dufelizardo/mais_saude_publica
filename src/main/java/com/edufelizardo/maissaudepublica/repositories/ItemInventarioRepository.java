package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ItemInventarioRepository extends JpaRepository<ItemInventario, UUID> {

    @Query("select i from ItemInventario i join fetch i.bem b left join fetch i.setorEsperado left join fetch i.setorEncontrado left join fetch i.unidadeEncontrada where i.inventario.uuid in :inventarios order by b.numeroTombamento")
    List<ItemInventario> findDosInventarios(@Param("inventarios") Collection<UUID> inventarios);
}
