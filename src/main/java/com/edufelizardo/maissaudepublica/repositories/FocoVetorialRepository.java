package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.FocoVetorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FocoVetorialRepository extends JpaRepository<FocoVetorial, UUID> {

    @Query("select f from FocoVetorial f join fetch f.vistoria v join fetch v.imovel i join fetch i.microarea m join fetch m.area "
            + "order by v.data desc, v.registradoEm desc")
    List<FocoVetorial> findTodos();
}
