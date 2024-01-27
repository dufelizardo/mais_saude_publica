package com.edufelizardo.API_Mais_Saude_Publica.repositories;

import com.edufelizardo.API_Mais_Saude_Publica.model.primeiro_departamento_operacoes.PrimeiroDepartOrganizacionalTGM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface PrimeiroDepartOrganizacionalTGMRepository extends JpaRepository<PrimeiroDepartOrganizacionalTGM, UUID> {
    Optional<PrimeiroDepartOrganizacionalTGM> findByNome(String nome);
}
