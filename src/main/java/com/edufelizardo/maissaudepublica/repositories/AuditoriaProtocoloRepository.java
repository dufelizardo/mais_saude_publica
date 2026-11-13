package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AuditoriaProtocolo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AuditoriaProtocoloRepository extends JpaRepository<AuditoriaProtocolo, UUID> {

    @Query("select a from AuditoriaProtocolo a join fetch a.unidade join fetch a.versao v join fetch v.checklist left join fetch a.setor "
            + "where a.dataAuditoria >= ?1 and a.dataAuditoria <= ?2 order by a.dataAuditoria desc")
    List<AuditoriaProtocolo> findNoPeriodo(java.time.LocalDate de, java.time.LocalDate ate);
}
