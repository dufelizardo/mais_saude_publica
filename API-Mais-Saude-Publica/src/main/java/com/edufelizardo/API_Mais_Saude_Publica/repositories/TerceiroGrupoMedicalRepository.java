package com.edufelizardo.API_Mais_Saude_Publica.repositories;

import com.edufelizardo.API_Mais_Saude_Publica.model.TerceiroGrupoMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TerceiroGrupoMedicalRepository extends JpaRepository<TerceiroGrupoMedical, UUID> {
    Optional<TerceiroGrupoMedical> findByNome(String nome);
}
