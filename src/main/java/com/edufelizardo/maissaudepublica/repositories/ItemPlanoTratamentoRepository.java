package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ItemPlanoTratamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemPlanoTratamentoRepository extends JpaRepository<ItemPlanoTratamento, UUID> {
    @Query("select i from ItemPlanoTratamento i join fetch i.procedimentoPrevisto where i.plano.uuid in ?1 order by i.ordem asc, i.criadoEm asc")
    List<ItemPlanoTratamento> findDosPlanos(Collection<UUID> planoIds);

    List<ItemPlanoTratamento> findByPlano_Uuid(UUID planoId);

    /** O item que o procedimento executa, se houver. */
    Optional<ItemPlanoTratamento> findByProcedimento_Uuid(UUID procedimentoId);
}
