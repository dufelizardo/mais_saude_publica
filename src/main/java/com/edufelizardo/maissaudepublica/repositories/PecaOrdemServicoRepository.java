package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PecaOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PecaOrdemServicoRepository extends JpaRepository<PecaOrdemServico, UUID> {

    @Query("select p from PecaOrdemServico p left join fetch p.material where p.ordem.uuid in :ordens")
    List<PecaOrdemServico> findDasOrdens(@Param("ordens") Collection<UUID> ordens);
}
