package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ResponsavelBem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResponsavelBemRepository extends JpaRepository<ResponsavelBem, UUID> {

    @Query("select r from ResponsavelBem r join fetch r.profissional where r.bem.uuid in :bens order by r.desde desc, r.registradoEm desc")
    List<ResponsavelBem> findDosBens(@Param("bens") Collection<UUID> bens);

    @Query("select r from ResponsavelBem r join fetch r.profissional where r.ate is null and r.bem.uuid in :bens")
    List<ResponsavelBem> findAtuais(@Param("bens") Collection<UUID> bens);

    @Query("select r.bem.uuid from ResponsavelBem r where r.ate is null and r.profissional.cpf = :cpf")
    List<UUID> findBensAtuaisDoCpf(@Param("cpf") String cpf);
}
