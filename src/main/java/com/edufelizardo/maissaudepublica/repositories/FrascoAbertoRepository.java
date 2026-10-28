package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.FrascoAberto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FrascoAbertoRepository extends JpaRepository<FrascoAberto, UUID> {

    List<FrascoAberto> findByLote_UuidAndEncerradoEmIsNullOrderByAbertoEmAsc(UUID loteId);

    @Query("select f from FrascoAberto f join fetch f.lote l join fetch l.imunobiologico where l.unidade.uuid = ?1 and f.encerradoEm is null order by f.validoAte")
    List<FrascoAberto> findAbertosDaUnidade(UUID unidadeId);
}
