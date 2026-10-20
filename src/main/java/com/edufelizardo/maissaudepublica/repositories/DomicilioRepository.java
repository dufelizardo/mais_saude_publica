package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Domicilio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DomicilioRepository extends JpaRepository<Domicilio, UUID> {
    List<Domicilio> findByImovel_UuidOrderByIdentificacaoAsc(UUID imovelId);

    List<Domicilio> findByImovel_UuidIn(Collection<UUID> imovelIds);

    boolean existsByImovel_UuidAndIdentificacaoIgnoreCase(UUID imovelId, String identificacao);
}
