package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ProcessoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProcessoSanitarioRepository extends JpaRepository<ProcessoSanitario, UUID> {

    @Query("select p from ProcessoSanitario p join fetch p.estabelecimento order by p.lavradoEm desc")
    List<ProcessoSanitario> findTodos();

    long countByNumeroStartingWith(String prefixo);
}
