package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ProjetoTerapeuticoSingular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProjetoTerapeuticoSingularRepository extends JpaRepository<ProjetoTerapeuticoSingular, UUID> {
    @Query("select p from ProjetoTerapeuticoSingular p left join fetch p.profissional where p.acompanhamento.uuid = ?1 order by p.versao desc")
    List<ProjetoTerapeuticoSingular> findDoAcompanhamento(UUID acompanhamentoId);

    /** As versões vigentes dos casos, para os indicadores "Sem PTS" e "PTS a revisar". */
    @Query("select p from ProjetoTerapeuticoSingular p where p.acompanhamento.uuid in ?1 and p.substituidoEm is null")
    List<ProjetoTerapeuticoSingular> findVigentes(Collection<UUID> acompanhamentoIds);

    /** Todas as versões dos casos (para saber qual valia numa data, ADR-0155). */
    @Query("select p from ProjetoTerapeuticoSingular p where p.acompanhamento.uuid in ?1")
    List<ProjetoTerapeuticoSingular> findDosAcompanhamentos(Collection<UUID> acompanhamentoIds);
}
