package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MembroNucleo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembroNucleoRepository extends JpaRepository<MembroNucleo, UUID> {
    List<MembroNucleo> findByNucleo_UuidInOrderByInicioAsc(Collection<UUID> nucleoIds);

    List<MembroNucleo> findByNucleo_UuidAndFimIsNull(UUID nucleoId);

    Optional<MembroNucleo> findByPaciente_UuidAndFimIsNull(UUID pacienteId);

    List<MembroNucleo> findByPaciente_UuidOrderByInicioDesc(UUID pacienteId);

    /** Pessoas vigentes por microárea: [microareaId, quantidade]. */
    @Query("select m.nucleo.domicilio.imovel.microarea.uuid, count(m) from MembroNucleo m where m.fim is null group by m.nucleo.domicilio.imovel.microarea.uuid")
    List<Object[]> contarVigentesPorMicroarea();
}
