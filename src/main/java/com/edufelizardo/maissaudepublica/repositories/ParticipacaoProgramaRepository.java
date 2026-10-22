package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ParticipacaoPrograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ParticipacaoProgramaRepository extends JpaRepository<ParticipacaoPrograma, UUID> {
    @Query("select p from ParticipacaoPrograma p join fetch p.unidade left join fetch p.equipe where p.programa.uuid = ?1 order by p.inicio")
    List<ParticipacaoPrograma> findDoPrograma(UUID programaId);

    @Query("select p from ParticipacaoPrograma p join fetch p.unidade where p.fim is null")
    List<ParticipacaoPrograma> findVigentesComUnidade();
}
