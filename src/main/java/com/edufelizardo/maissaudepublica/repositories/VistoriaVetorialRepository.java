package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.VistoriaVetorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface VistoriaVetorialRepository extends JpaRepository<VistoriaVetorial, UUID> {

    @Query("select distinct v from VistoriaVetorial v join fetch v.imovel i join fetch i.microarea m join fetch m.area join fetch v.agente "
            + "left join fetch v.focos where v.cicloAno = ?1 and v.cicloNumero = ?2 order by v.data desc, v.registradoEm desc")
    List<VistoriaVetorial> findDoCiclo(int ano, int ciclo);

    @Query("select distinct v from VistoriaVetorial v join fetch v.imovel i join fetch i.microarea m join fetch m.area join fetch v.agente "
            + "left join fetch v.focos where i.uuid = ?1 order by v.data desc, v.registradoEm desc")
    List<VistoriaVetorial> findDoImovel(UUID imovelId);

    List<VistoriaVetorial> findByAcao_Uuid(UUID acaoId);
}
