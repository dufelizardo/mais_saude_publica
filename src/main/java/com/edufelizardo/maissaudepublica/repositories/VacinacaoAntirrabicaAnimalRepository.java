package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.VacinacaoAntirrabicaAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface VacinacaoAntirrabicaAnimalRepository extends JpaRepository<VacinacaoAntirrabicaAnimal, UUID> {

    @Query("select v from VacinacaoAntirrabicaAnimal v join fetch v.microarea m join fetch m.area where year(v.data) = ?1 order by v.data desc, v.registradoEm desc")
    List<VacinacaoAntirrabicaAnimal> findDoAno(int ano);
}
