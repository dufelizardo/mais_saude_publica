package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Comunicado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ComunicadoRepository extends JpaRepository<Comunicado, UUID> {

    List<Comunicado> findAllByOrderByInicioDesc();

    @Query("select distinct c from Comunicado c left join fetch c.unidade join c.canais k where c.situacao = 'PUBLICADO' and c.publico = 'POPULACAO' "
            + "and k.tipo = 'PORTAL' and c.inicio <= ?1 and c.fim >= ?1 order by c.inicio desc")
    List<Comunicado> findNoPortal(LocalDate hoje);
}
