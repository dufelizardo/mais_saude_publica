package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EncaminhamentoIntersetorialRepository extends JpaRepository<EncaminhamentoIntersetorial, UUID> {

    @Query("select e from EncaminhamentoIntersetorial e join fetch e.instituicao left join fetch e.profissional "
            + "where e.dominioOrigem = ?1 and e.origemId = ?2 order by e.enviadoEm desc, e.registradoEm desc")
    List<EncaminhamentoIntersetorial> findDaOrigem(DominioOrigemEncaminhamento dominio, UUID origemId);

    @Query("select e from EncaminhamentoIntersetorial e where e.dominioOrigem = ?1 and e.origemId in ?2")
    List<EncaminhamentoIntersetorial> findDasOrigens(DominioOrigemEncaminhamento dominio, Collection<UUID> origens);

    @Query("select e from EncaminhamentoIntersetorial e join fetch e.unidade where e.dominioOrigem = ?1 and e.enviadoEm between ?2 and ?3")
    List<EncaminhamentoIntersetorial> findEnviadosNoIntervalo(DominioOrigemEncaminhamento dominio, LocalDate de, LocalDate ate);
}
