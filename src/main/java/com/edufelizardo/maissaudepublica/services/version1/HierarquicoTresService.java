package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.HierarquicoTresResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HierarquicoTresService {
    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    public List<HierarquicoTresResponseDto> getAllHierarquiaTresService() {
        return unidadeDeSaudeRepository.findByTipo(TipoUnidadeDeSaude.ADMINISTRACAO4.getTipo())
                .stream()
                .map(HierarquicoTresResponseDto::fromHierarquicoResponseDto)
                .collect(Collectors.toList());
    }

    public HierarquicoTresResponseDto findByNomeHierarquicoTresService (String nome) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public HierarquicoTresResponseDto createHierarquicoTresService (HierarquicoTresRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = new UnidadeDeSaude(dto);
        if (dto.getAdministracaoSuperior() != null && !dto.getAdministracaoSuperior().isEmpty()){
            UnidadeDeSaude saude = unidadeDeSaudeRepository.findByNome(dto.getAdministracaoSuperior())
                    .orElseThrow(() -> new ResourceNotFoundException("Unidade superior com o nome " + dto.getAdministracaoSuperior() + " não foi encontrada."));

            unidadeDeSaude.setUnidadeSuperior(saude);
        }
        unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);
        return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }
    @Transactional
    public HierarquicoTresResponseDto updateHierarquicoTresNomeService (@Valid String nome, UnidadeDeSaudeNomeUpdateRequestDto dto) {
        try {
            UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);

            if (unidadeDeSaude.getNome().isEmpty()) {
                throw new ResourceBadRequestException("Instituição com nome '" + nome + "' não foi encontrada.");
            }

            unidadeDeSaude.setNome(dto.getNome());

            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);

            return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
        } catch (Exception e) {
            tratarExcecao(e);
            return null;
        }
    }
    @Transactional
    public HierarquicoTresResponseDto updateHierarquicoTresContatoService (@Valid String nome, UnidadeDeSaudeEnderecoRequestDto dto) {
        try {
            // Procura a unidade de saúde pelo nome
            UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);

            // Verifica se o Optional está vazio
            if (unidadeDeSaude.getNome().isEmpty()) {
                throw new ResourceNotFoundException("Instituição com nome '" + nome + "' não foi encontrada.");
            }

            // Atualiza o nome da unidade de saúde
            unidadeDeSaude.setEndereco(new Endereco(dto.getEndereco()));
            unidadeDeSaude.setSaudeTelefones(dto.getTelefones());
            unidadeDeSaude.setEmail(dto.getEmail());

            // Salva a unidade atualizada no banco de dados
            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);

            // Retorna o DTO de resposta
            return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
        } catch (Exception e) {
            tratarExcecao(e);
            return null;
        }
    }

    @Transactional
    public HierarquicoTresResponseDto updateHierarquicoTresHorarioFuncionamentoService (@Valid String nome, UnidadeDeSaudeHorarioDeFuncionamentoRequestDto dto) {
        try {
            // Procura a unidade de saúde pelo nome
            UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);

            // Verifica se o Optional está vazio
            if (unidadeDeSaude.getNome().isEmpty()) {
                throw new ResourceNotFoundException("Instituição com nome '" + nome + "' não foi encontrada.");
            }

            // Atualiza o nome da unidade de saúde
            unidadeDeSaude.setHorarioFuncionamento(dto.getHorarioFuncionamento());

            // Salva a unidade atualizada no banco de dados
            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);

            // Retorna o DTO de resposta
            return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
        } catch (Exception e) {
            tratarExcecao(e);
            return null;
        }
    }

    @Transactional
    public HierarquicoTresResponseDto updateHierarquicoTresHorarioAtendimentoService (@Valid String nome, UnidadeDeSaudeHorarioDeAtendimentoRequestDto dto) {
        try {
            // Procura a unidade de saúde pelo nome
            UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);

            // Verifica se o Optional está vazio
            if (unidadeDeSaude.getNome().isEmpty()) {
                throw new ResourceNotFoundException("Instituição com nome '" + nome + "' não foi encontrada.");
            }

            // Atualiza o nome da unidade de saúde
            unidadeDeSaude.setHorarioAtendimento(dto.getHorarioAtendimento());

            // Salva a unidade atualizada no banco de dados
            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);

            // Retorna o DTO de resposta
            return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
        } catch (Exception e) {
            tratarExcecao(e);
            return null;
        }
    }

    @Transactional
    public HierarquicoTresResponseDto desableHierarquicoTresService(String nome, UnidadeDeSaudeAtivoRequestDto dto) {
        try {
            // Procura a unidade de saúde pelo nome
            UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);

            // Verifica se o Optional está vazio
            if (unidadeDeSaude.getNome().isEmpty()) {
                throw new ResourceNotFoundException("Instituição com nome '" + nome + "' não foi encontrada.");
            }

            // Atualiza o nome da unidade de saúde
            unidadeDeSaude.setAtivo(dto.isAtivo());

            // Salva a unidade atualizada no banco de dados
            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);

            // Retorna o DTO de resposta
            return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);

        } catch (Exception e) {
            tratarExcecao(e);
            return null;
        }
    }

    private UnidadeDeSaude buscarUnidadeDeSaudePorNome(String nome) {
        return unidadeDeSaudeRepository.findByNomeAndTipo(nome, TipoUnidadeDeSaude.ADMINISTRACAO2.getTipo())
                .stream()
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar " + nome + " em nossos registros."));
    }

    private void tratarExcecao(Exception e) {
        if (e instanceof DataIntegrityViolationException) {
            throw new ResourceBadRequestException("Já existe uma instituição registrada com este nome.", e);
        } else if (e instanceof ResourceBadRequestException) {
            throw new ResourceBadRequestException("Não foi possível efetivar a atualização.", e);
        } else {
            throw new RuntimeException("Erro interno ao processar a solicitação", e);
        }
    }
}
