package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.OrdemServicoManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrdemServicoManutencaoRepository extends JpaRepository<OrdemServicoManutencao, UUID> {

    @Query("select o from OrdemServicoManutencao o join fetch o.bem b join fetch b.unidade left join fetch o.plano left join fetch o.instrumento order by o.abertaEm desc, o.numero desc")
    List<OrdemServicoManutencao> findTodas();

    @Query("select o from OrdemServicoManutencao o join fetch o.bem b join fetch b.unidade left join fetch o.plano left join fetch o.instrumento where b.uuid = :bem order by o.abertaEm desc, o.numero desc")
    List<OrdemServicoManutencao> findDoBem(@Param("bem") UUID bem);

    @Query("select o from OrdemServicoManutencao o join fetch o.bem b join fetch b.unidade left join fetch o.plano left join fetch o.instrumento where o.abertaPorCpf = :cpf order by o.abertaEm desc, o.numero desc")
    List<OrdemServicoManutencao> findAbertasPor(@Param("cpf") String cpf);

    @Query("select max(o.numero) from OrdemServicoManutencao o where o.numero like concat(:prefixo, '%')")
    String findMaiorNumero(@Param("prefixo") String prefixo);
}
