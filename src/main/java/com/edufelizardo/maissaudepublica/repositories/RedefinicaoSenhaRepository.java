package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RedefinicaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RedefinicaoSenhaRepository extends JpaRepository<RedefinicaoSenha, UUID> {

    Optional<RedefinicaoSenha> findByTokenHash(String tokenHash);

    /** Pedidos do usuário a partir de um instante (limite por hora). */
    long countByUsuario_UuidAndCriadaEmAfter(UUID usuarioId, Instant desde);

    /** Links ainda não usados do usuário (para invalidar quando sai um novo ou a senha muda). */
    List<RedefinicaoSenha> findByUsuario_UuidAndUsadaEmIsNull(UUID usuarioId);

    List<RedefinicaoSenha> findByUsuario_Uuid(UUID usuarioId);
}
