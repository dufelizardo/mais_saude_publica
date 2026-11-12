package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.MedidaSanitaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MedidaSanitariaRepository extends JpaRepository<MedidaSanitaria, UUID> {

    @Query("select m from MedidaSanitaria m join fetch m.estabelecimento left join fetch m.processo order by m.inicio desc, m.registradoEm desc")
    List<MedidaSanitaria> findTodas();

    List<MedidaSanitaria> findByEstabelecimento_Uuid(UUID estabelecimentoId);

    List<MedidaSanitaria> findByProcesso_UuidOrderByInicioAsc(UUID processoId);

    long countByNumeroStartingWith(String prefixo);
}
