package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcaoEducativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcaoEducativaRepository extends JpaRepository<AcaoEducativa, UUID> {

    Optional<AcaoEducativa> findByAcao_Uuid(UUID acaoId);

    @Query("select e from AcaoEducativa e join fetch e.acao a left join fetch a.unidade where e.campanhaComunicacao.uuid = ?1 order by a.dataPrevista")
    List<AcaoEducativa> findDaCampanha(UUID campanhaId);

    @Query("select e from AcaoEducativa e join fetch e.acao")
    List<AcaoEducativa> findTodas();
}
