package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AtendimentoRepository extends JpaRepository<Atendimento, UUID> {
    /**
     * Todos os atendimentos de um paciente — usado pela agregação do Prontuário (ver ADR-0045).
     */
    List<Atendimento> findByPacienteUuid(UUID pacienteUuid);

    List<Atendimento> findByAgendamento_Uuid(UUID agendamentoId);

    /**
     * Último atendimento realizado (em andamento ou concluído) de cada paciente em cada unidade: [paciente, unidade,
     * dataHora]. O acompanhamento programático usa para apontar quem está atrasado (ADR-0119).
     */
    @Query("select a.paciente.uuid, a.unidade.uuid, max(a.dataHora) from Atendimento a "
            + "where a.status <> com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento.AGENDADO "
            + "and a.paciente.uuid in ?1 group by a.paciente.uuid, a.unidade.uuid")
    List<Object[]> findUltimosRealizados(Collection<UUID> pacienteIds);

    /** Paciente, unidade e data de cada atendimento realizado dos pacientes no intervalo (indicadores, ADR-0141). */
    @Query("select a.paciente.uuid, a.unidade.uuid, a.dataHora from Atendimento a "
            + "where a.status <> com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento.AGENDADO "
            + "and a.paciente.uuid in ?1 and a.dataHora >= ?2 and a.dataHora < ?3")
    List<Object[]> findRealizadosNoIntervalo(Collection<UUID> pacienteIds, java.time.LocalDateTime de, java.time.LocalDateTime ate);

    /**
     * Produção assistencial (ADR-0143): cada atendimento concluído no intervalo, só com o que se agrega
     * [unidade, dataHora, tipo, especialidade, conselho do profissional, nascimento e sexo do paciente]; nunca o paciente.
     */
    @Query("select a.unidade.uuid, a.dataHora, a.tipo, e.nome, pr.conselhoClasse, pa.dataNascimento, pa.sexo from Atendimento a "
            + "left join a.especialidade e left join a.profissional pr left join a.paciente pa "
            + "where a.status = com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento.CONCLUIDO "
            + "and a.unidade is not null and a.dataHora >= ?1 and a.dataHora < ?2")
    List<Object[]> findConcluidosParaProducao(java.time.LocalDateTime de, java.time.LocalDateTime ate);

    /** Situação operacional (ADR-0144): [atendimento, unidade, chegada] dos que chegaram no intervalo. */
    @Query("select a.uuid, a.unidade.uuid, a.chegadaEm from Atendimento a where a.chegadaEm >= ?1 and a.chegadaEm < ?2")
    List<Object[]> findComChegadaNoIntervalo(java.time.LocalDateTime de, java.time.LocalDateTime ate);

    /**
     * Situação agora (ADR-0144): [atendimento, unidade, chegada] dos que estão em andamento desde o instante; sem a
     * chegada registrada (anterior à BI2), vale a data e hora do atendimento.
     */
    @Query("select a.uuid, a.unidade.uuid, coalesce(a.chegadaEm, a.dataHora) from Atendimento a "
            + "where a.status = com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento.EM_ANDAMENTO "
            + "and coalesce(a.chegadaEm, a.dataHora) >= ?1")
    List<Object[]> findEmAndamentoDesde(java.time.LocalDateTime desde);
}
