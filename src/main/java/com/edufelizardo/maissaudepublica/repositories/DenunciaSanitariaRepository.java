package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.DenunciaSanitaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DenunciaSanitariaRepository extends JpaRepository<DenunciaSanitaria, UUID> {

    @Query("select d from DenunciaSanitaria d left join fetch d.estabelecimento order by d.recebidaEm desc")
    List<DenunciaSanitaria> findTodas();

    long countByProtocoloStartingWith(String prefixo);
}
