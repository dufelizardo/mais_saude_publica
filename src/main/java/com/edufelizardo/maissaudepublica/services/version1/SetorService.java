package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SetorRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SetorResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SetorService {

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public SetorResponseDto criar(SetorRequestDto dto) {
        UnidadeDeSaude unidade = buscarUnidadePorId(dto.getUnidadeId());
        Profissional responsavel = buscarResponsavelSeInformado(dto.getMatriculaResponsavel());

        Setor setor = new Setor(unidade, dto.getNome(), dto.getCodigo(), dto.getTipo(), dto.getAtivo(), responsavel);
        setor = setorRepository.save(setor);
        return SetorResponseDto.fromSetor(setor);
    }

    public SetorResponseDto atualizar(UUID uuid, SetorRequestDto dto) {
        Setor setor = buscarEntidadePorId(uuid);
        UnidadeDeSaude unidade = buscarUnidadePorId(dto.getUnidadeId());
        Profissional responsavel = buscarResponsavelSeInformado(dto.getMatriculaResponsavel());

        setor.setUnidade(unidade);
        setor.setNome(dto.getNome());
        setor.setCodigo(dto.getCodigo());
        setor.setTipo(dto.getTipo());
        setor.setAtivo(dto.getAtivo());
        setor.setResponsavel(responsavel);
        setor = setorRepository.save(setor);
        return SetorResponseDto.fromSetor(setor);
    }

    public List<SetorResponseDto> listar() {
        return setorRepository.findAll()
                .stream()
                .map(SetorResponseDto::fromSetor)
                .collect(Collectors.toList());
    }

    public SetorResponseDto buscarPorId(UUID uuid) {
        return SetorResponseDto.fromSetor(buscarEntidadePorId(uuid));
    }

    Setor buscarEntidadePorId(UUID uuid) {
        return setorRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um setor com o id " + uuid + " em nossos registros."));
    }

    private UnidadeDeSaude buscarUnidadePorId(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }

    /**
     * FK direta pra {@link Profissional} por matrícula (ver ADR-0034 e o mesmo padrão já usado por
     * {@code LotacaoService}) — sem vínculo fraco por CPF, diferente do padrão mais antigo da
     * ADR-0014, porque o RH já é estável o suficiente pra referências novas.
     */
    private Profissional buscarResponsavelSeInformado(String matriculaResponsavel) {
        if (matriculaResponsavel == null || matriculaResponsavel.isEmpty()) {
            return null;
        }
        return profissionalRepository.findByMatricula(matriculaResponsavel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matriculaResponsavel + " em nossos registros."));
    }
}
