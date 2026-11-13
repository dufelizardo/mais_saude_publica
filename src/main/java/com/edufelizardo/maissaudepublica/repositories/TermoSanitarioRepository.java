package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TermoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TermoSanitarioRepository extends JpaRepository<TermoSanitario, UUID> {

    Optional<TermoSanitario> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    @Query("select t from TermoSanitario t join fetch t.documento where t.processo.uuid = ?1 order by t.emitidoEm")
    List<TermoSanitario> findDoProcesso(UUID processoId);

    @Query("select t from TermoSanitario t join fetch t.documento where t.medida.uuid = ?1 order by t.emitidoEm")
    List<TermoSanitario> findDaMedida(UUID medidaId);
}
