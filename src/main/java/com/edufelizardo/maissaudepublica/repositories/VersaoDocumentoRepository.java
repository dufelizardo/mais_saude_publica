package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.VersaoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VersaoDocumentoRepository extends JpaRepository<VersaoDocumento, UUID> {

    List<VersaoDocumento> findByDocumento_UuidOrderByNumeroDesc(UUID documentoId);
}
