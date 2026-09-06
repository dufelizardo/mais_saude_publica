package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeAtivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeEnderecoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeHorarioDeAtendimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeHorarioDeFuncionamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeNomeUpdateRequestDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reúne o CRUD comum aos 4 níveis hierárquicos de UnidadeDeSaude. Cada nível
 * só precisa informar seu {@link TipoUnidadeDeSaude} e como mapear a entidade
 * para o DTO de resposta correspondente.
 */
public abstract class AbstractHierarquicoService<RES> {

    @Autowired
    protected UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    protected abstract TipoUnidadeDeSaude getTipo();

    protected abstract RES toResponseDto(UnidadeDeSaude unidadeDeSaude);

    public List<RES> getAll() {
        return unidadeDeSaudeRepository.findByTipo(getTipo().getTipo())
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public RES findByNome(String nome) {
        return toResponseDto(buscarUnidadeDeSaudePorNome(nome));
    }

    @Transactional
    public RES updateNome(@Valid String nome, UnidadeDeSaudeNomeUpdateRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        unidadeDeSaude.setNome(dto.getNome());
        return salvar(unidadeDeSaude);
    }

    @Transactional
    public RES updateContato(@Valid String nome, UnidadeDeSaudeEnderecoRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        unidadeDeSaude.setEndereco(new Endereco(dto.getEndereco()));
        unidadeDeSaude.setSaudeTelefones(dto.getTelefones());
        unidadeDeSaude.setEmail(dto.getEmail());
        return salvar(unidadeDeSaude);
    }

    @Transactional
    public RES updateHorarioFuncionamento(@Valid String nome, UnidadeDeSaudeHorarioDeFuncionamentoRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        unidadeDeSaude.setHorarioFuncionamento(dto.getHorarioFuncionamento());
        return salvar(unidadeDeSaude);
    }

    @Transactional
    public RES updateHorarioAtendimento(@Valid String nome, UnidadeDeSaudeHorarioDeAtendimentoRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        unidadeDeSaude.setHorarioAtendimento(dto.getHorarioAtendimento());
        return salvar(unidadeDeSaude);
    }

    @Transactional
    public RES desabilitar(String nome, UnidadeDeSaudeAtivoRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        unidadeDeSaude.setAtivo(dto.isAtivo());
        return salvar(unidadeDeSaude);
    }

    /**
     * Vincula a unidade superior informada, quando o nível hierárquico admite uma
     * (Um, Dois e Três). O nível Zero, que não tem superior, simplesmente não chama este método.
     */
    protected void vincularSuperiorSeInformado(UnidadeDeSaude unidadeDeSaude, String nomeSuperior) {
        if (nomeSuperior != null && !nomeSuperior.isEmpty()) {
            UnidadeDeSaude superior = unidadeDeSaudeRepository.findByNome(nomeSuperior)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Unidade superior com o nome " + nomeSuperior + " não foi encontrada."));
            unidadeDeSaude.setUnidadeSuperior(superior);
        }
    }

    protected RES salvar(UnidadeDeSaude unidadeDeSaude) {
        try {
            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);
            return toResponseDto(unidadeDeSaude);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceBadRequestException("Já existe uma instituição registrada com este nome.", e);
        }
    }

    protected UnidadeDeSaude buscarUnidadeDeSaudePorNome(String nome) {
        return unidadeDeSaudeRepository.findByNomeAndTipo(nome, getTipo().getTipo())
                .stream()
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar " + nome + " em nossos registros."));
    }
}
