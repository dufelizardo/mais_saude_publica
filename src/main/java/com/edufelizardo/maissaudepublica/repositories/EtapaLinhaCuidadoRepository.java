package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EtapaLinhaCuidado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EtapaLinhaCuidadoRepository extends JpaRepository<EtapaLinhaCuidado, UUID> {

    /** As etapas da linha, na ordem (as desativadas também, para os registros antigos). */
    @Query("select e from EtapaLinhaCuidado e left join fetch e.especialidade left join fetch e.exame left join fetch e.procedimento "
            + "left join fetch e.imunobiologico where e.linha.uuid = ?1 order by e.ordem")
    List<EtapaLinhaCuidado> findDaLinha(UUID linhaId);
}
