package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RiscoAmbiental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RiscoAmbientalRepository extends JpaRepository<RiscoAmbiental, UUID> {

    @Query("select r from RiscoAmbiental r left join fetch r.microarea m left join fetch m.area order by r.registradoEm desc")
    List<RiscoAmbiental> findTodos();
}
