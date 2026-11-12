package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PenalidadeSanitaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PenalidadeSanitariaRepository extends JpaRepository<PenalidadeSanitaria, UUID> {

    List<PenalidadeSanitaria> findByProcesso_Uuid(UUID processoId);

    @Query("select p from PenalidadeSanitaria p join fetch p.processo")
    List<PenalidadeSanitaria> findTodas();
}
