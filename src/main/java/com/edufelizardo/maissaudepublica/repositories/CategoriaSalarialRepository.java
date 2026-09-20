package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoriaSalarialRepository extends JpaRepository<CategoriaSalarial, UUID> {
}
