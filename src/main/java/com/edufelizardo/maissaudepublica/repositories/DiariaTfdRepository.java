package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.DiariaTfd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DiariaTfdRepository extends JpaRepository<DiariaTfd, UUID> {

    @Query("select d from DiariaTfd d join fetch d.solicitacao s join fetch s.paciente join fetch d.credor order by d.concedidaEm desc, d.registradoEm desc")
    List<DiariaTfd> findTodas();

    boolean existsBySolicitacao_UuidAndBeneficiario(UUID solicitacaoId, com.edufelizardo.maissaudepublica.models.enuns.BeneficiarioDiariaTfd beneficiario);
}
