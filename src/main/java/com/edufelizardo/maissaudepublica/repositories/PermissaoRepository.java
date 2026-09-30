package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissaoRepository extends JpaRepository<Permissao, UUID> {
    Optional<Permissao> findByCodigo(String codigo);

    List<Permissao> findByCodigoIn(Collection<String> codigos);
}
