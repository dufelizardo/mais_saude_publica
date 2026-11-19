package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MembroInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MembroInventarioRepository extends JpaRepository<MembroInventario, UUID> {

    @Query("select m from MembroInventario m join fetch m.profissional where m.inventario.uuid in :inventarios")
    List<MembroInventario> findDosInventarios(@Param("inventarios") Collection<UUID> inventarios);

    @Query("select m.inventario.uuid from MembroInventario m where m.profissional.cpf = :cpf")
    List<UUID> findInventariosDoCpf(@Param("cpf") String cpf);
}
