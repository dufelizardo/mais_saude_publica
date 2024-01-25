package com.edufelizardo.API_Mais_Saude_Publica.repositories;

import com.edufelizardo.API_Mais_Saude_Publica.model.PrimeiroGrupoMedical;
import com.edufelizardo.API_Mais_Saude_Publica.model.SegundoGrupoMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface SegundoGrupoMedicalRepository extends JpaRepository<SegundoGrupoMedical, UUID> {
    Optional<SegundoGrupoMedical> findByNome(String nome);
}
