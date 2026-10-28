package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.UnidadeCapacidadeAssistencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UnidadeCapacidadeAssistencialRepository extends JpaRepository<UnidadeCapacidadeAssistencial, UUID> {
    @Query("select a from UnidadeCapacidadeAssistencial a join fetch a.capacidade c left join fetch c.perfil "
            + "where a.unidade.uuid = ?1 order by a.inicio")
    List<UnidadeCapacidadeAssistencial> findDaUnidade(UUID unidadeId);

    boolean existsByUnidade_UuidAndCapacidade_UuidAndFimIsNull(UUID unidadeId, UUID capacidadeId);

    boolean existsByUnidade_UuidAndCapacidade_CodigoAndFimIsNull(UUID unidadeId, String codigo);

    boolean existsByCapacidade_UuidAndFimIsNull(UUID capacidadeId);

    /** As vigentes de toda a rede, com a capacidade e a unidade (o painel da RAPS, ADR-0153). */
    @org.springframework.data.jpa.repository.Query("select a from UnidadeCapacidadeAssistencial a join fetch a.capacidade join fetch a.unidade "
            + "where a.fim is null")
    List<UnidadeCapacidadeAssistencial> findVigentesComCapacidade();
}
