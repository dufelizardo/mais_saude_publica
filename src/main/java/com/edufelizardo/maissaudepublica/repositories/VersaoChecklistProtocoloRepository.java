package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.VersaoChecklistProtocolo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VersaoChecklistProtocoloRepository extends JpaRepository<VersaoChecklistProtocolo, UUID> {

    Optional<VersaoChecklistProtocolo> findFirstByChecklist_UuidOrderByVersaoDesc(UUID checklistId);
}
