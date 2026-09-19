package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Lotacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotacaoRepository extends JpaRepository<Lotacao, UUID> {
    Optional<Lotacao> findByProfissional_MatriculaAndDataFimIsNull(String matricula);

    List<Lotacao> findByProfissional_MatriculaOrderByDataInicioDesc(String matricula);
}
