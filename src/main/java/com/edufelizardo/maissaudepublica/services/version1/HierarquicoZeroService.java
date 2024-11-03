package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.HierarquicoZeroResponseDto;
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
public class HierarquicoZeroService {

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;


    public List<HierarquicoZeroResponseDto> getAllHierarquiasZeroService() {
        return unidadeDeSaudeRepository.findByTipo(TipoUnidadeDeSaude.ADMINISTRACAO1.getTipo())
                .stream()
                .map(HierarquicoZeroResponseDto::fromHierarquicoResponseDto)
                .collect(Collectors.toList());
    }

    public HierarquicoZeroResponseDto findByNomeHierarquicoZeroService(String nome) {
        UnidadeDeSaude unidadeDeSaude = unidadeDeSaudeRepository.findByNomeAndTipo(nome, TipoUnidadeDeSaude.ADMINISTRACAO1.getTipo())
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar " + nome + " em nossos registros."));
        return HierarquicoZeroResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public HierarquicoZeroResponseDto createHierarquicoZeroService(HierarquicoZeroRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = new UnidadeDeSaude(dto);
        unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);
        return HierarquicoZeroResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public HierarquicoZeroResponseDto updateHierarquicoZeroNomeService(@Valid String nome, UnidadeDeSaudeNomeUpdateRequestDto dto) {
        // Procura a unidade de saúde pelo nome
        Optional<UnidadeDeSaude> unidadeDeSaudeOpt = unidadeDeSaudeRepository.findByNome(nome);

        // Verifica se o Optional está vazio
        if (unidadeDeSaudeOpt.isEmpty()) {
            throw new ResourceNotFoundException("Instituição com nome '" + nome + "' não foi encontrada.");
        }

        // Atualiza o nome da unidade de saúde
        UnidadeDeSaude unidadeDeSaude = unidadeDeSaudeOpt.get();
        unidadeDeSaude.setNome(dto.getNome());

        try {

            // Salva a unidade atualizada no banco de dados
            unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);

            // Retorna o DTO de resposta
            return HierarquicoZeroResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);

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
    public HierarquicoZeroResponseDto updateHierarquicoZeroContatoService(@Valid String nome, UnidadeDeSaudeEnderecoRequestDto dto) {
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
            return HierarquicoZeroResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);

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
    public HierarquicoZeroResponseDto updateHierarquicoZeroHorarioFuncionamentoService(@Valid String nome, UnidadeDeSaudeHorarioDeFuncionamentoRequestDto dto) {
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
            return HierarquicoZeroResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);

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
    public HierarquicoZeroResponseDto updateHierarquicoZeroHorarioAtendimentoService(@Valid String nome, UnidadeDeSaudeHorarioDeAtendimentoRequestDto dto) {
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
            return HierarquicoZeroResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);

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

    public HierarquicoZeroResponseDto deletHierarquicoZeroService(String nome, UnidadeDeSaudeAtivoRequestDto dto) {
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
            return HierarquicoZeroResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);

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
