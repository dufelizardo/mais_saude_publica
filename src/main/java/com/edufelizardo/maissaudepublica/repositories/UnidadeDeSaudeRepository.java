package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnidadeDeSaudeRepository extends JpaRepository<UnidadeDeSaude, UUID> {
    /**
     * Trava a linha da unidade até o fim da transação. Toda operação que pode criar um lote numa
     * unidade (entrada de lote, correção de lote, transferência) passa por aqui antes de procurar a
     * remessa, para duas operações simultâneas não criarem dois lotes da mesma remessa (ADR-0060).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UnidadeDeSaude u where u.uuid = :uuid")
    Optional<UnidadeDeSaude> findByIdParaMovimentarEstoque(@Param("uuid") UUID uuid);

    Optional<UnidadeDeSaude> findByNome(String nome);

    List<UnidadeDeSaude> findByTipo(TipoUnidadeDeSaude tipo);

    List<UnidadeDeSaude> findByNomeAndTipo(String nome, TipoUnidadeDeSaude tipo);

    List<UnidadeDeSaude> findByTipoIn(List<TipoUnidadeDeSaude> tipos);

    List<UnidadeDeSaude> findByNomeAndTipoIn(String nome, List<TipoUnidadeDeSaude> tipos);

    List<UnidadeDeSaude> findByResponsavelCpfAndResponsavelIsNull(String responsavelCpf);
}
