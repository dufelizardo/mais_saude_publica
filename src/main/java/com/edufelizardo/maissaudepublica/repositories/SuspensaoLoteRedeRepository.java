package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.SuspensaoLoteRede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SuspensaoLoteRedeRepository extends JpaRepository<SuspensaoLoteRede, UUID> {

    @Query("select s from SuspensaoLoteRede s join fetch s.imunobiologico order by s.suspensaEm desc")
    List<SuspensaoLoteRede> findTodas();

    Optional<SuspensaoLoteRede> findFirstByImunobiologico_UuidAndNumeroLoteAndSituacao(UUID imunobiologicoId, String numeroLote,
                                                                                         com.edufelizardo.maissaudepublica.models.enuns.SituacaoSuspensaoLote situacao);
}
