package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PacienteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PacienteResponseDto;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD do Paciente (Assistência, primeira fatia da onda pós-Fundação — ver ADR-0039,
 * MAPA-DE-DOMINIOS.md #5). Sem geração de matrícula (o CNS já é um identificador externo
 * pré-existente, ao contrário da matrícula do Profissional) e sem reconciliação com outro
 * domínio — Paciente ainda não é referenciado por FK direta em nenhuma entidade implementada.
 */
@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    public PacienteResponseDto criar(PacienteRequestDto dto) {
        Paciente paciente = new Paciente(dto);
        paciente = pacienteRepository.save(paciente);
        return PacienteResponseDto.fromPaciente(paciente);
    }

    public PacienteResponseDto atualizar(UUID uuid, PacienteRequestDto dto) {
        Paciente paciente = buscarEntidadePorId(uuid);
        paciente.setNome(dto.getNome());
        paciente.setCpf(dto.getCpf());
        paciente.setCartaoSus(dto.getCartaoSus());
        paciente.setDataNascimento(dto.getDataNascimento());
        paciente.setSexo(dto.getSexo());
        paciente.setEndereco(new Endereco(dto.getEndereco()));
        paciente.setTelefones(dto.getTelefones());
        paciente.setEmail(dto.getEmail());
        paciente.setAtivo(dto.getAtivo());
        paciente = pacienteRepository.save(paciente);
        return PacienteResponseDto.fromPaciente(paciente);
    }

    public List<PacienteResponseDto> listar() {
        return pacienteRepository.findAll()
                .stream()
                .map(PacienteResponseDto::fromPaciente)
                .collect(Collectors.toList());
    }

    public PacienteResponseDto buscarPorId(UUID uuid) {
        return PacienteResponseDto.fromPaciente(buscarEntidadePorId(uuid));
    }

    public List<PacienteResponseDto> buscarPorCpf(String cpf) {
        List<PacienteResponseDto> encontrados = pacienteRepository.findByCpf(cpf)
                .stream()
                .map(PacienteResponseDto::fromPaciente)
                .collect(Collectors.toList());
        if (encontrados.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar um paciente com o CPF " + cpf + " em nossos registros.");
        }
        return encontrados;
    }

    public List<PacienteResponseDto> buscarPorCartaoSus(String cartaoSus) {
        List<PacienteResponseDto> encontrados = pacienteRepository.findByCartaoSus(cartaoSus)
                .stream()
                .map(PacienteResponseDto::fromPaciente)
                .collect(Collectors.toList());
        if (encontrados.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar um paciente com o cartão SUS " + cartaoSus + " em nossos registros.");
        }
        return encontrados;
    }

    private Paciente buscarEntidadePorId(UUID uuid) {
        return pacienteRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um paciente com o id " + uuid + " em nossos registros."));
    }
}
