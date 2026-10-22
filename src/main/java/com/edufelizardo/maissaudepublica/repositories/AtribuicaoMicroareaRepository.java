package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AtribuicaoMicroarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AtribuicaoMicroareaRepository extends JpaRepository<AtribuicaoMicroarea, UUID> {
    List<AtribuicaoMicroarea> findByFimIsNull();

    Optional<AtribuicaoMicroarea> findByMicroarea_UuidAndFimIsNull(UUID microareaId);

    Optional<AtribuicaoMicroarea> findByMembro_UuidAndFimIsNull(UUID membroId);

    List<AtribuicaoMicroarea> findByMembro_UuidInOrderByInicioDesc(List<UUID> membroIds);

    List<AtribuicaoMicroarea> findByMicroarea_Area_UuidOrderByInicioDesc(UUID areaId);

    List<AtribuicaoMicroarea> findByMicroarea_Area_UuidAndFimIsNull(UUID areaId);
}
