package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ClassificacaoCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ClassificacaoCasoRepository extends JpaRepository<ClassificacaoCaso, UUID> {
    @Query("select c from ClassificacaoCaso c left join fetch c.responsavel where c.notificacao.uuid = ?1 order by c.classificadaEm desc")
    List<ClassificacaoCaso> findDaNotificacao(UUID notificacaoId);
}
