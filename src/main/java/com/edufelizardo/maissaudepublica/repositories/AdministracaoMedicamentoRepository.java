package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AdministracaoMedicamento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdministracaoMedicamentoRepository extends JpaRepository<AdministracaoMedicamento, UUID> {
    List<AdministracaoMedicamento> findByAtendimentoUuid(UUID atendimentoUuid);

    /** Trava a versão para retificar: impede duas retificações simultâneas (ADR-0062). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AdministracaoMedicamento a where a.uuid = :uuid")
    Optional<AdministracaoMedicamento> findByIdParaRetificar(@Param("uuid") UUID uuid);

    /** Pares (registro corrigido, versão que o corrige). */
    @Query("select r.retificacaoDe.uuid, r.uuid from AdministracaoMedicamento r where r.retificacaoDe is not null")
    List<Object[]> findParesDeRetificacao();

    /** Administrações vigentes (não retificadas) por atendimento — para o resumo da listagem. */
    @Query("select a.atendimento.uuid, count(a) from AdministracaoMedicamento a "
            + "where not exists (select r from AdministracaoMedicamento r where r.retificacaoDe = a) group by a.atendimento.uuid")
    List<Object[]> contarVigentesPorAtendimento();
}
