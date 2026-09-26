package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MedicamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MedicamentoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD do Medicamento (Farmácia, primeira fatia do domínio — ver MAPA-DE-DOMINIOS.md #9,
 * ADR-0049). Sem FK — Medicamento é raiz do domínio, ainda não referenciado por nenhuma outra
 * entidade implementada.
 */
@Service
public class MedicamentoService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    public MedicamentoResponseDto criar(MedicamentoRequestDto dto) {
        Medicamento medicamento = new Medicamento(dto);
        medicamento = medicamentoRepository.save(medicamento);
        return MedicamentoResponseDto.fromMedicamento(medicamento);
    }

    public MedicamentoResponseDto atualizar(UUID uuid, MedicamentoRequestDto dto) {
        Medicamento medicamento = buscarEntidadePorId(uuid);
        medicamento.setNome(dto.getNome());
        medicamento.setPrincipioAtivo(dto.getPrincipioAtivo());
        medicamento.setApresentacao(dto.getApresentacao());
        medicamento.setCodigo(dto.getCodigo());
        medicamento.setAtivo(dto.getAtivo());
        medicamento = medicamentoRepository.save(medicamento);
        return MedicamentoResponseDto.fromMedicamento(medicamento);
    }

    public List<MedicamentoResponseDto> listar() {
        return medicamentoRepository.findAll()
                .stream()
                .map(MedicamentoResponseDto::fromMedicamento)
                .collect(Collectors.toList());
    }

    public MedicamentoResponseDto buscarPorId(UUID uuid) {
        return MedicamentoResponseDto.fromMedicamento(buscarEntidadePorId(uuid));
    }

    private Medicamento buscarEntidadePorId(UUID uuid) {
        return medicamentoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um medicamento com o id " + uuid + " em nossos registros."));
    }
}
