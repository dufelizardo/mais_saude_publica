package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MetaProgramatica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MetaProgramaticaRepository extends JpaRepository<MetaProgramatica, UUID> {
    @Query("select m from MetaProgramatica m left join fetch m.unidade left join fetch m.exame where m.programa.uuid = ?1 order by m.inicio desc, m.indicador")
    List<MetaProgramatica> findDoPrograma(UUID programaId);
}
