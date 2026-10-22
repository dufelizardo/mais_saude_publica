package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ComunicacaoMinisterioPublico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.edufelizardo.maissaudepublica.models.enuns.MomentoComunicacaoMp;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ComunicacaoMinisterioPublicoRepository extends JpaRepository<ComunicacaoMinisterioPublico, UUID> {
    @Query("select c from ComunicacaoMinisterioPublico c left join fetch c.responsavel where c.internacao.uuid in ?1 order by c.comunicadaEm")
    List<ComunicacaoMinisterioPublico> findDasInternacoes(Collection<UUID> internacaoIds);

    boolean existsByInternacao_UuidAndMomento(UUID internacaoId, MomentoComunicacaoMp momento);
}
