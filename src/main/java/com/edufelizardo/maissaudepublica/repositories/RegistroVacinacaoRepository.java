package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RegistroVacinacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RegistroVacinacaoRepository extends JpaRepository<RegistroVacinacao, UUID> {

    boolean existsByRetificacaoDe_Uuid(UUID registroId);

    /** As doses vigentes do paciente (nem anuladas, nem retificadas), na ordem da aplicação (ADR-0147). */
    @Query("select r from RegistroVacinacao r join fetch r.imunobiologico where r.paciente.uuid = ?1 and r.anulada = false "
            + "and not exists (select 1 from RegistroVacinacao s where s.retificacaoDe = r) order by r.aplicadaEm")
    List<RegistroVacinacao> findVigentesDoPaciente(UUID pacienteId);

    /** As doses vigentes dos pacientes, para a lista de atrasados da unidade. */
    @Query("select r from RegistroVacinacao r join fetch r.imunobiologico where r.paciente.uuid in ?1 and r.anulada = false "
            + "and not exists (select 1 from RegistroVacinacao s where s.retificacaoDe = r)")
    List<RegistroVacinacao> findVigentesDosPacientes(Collection<UUID> pacienteIds);

    /** As aplicadas vigentes da unidade no intervalo (a sala de vacina do dia). */
    @Query("select r from RegistroVacinacao r join fetch r.imunobiologico join fetch r.paciente where r.unidade.uuid = ?1 "
            + "and r.aplicadaEm >= ?2 and r.aplicadaEm < ?3 "
            + "and r.origem = com.edufelizardo.maissaudepublica.models.enuns.OrigemRegistroVacinacao.APLICADA and r.anulada = false "
            + "and not exists (select 1 from RegistroVacinacao s where s.retificacaoDe = r) order by r.aplicadaEm")
    List<RegistroVacinacao> findAplicadasDaUnidade(UUID unidadeId, LocalDateTime de, LocalDateTime ate);

    /** As aplicadas vigentes da rede no intervalo (a vacinação na Sala de Situação, ADR-0149); o escopo filtra depois. */
    @Query("select r from RegistroVacinacao r join fetch r.imunobiologico join fetch r.unidade where r.aplicadaEm >= ?1 and r.aplicadaEm < ?2 "
            + "and r.origem = com.edufelizardo.maissaudepublica.models.enuns.OrigemRegistroVacinacao.APLICADA and r.anulada = false "
            + "and not exists (select 1 from RegistroVacinacao s where s.retificacaoDe = r)")
    List<RegistroVacinacao> findAplicadasEntre(LocalDateTime de, LocalDateTime ate);

    /** As pessoas vacinadas na campanha (doses vigentes com a campanha), o andamento da meta (ADR-0149). */
    @Query("select count(distinct r.paciente.uuid) from RegistroVacinacao r where r.campanha.uuid = ?1 and r.anulada = false "
            + "and not exists (select 1 from RegistroVacinacao s where s.retificacaoDe = r)")
    long countPessoasDaCampanha(UUID campanhaId);
}
