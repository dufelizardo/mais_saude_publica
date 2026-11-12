package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.IrregularidadeSanitaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IrregularidadeSanitariaRepository extends JpaRepository<IrregularidadeSanitaria, UUID> {

    @Query("select r from IrregularidadeSanitaria r join fetch r.estabelecimento join fetch r.inspecao order by r.registradoEm")
    List<IrregularidadeSanitaria> findTodas();

    List<IrregularidadeSanitaria> findByEstabelecimento_UuidOrderByRegistradoEmAsc(UUID estabelecimentoId);

    List<IrregularidadeSanitaria> findByInspecao_UuidOrderByRegistradoEmAsc(UUID inspecaoId);
}
