package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Publicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PublicacaoRepository extends JpaRepository<Publicacao, UUID> {

    @Query("select p from Publicacao p join fetch p.canal where p.comunicado.uuid = ?1 order by p.publicadaEm desc")
    List<Publicacao> findDoComunicado(UUID comunicadoId);

    @Query("select p from Publicacao p join fetch p.canal where p.conteudo.uuid = ?1 order by p.publicadaEm desc")
    List<Publicacao> findDoConteudo(UUID conteudoId);
}
