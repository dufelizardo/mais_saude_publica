package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ObservacaoAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ObservacaoAnimalRepository extends JpaRepository<ObservacaoAnimal, UUID> {

    @Query("select o from ObservacaoAnimal o join fetch o.animal order by o.fimPrevisto desc, o.registradoEm desc")
    List<ObservacaoAnimal> findTodas();

    @Query("select count(o) > 0 from ObservacaoAnimal o where o.animal.uuid = ?1 and o.desfecho is null")
    boolean existeAbertaDoAnimal(UUID animalId);

    @Query("select count(o) > 0 from ObservacaoAnimal o where o.notificacao.uuid = ?1 and o.desfecho is null")
    boolean existeAbertaDaNotificacao(UUID notificacaoId);
}
