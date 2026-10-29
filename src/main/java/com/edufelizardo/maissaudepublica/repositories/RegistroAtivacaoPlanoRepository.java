package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RegistroAtivacaoPlano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RegistroAtivacaoPlanoRepository extends JpaRepository<RegistroAtivacaoPlano, UUID> {

    @Query("select r from RegistroAtivacaoPlano r join fetch r.nivel where r.ativacao.uuid in ?1 order by r.registradoEm desc")
    List<RegistroAtivacaoPlano> findDasAtivacoes(java.util.Collection<UUID> ativacoes);
}
