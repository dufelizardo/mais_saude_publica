package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AtribuicaoAcessoRepository extends JpaRepository<AtribuicaoAcesso, UUID> {
    List<AtribuicaoAcesso> findByUsuarioUuid(UUID usuarioUuid);

    List<AtribuicaoAcesso> findByUsuarioCpf(String cpf);
}
