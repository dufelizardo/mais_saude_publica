package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Surto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SurtoRepository extends JpaRepository<Surto, UUID> {
    @Query("select s from Surto s join fetch s.agravo join fetch s.unidade order by s.abertoEm desc")
    List<Surto> findTodos();
}
