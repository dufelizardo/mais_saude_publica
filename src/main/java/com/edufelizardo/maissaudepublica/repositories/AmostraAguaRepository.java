package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AmostraAgua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AmostraAguaRepository extends JpaRepository<AmostraAgua, UUID> {

    @Query("select a from AmostraAgua a join fetch a.ponto order by a.dataColeta desc, a.registradoEm desc")
    List<AmostraAgua> findTodas();
}
