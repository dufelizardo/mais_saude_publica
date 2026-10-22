package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EtapaTratamentoProtetico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EtapaTratamentoProteticoRepository extends JpaRepository<EtapaTratamentoProtetico, UUID> {
    @Query("select e from EtapaTratamentoProtetico e join fetch e.profissional where e.tratamento.uuid in ?1 order by e.data asc, e.registradoEm asc")
    List<EtapaTratamentoProtetico> findDosTratamentos(Collection<UUID> tratamentoIds);
}
