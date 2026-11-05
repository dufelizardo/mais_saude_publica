package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AnimalZoonoses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AnimalZoonosesRepository extends JpaRepository<AnimalZoonoses, UUID> {

    @Query("select a from AnimalZoonoses a left join fetch a.microarea m left join fetch m.area order by a.registradoEm desc")
    List<AnimalZoonoses> findTodos();
}
