package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EstabelecimentoRegulado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstabelecimentoReguladoRepository extends JpaRepository<EstabelecimentoRegulado, UUID> {

    Optional<EstabelecimentoRegulado> findByDocumento(String documento);

    @Query("select distinct e from EstabelecimentoRegulado e left join fetch e.atividades left join fetch e.unidade left join fetch e.microarea m left join fetch m.area order by e.razaoSocial")
    List<EstabelecimentoRegulado> findTodos();
}
