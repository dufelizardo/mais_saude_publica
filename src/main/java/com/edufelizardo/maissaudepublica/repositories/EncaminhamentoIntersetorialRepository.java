package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEncaminhamentoIntersetorial;
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

    /** Os enviados no período e, de qualquer data, os ainda em aberto (painel da IS2, ADR-0211). */
    @Query("select e from EncaminhamentoIntersetorial e join fetch e.instituicao join fetch e.unidade "
            + "where e.enviadoEm between ?1 and ?2 or e.situacao in (?3, ?4)")
    List<EncaminhamentoIntersetorial> findParaPainel(LocalDate de, LocalDate ate, SituacaoEncaminhamentoIntersetorial aberto1,
                                                     SituacaoEncaminhamentoIntersetorial aberto2);

    @Query("select e from EncaminhamentoIntersetorial e join fetch e.unidade where e.instituicao.uuid = ?1 and e.situacao in (?2, ?3)")
    List<EncaminhamentoIntersetorial> findEmAbertoDaInstituicao(UUID instituicaoId, SituacaoEncaminhamentoIntersetorial aberto1,
                                                                SituacaoEncaminhamentoIntersetorial aberto2);
}
