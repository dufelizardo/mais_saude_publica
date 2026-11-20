package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.BemPatrimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BemPatrimonialRepository extends JpaRepository<BemPatrimonial, UUID> {

    @Query("select b from BemPatrimonial b join fetch b.unidade left join fetch b.setor left join fetch b.fornecedor left join fetch b.veiculo order by b.registradoEm desc")
    List<BemPatrimonial> findTodos();

    @Query("select b from BemPatrimonial b join fetch b.unidade left join fetch b.setor left join fetch b.fornecedor left join fetch b.veiculo where b.uuid in :ids order by b.registradoEm desc")
    List<BemPatrimonial> findComRelacoes(@Param("ids") Collection<UUID> ids);

    @Query("select max(b.numeroTombamento) from BemPatrimonial b where b.numeroTombamento like concat(:prefixo, '%')")
    String findMaiorNumero(@Param("prefixo") String prefixo);

    Optional<BemPatrimonial> findByVeiculoUuid(UUID veiculoId);
}
