package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CapacidadeAssistencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CapacidadeAssistencialRepository extends JpaRepository<CapacidadeAssistencial, UUID> {
    @Query("select c from CapacidadeAssistencial c left join fetch c.perfil order by c.nome")
    List<CapacidadeAssistencial> findAllComPerfil();

    Optional<CapacidadeAssistencial> findByCodigo(String codigo);
}
