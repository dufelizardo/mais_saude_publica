package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CessaoVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CessaoVeiculoRepository extends JpaRepository<CessaoVeiculo, UUID> {

    @Query("select c from CessaoVeiculo c join fetch c.veiculo v join fetch v.unidade where c.mobilizacao.uuid in ?1 order by c.cedidaEm")
    List<CessaoVeiculo> findDasMobilizacoes(Collection<UUID> mobilizacaoIds);

    @Query("select c from CessaoVeiculo c join fetch c.mobilizacao m join fetch m.emergencia where c.veiculo.uuid = ?1 and c.devolvidaEm is null")
    Optional<CessaoVeiculo> findAbertaDoVeiculo(UUID veiculoId);

    @Query("select c from CessaoVeiculo c join fetch c.mobilizacao m join fetch m.emergencia join fetch c.veiculo where c.devolvidaEm is null")
    List<CessaoVeiculo> findAbertas();
}
