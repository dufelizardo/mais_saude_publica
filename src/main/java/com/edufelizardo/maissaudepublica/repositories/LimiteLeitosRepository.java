package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.LimiteLeitos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface LimiteLeitosRepository extends JpaRepository<LimiteLeitos, UUID> {

    /** O histórico da unidade, com o mais recente de cada capacidade antes dos demais. */
    @Query("select l from LimiteLeitos l join fetch l.capacidade where l.unidade.uuid = ?1 order by l.registradoEm desc")
    List<LimiteLeitos> findDaUnidade(UUID unidadeId);
}
