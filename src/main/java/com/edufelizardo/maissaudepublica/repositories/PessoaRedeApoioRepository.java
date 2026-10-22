package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PessoaRedeApoio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PessoaRedeApoioRepository extends JpaRepository<PessoaRedeApoio, UUID> {
    @Query("select p from PessoaRedeApoio p where p.acompanhamento.uuid = ?1 order by p.inicio desc, p.nome")
    List<PessoaRedeApoio> findDoAcompanhamento(UUID acompanhamentoId);
}
