package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalAtivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalContatoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProfissionalResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD do Profissional (RH), com reconciliação síncrona do vínculo fraco por CPF com
 * UnidadeDeSaude — ver ADR-0014. Não estende {@link AbstractHierarquicoService}, que é
 * específico do domínio de UnidadeDeSaude.
 */
@Service
public class ProfissionalService {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    public List<ProfissionalResponseDto> getAll() {
        return profissionalRepository.findAll()
                .stream()
                .map(ProfissionalResponseDto::fromProfissional)
                .collect(Collectors.toList());
    }

    public ProfissionalResponseDto findByCpf(String cpf) {
        return ProfissionalResponseDto.fromProfissional(buscarProfissionalPorCpf(cpf));
    }

    @Transactional
    public ProfissionalResponseDto create(ProfissionalRequestDto dto) {
        Profissional profissional = profissionalRepository.save(new Profissional(dto));
        reconciliarUnidadesPendentes(profissional);
        return ProfissionalResponseDto.fromProfissional(profissional);
    }

    @Transactional
    public ProfissionalResponseDto updateContato(String cpf, ProfissionalContatoRequestDto dto) {
        Profissional profissional = buscarProfissionalPorCpf(cpf);
        profissional.setTelefone(dto.getTelefone());
        profissional.setEmail(dto.getEmail());
        profissional = profissionalRepository.save(profissional);
        return ProfissionalResponseDto.fromProfissional(profissional);
    }

    @Transactional
    public ProfissionalResponseDto desabilitar(String cpf, ProfissionalAtivoRequestDto dto) {
        Profissional profissional = buscarProfissionalPorCpf(cpf);
        profissional.setAtivo(dto.isAtivo());
        profissional = profissionalRepository.save(profissional);
        return ProfissionalResponseDto.fromProfissional(profissional);
    }

    /**
     * Vincula este Profissional recém-criado a toda UnidadeDeSaude que já tinha o CPF dele
     * informado como {@code responsavelCpf}, mas ainda sem {@code responsavel} resolvido —
     * metade "profissional chega depois" da reconciliação (ver ADR-0014).
     */
    private void reconciliarUnidadesPendentes(Profissional profissional) {
        List<UnidadeDeSaude> unidadesPendentes =
                unidadeDeSaudeRepository.findByResponsavelCpfAndResponsavelIsNull(profissional.getCpf());
        unidadesPendentes.forEach(unidade -> unidade.setResponsavel(profissional));
        unidadeDeSaudeRepository.saveAll(unidadesPendentes);
    }

    private Profissional buscarProfissionalPorCpf(String cpf) {
        return profissionalRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com o CPF " + cpf + " em nossos registros."));
    }
}
