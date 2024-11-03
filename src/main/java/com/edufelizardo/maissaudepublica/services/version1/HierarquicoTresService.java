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
import java.util.Optional;
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
        UnidadeDeSaude unidadeDeSaude = unidadeDeSaudeRepository.findByNomeAndTipo(nome, TipoUnidadeDeSaude.ADMINISTRACAO4.getTipo())
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar " + nome + " em nossos registros."));
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
            Optional<UnidadeDeSaude> unidadeDeSaudeOpt = unidadeDeSaudeRepository.findByNome(nome);

            if (unidadeDeSaudeOpt.isEmpty()) {
                throw new ResourceBadRequestException("Instituição com nome '" + nome + "' não foi encontrada.");
            }

            UnidadeDeSaude unidadeDeSaude = unidadeDeSaudeOpt.get();
            unidadeDeSaude.setNome(dto.getNome());

            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);

            return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
        } catch (DataIntegrityViolationException e) {
            // Captura a exceção se o novo nome violar uma regra de integridade, como duplicidade
            throw new ResourceBadRequestException("Já existe uma instituição registrada com este nome.", e);
        } catch (ResourceBadRequestException e) {
            // Captura uma exceção customizada e lança a mesma mensagem
            throw new ResourceBadRequestException("Não foi possível efetivar a atualização.", e);
        } catch (Exception e) {
            // Lança uma exceção para erros inesperados
            throw new RuntimeException("Erro interno ao processar a solicitação", e);
        }
    }
    @Transactional
    public HierarquicoTresResponseDto updateHierarquicoTresContatoService (@Valid String nome, UnidadeDeSaudeEnderecoRequestDto dto) {
        try {
            // Procura a unidade de saúde pelo nome
            Optional<UnidadeDeSaude> unidadeDeSaudeOpt = unidadeDeSaudeRepository.findByNome(nome);

            // Verifica se o Optional está vazio
            if (unidadeDeSaudeOpt.isEmpty()) {
                throw new ResourceNotFoundException("Instituição com nome '" + nome + "' não foi encontrada.");
            }

            // Atualiza o nome da unidade de saúde
            UnidadeDeSaude unidadeDeSaude = unidadeDeSaudeOpt.get();
            unidadeDeSaude.setEndereco(new Endereco(dto.getEndereco()));
            unidadeDeSaude.setSaudeTelefones(dto.getTelefones());
            unidadeDeSaude.setEmail(dto.getEmail());

            // Salva a unidade atualizada no banco de dados
            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);

            // Retorna o DTO de resposta
            return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
        } catch (DataIntegrityViolationException e) {
            // Captura a exceção se o novo nome violar uma regra de integridade, como duplicidade
            throw new ResourceBadRequestException("Já existe uma instituição registrada com este nome.", e);
        } catch (ResourceBadRequestException e) {
            // Captura uma exceção customizada e lança a mesma mensagem
            throw new ResourceBadRequestException("Não foi possível efetivar a atualização.", e);
        } catch (Exception e) {
            // Lança uma exceção para erros inesperados
            throw new RuntimeException("Erro interno ao processar a solicitação", e);
        }
    }

    @Transactional
    public HierarquicoTresResponseDto updateHierarquicoTresHorarioFuncionamentoService (@Valid String nome, UnidadeDeSaudeHorarioDeFuncionamentoRequestDto dto) {
        try {
            // Procura a unidade de saúde pelo nome
            Optional<UnidadeDeSaude> unidadeDeSaudeOpt = unidadeDeSaudeRepository.findByNome(nome);

            // Verifica se o Optional está vazio
            if (unidadeDeSaudeOpt.isEmpty()) {
                throw new ResourceNotFoundException("Instituição com nome '" + nome + "' não foi encontrada.");
            }

            // Atualiza o nome da unidade de saúde
            UnidadeDeSaude unidadeDeSaude = unidadeDeSaudeOpt.get();
            unidadeDeSaude.setHorarioFuncionamento(dto.getHorarioFuncionamento());

            // Salva a unidade atualizada no banco de dados
            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);

            // Retorna o DTO de resposta
            return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
        } catch (DataIntegrityViolationException e) {
            // Captura a exceção se o novo nome violar uma regra de integridade, como duplicidade
            throw new ResourceBadRequestException("Já existe uma instituição registrada com este nome.", e);
        } catch (ResourceBadRequestException e) {
            // Captura uma exceção customizada e lança a mesma mensagem
            throw new ResourceBadRequestException("Não foi possível efetivar a atualização.", e);
        } catch (Exception e) {
            // Lança uma exceção para erros inesperados
            throw new RuntimeException("Erro interno ao processar a solicitação", e);
        }
    }

    @Transactional
    public HierarquicoTresResponseDto updateHierarquicoTresHorarioAtendimentoService (@Valid String nome, UnidadeDeSaudeHorarioDeAtendimentoRequestDto dto) {
        try {
            // Procura a unidade de saúde pelo nome
            Optional<UnidadeDeSaude> unidadeDeSaudeOpt = unidadeDeSaudeRepository.findByNome(nome);

            // Verifica se o Optional está vazio
            if (unidadeDeSaudeOpt.isEmpty()) {
                throw new ResourceNotFoundException("Instituição com nome '" + nome + "' não foi encontrada.");
            }

            // Atualiza o nome da unidade de saúde
            UnidadeDeSaude unidadeDeSaude = unidadeDeSaudeOpt.get();
            unidadeDeSaude.setHorarioAtendimento(dto.getHorarioAtendimento());

            // Salva a unidade atualizada no banco de dados
            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);

            // Retorna o DTO de resposta
            return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
        } catch (DataIntegrityViolationException e) {
            // Captura a exceção se o novo nome violar uma regra de integridade, como duplicidade
            throw new ResourceBadRequestException("Já existe uma instituição registrada com este nome.", e);
        } catch (ResourceBadRequestException e) {
            // Captura uma exceção customizada e lança a mesma mensagem
            throw new ResourceBadRequestException("Não foi possível efetivar a atualização.", e);
        } catch (Exception e) {
            // Lança uma exceção para erros inesperados
            throw new RuntimeException("Erro interno ao processar a solicitação", e);
        }
    }

    @Transactional
    public HierarquicoTresResponseDto deletHierarquicoTresService(String nome, UnidadeDeSaudeAtivoRequestDto dto) {
        try {
            // Procura a unidade de saúde pelo nome
            Optional<UnidadeDeSaude> unidadeDeSaudeOpt = unidadeDeSaudeRepository.findByNome(nome);

            // Verifica se o Optional está vazio
            if (unidadeDeSaudeOpt.isEmpty()) {
                throw new ResourceNotFoundException("Instituição com nome '" + nome + "' não foi encontrada.");
            }

            // Atualiza o nome da unidade de saúde
            UnidadeDeSaude unidadeDeSaude = unidadeDeSaudeOpt.get();
            unidadeDeSaude.setAtivo(dto.isAtivo());

            // Salva a unidade atualizada no banco de dados
            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);

            // Retorna o DTO de resposta
            return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);

        } catch (DataIntegrityViolationException e) {
            // Captura a exceção se o novo nome violar uma regra de integridade, como duplicidade
            throw new ResourceBadRequestException("Já existe uma instituição registrada com este nome.", e);
        } catch (ResourceBadRequestException e) {
            // Captura uma exceção customizada e lança a mesma mensagem
            throw new ResourceBadRequestException("Não foi possível efetivar a atualização.", e);
        } catch (Exception e) {
            // Lança uma exceção para erros inesperados
            throw new RuntimeException("Erro interno ao processar a solicitação", e);
        }
    }
}
