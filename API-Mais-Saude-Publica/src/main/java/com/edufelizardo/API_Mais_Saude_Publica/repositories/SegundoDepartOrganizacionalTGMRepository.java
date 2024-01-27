package com.edufelizardo.API_Mais_Saude_Publica.repositories;

import com.edufelizardo.API_Mais_Saude_Publica.model.segundo_departamento_operacoes.SegundoDepartOrganizacionalTGM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface SegundoDepartOrganizacionalTGMRepository extends JpaRepository<SegundoDepartOrganizacionalTGM, UUID> {
    Optional<SegundoDepartOrganizacionalTGM> findByNome(String nome);
}
