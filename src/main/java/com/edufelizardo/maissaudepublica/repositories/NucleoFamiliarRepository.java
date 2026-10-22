package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.NucleoFamiliar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NucleoFamiliarRepository extends JpaRepository<NucleoFamiliar, UUID> {
    List<NucleoFamiliar> findByDomicilio_UuidInOrderByInicioDesc(Collection<UUID> domicilioIds);

    /** Famílias vigentes por microárea: [microareaId, quantidade]. */
    @Query("select n.domicilio.imovel.microarea.uuid, count(n) from NucleoFamiliar n where n.fim is null group by n.domicilio.imovel.microarea.uuid")
    List<Object[]> contarVigentesPorMicroarea();
}
