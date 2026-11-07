package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ServicoUnidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ServicoUnidadeRepository extends JpaRepository<ServicoUnidade, UUID> {

    @Query("select s from ServicoUnidade s join fetch s.tipo left join fetch s.responsavel where s.unidade.uuid = ?1 order by lower(s.nome)")
    List<ServicoUnidade> findDaUnidade(UUID unidadeId);

    @Query("select count(s) from ServicoUnidade s where s.tipo.uuid = ?1 and s.fim is null")
    long contarVigentesDoTipo(UUID tipoId);

    /** Os serviços vigentes da unidade que dependem da capacidade: ela não encerra antes deles. */
    @Query("select s.nome from ServicoUnidade s where s.unidade.uuid = ?1 and s.tipo.capacidadeExigida.uuid = ?2 and s.fim is null order by s.nome")
    List<String> findVigentesQueExigem(UUID unidadeId, UUID capacidadeId);
}
