package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Documento;
import com.edufelizardo.maissaudepublica.models.enuns.DonoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DocumentoRepository extends JpaRepository<Documento, UUID> {

    @Query("select d from Documento d join fetch d.tipo join fetch d.unidade where d.donoTipo = ?1 and d.donoId = ?2 order by d.dataDocumento desc, d.registradoEm desc")
    List<Documento> findDoDono(DonoDocumento tipo, UUID donoId);
}
