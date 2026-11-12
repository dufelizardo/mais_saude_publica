package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.LicencaSanitaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface LicencaSanitariaRepository extends JpaRepository<LicencaSanitaria, UUID> {

    @Query("select l from LicencaSanitaria l join fetch l.estabelecimento order by l.emitidaEm desc, l.registradoEm desc")
    List<LicencaSanitaria> findTodas();

    List<LicencaSanitaria> findByEstabelecimento_UuidOrderByEmitidaEmDescRegistradoEmDesc(UUID estabelecimentoId);

    long countByNumeroStartingWith(String prefixo);
}
