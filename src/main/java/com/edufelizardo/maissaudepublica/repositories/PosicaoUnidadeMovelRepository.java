package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PosicaoUnidadeMovel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PosicaoUnidadeMovelRepository extends JpaRepository<PosicaoUnidadeMovel, UUID> {

    @Query("select p from PosicaoUnidadeMovel p join fetch p.ponto where p.unidade.uuid = ?1 order by p.desde desc")
    List<PosicaoUnidadeMovel> findDaUnidade(UUID unidadeId);
}
