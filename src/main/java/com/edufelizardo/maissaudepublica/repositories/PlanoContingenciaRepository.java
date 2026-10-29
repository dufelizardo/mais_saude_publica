package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PlanoContingencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PlanoContingenciaRepository extends JpaRepository<PlanoContingencia, UUID> {

    @Query("select p from PlanoContingencia p join fetch p.tipo order by p.nome, p.versao desc")
    List<PlanoContingencia> findTodos();

    @Query("select p from PlanoContingencia p where upper(p.codigo) = upper(?1) order by p.versao desc")
    List<PlanoContingencia> findVersoes(String codigo);
}
