package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Veiculo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Veiculo v where v.uuid = :uuid")
    Optional<Veiculo> findByIdParaAtualizar(@Param("uuid") UUID uuid);

    @Query("select v from Veiculo v join fetch v.unidade order by v.prefixo")
    List<Veiculo> findTodos();

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndUuidNot(String placa, UUID uuid);

    boolean existsByPrefixoIgnoreCase(String prefixo);

    boolean existsByPrefixoIgnoreCaseAndUuidNot(String prefixo, UUID uuid);
}
