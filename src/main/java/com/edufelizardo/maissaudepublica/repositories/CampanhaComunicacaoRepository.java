package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CampanhaComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanhaComunicacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampanhaComunicacaoRepository extends JpaRepository<CampanhaComunicacao, UUID> {

    List<CampanhaComunicacao> findAllByOrderByInicioDesc();

    List<CampanhaComunicacao> findBySituacaoOrderByInicioAsc(SituacaoCampanhaComunicacao situacao);
}
