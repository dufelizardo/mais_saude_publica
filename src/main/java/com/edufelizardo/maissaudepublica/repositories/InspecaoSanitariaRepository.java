package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.InspecaoSanitaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InspecaoSanitariaRepository extends JpaRepository<InspecaoSanitaria, UUID> {

    @Query("select i from InspecaoSanitaria i join fetch i.estabelecimento left join fetch i.denuncia left join fetch i.roteiroVersao v left join fetch v.roteiro order by i.dataInspecao desc, i.registradoEm desc")
    List<InspecaoSanitaria> findTodas();

    List<InspecaoSanitaria> findByEstabelecimento_UuidOrderByDataInspecaoDescRegistradoEmDesc(UUID estabelecimentoId);

    List<InspecaoSanitaria> findByDenuncia_Uuid(UUID denunciaId);
}
