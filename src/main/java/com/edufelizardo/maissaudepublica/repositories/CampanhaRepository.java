package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Campanha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CampanhaRepository extends JpaRepository<Campanha, UUID> {
    @Query("select c from Campanha c left join fetch c.programa order by c.inicio desc, c.nome asc")
    List<Campanha> findTodas();
}
