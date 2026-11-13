package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ChecklistProtocolo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChecklistProtocoloRepository extends JpaRepository<ChecklistProtocolo, UUID> {

    Optional<ChecklistProtocolo> findByCodigo(String codigo);

    List<ChecklistProtocolo> findAllByOrderByNomeAsc();
}
