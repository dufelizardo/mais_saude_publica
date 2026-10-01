package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Lotacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotacaoRepository extends JpaRepository<Lotacao, UUID> {
    Optional<Lotacao> findByProfissional_MatriculaAndDataFimIsNull(String matricula);

    List<Lotacao> findByProfissional_MatriculaOrderByDataInicioDesc(String matricula);

    /** Lotações vigentes de todo o quadro, com unidade e cargo — uma consulta para a tela Profissionais (ADR-0072). */
    @Query("select l from Lotacao l join fetch l.unidade join fetch l.cargo where l.dataFim is null")
    List<Lotacao> findVigentesComUnidadeECargo();
}
