package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ExameAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ExameAnimalRepository extends JpaRepository<ExameAnimal, UUID> {

    @Query("select e from ExameAnimal e join fetch e.animal order by e.dataColeta desc, e.registradoEm desc")
    List<ExameAnimal> findTodos();
}
