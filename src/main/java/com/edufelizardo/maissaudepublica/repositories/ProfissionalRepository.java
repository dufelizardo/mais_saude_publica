package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfissionalRepository extends JpaRepository<Profissional, UUID> {
    /**
     * Busca a ficha ATIVA com este CPF (ver ADR-0017) — CPF não é mais único, então uma pessoa
     * recontratada pode ter fichas antigas desligadas com o mesmo CPF; esta consulta ignora essas.
     */
    Optional<Profissional> findByCpfAndAtivoTrue(String cpf);

    /**
     * Todas as fichas com este CPF, ativas ou não — usado só por
     * ProfissionalService.buscarProfissionalParaAlterarStatus, que precisa achar uma ficha
     * inativa pra poder reabilitá-la (findByCpfAndAtivoTrue não serviria nesse caso).
     */
    List<Profissional> findByCpf(String cpf);

    Optional<Profissional> findByMatricula(String matricula);

    boolean existsByMatricula(String matricula);
}
