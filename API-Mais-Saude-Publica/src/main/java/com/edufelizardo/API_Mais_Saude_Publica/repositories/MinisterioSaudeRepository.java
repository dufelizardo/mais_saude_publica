package com.edufelizardo.API_Mais_Saude_Publica.repositories;

import com.edufelizardo.API_Mais_Saude_Publica.model.MinisterioSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface MinisterioSaudeRepository extends JpaRepository<MinisterioSaude, UUID> {
    Optional<MinisterioSaude> findByNomeInstitucional(String nomeInstitucional);
}
