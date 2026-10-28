package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PermanenciaAcolhimentoNoturno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermanenciaAcolhimentoNoturnoRepository extends JpaRepository<PermanenciaAcolhimentoNoturno, UUID> {

    @Query("select p from PermanenciaAcolhimentoNoturno p join fetch p.paciente where p.unidade.uuid = ?1 and p.saida is null order by p.entrada")
    List<PermanenciaAcolhimentoNoturno> findAbertasDaUnidade(UUID unidadeId);

    @Query("select p from PermanenciaAcolhimentoNoturno p join fetch p.paciente where p.unidade.uuid = ?1 and p.saida >= ?2 order by p.saida desc")
    List<PermanenciaAcolhimentoNoturno> findSaidasDaUnidadeDesde(UUID unidadeId, LocalDate desde);

    @Query("select p from PermanenciaAcolhimentoNoturno p join fetch p.unidade where p.paciente.uuid = ?1 order by p.entrada desc")
    List<PermanenciaAcolhimentoNoturno> findDoPaciente(UUID pacienteId);

    boolean existsByPaciente_UuidAndSaidaIsNull(UUID pacienteId);

    long countByUnidade_UuidAndSaidaIsNull(UUID unidadeId);
}
