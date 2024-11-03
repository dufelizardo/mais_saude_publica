package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.HierarquicoDoisResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.HierarquicoUmResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.HierarquicoDoisService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/hierarquico-dois/")
@Tag(name = "Hierarquico 2", description = "Endpoints para Gerenciar Hierárquia Nível 2.")
public class UnidadeDeSaudeHierarquicoDoisController {
    @Autowired
    private HierarquicoDoisService service;

    @GetMapping
    public ResponseEntity<List<HierarquicoDoisResponseDto>> getAllHierarquicoDois () {
        List<HierarquicoDoisResponseDto> responseDtos = service.getAllHierarquiaDoisService();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{nome}")
    public ResponseEntity<HierarquicoDoisResponseDto> findByHierarquicoDois (@PathVariable String nome) {
        HierarquicoDoisResponseDto responseDto = service.findByNomeHierarquicoDoisService(nome);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping
    public ResponseEntity<SuccessResponseDto> createHierarquicoDois (@Valid @RequestBody HierarquicoDoisRequestDto dto) {
        try {
            HierarquicoDoisResponseDto responseDto = service.createHierarquicoDoisService(dto);
            String successMessage = "Unidade de Saúde criada com sucesso!";
            String details = "Nome: " + responseDto.getNome() + ", Tipo: " + responseDto.getTipo();

            SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

            return ResponseEntity.status(HttpStatus.CREATED).body(successResponseDto);
        } catch (DataIntegrityViolationException e){
            throw new ResourceBadRequestException("Já existe uma instituição registrada com este nome.", e);
        }catch (ResourceBadRequestException e) {
            throw new ResourceBadRequestException("Não foi possível efetivar o cadastro", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro interno ao processar a solicitação", e);
        }
    }

    @PatchMapping(value = "{nome}")
    public ResponseEntity<SuccessResponseDto> updateNomeHierarquiaDois (@PathVariable String nome,
                                                                     @Valid @RequestBody UnidadeDeSaudeNomeUpdateRequestDto dto) {
        HierarquicoDoisResponseDto responseDto = service.updateHierarquicoDoisNomeService(nome, dto);
        // Monta a mensagem de sucesso
        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        // Cria a resposta de sucesso
        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        // Retorna a resposta com status 200 OK
        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "contato/{nome}")
    public ResponseEntity<SuccessResponseDto> updateContatoHierarquiasDois(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeEnderecoRequestDto dto) {
        HierarquicoDoisResponseDto responseDto = service.updateHierarquicoDoisContatoService(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "horario-de-funcionamento/{nome}")
    public ResponseEntity<SuccessResponseDto> updateHoraDeFuncionamentoHierarquiasZero(@PathVariable String nome,
                                                                                       @Valid @RequestBody UnidadeDeSaudeHorarioDeFuncionamentoRequestDto dto) {
        // Chama o serviço para realizar a atualização
        HierarquicoDoisResponseDto responseDto = service.updateHierarquicoDoisHorarioFuncionamentoService(nome, dto);
        // Monta a mensagem de sucesso
        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        // Cria a resposta de sucesso
        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        // Retorna a resposta com status 200 OK
        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "horario-de-atendimento/{nome}")
    public ResponseEntity<SuccessResponseDto> updateHoraDeAtendimentoHierarquiasZero(@PathVariable String nome,
                                                                                     @Valid @RequestBody UnidadeDeSaudeHorarioDeAtendimentoRequestDto dto) {
        // Chama o serviço para realizar a atualização
        HierarquicoDoisResponseDto responseDto = service.updateHierarquicoDoisHorarioAtendimentoService(nome, dto);
        // Monta a mensagem de sucesso
        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        // Cria a resposta de sucesso
        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        // Retorna a resposta com status 200 OK
        return ResponseEntity.ok().body(successResponseDto);
    }

    @DeleteMapping(value = "des-habilitar/{nome}")
    public ResponseEntity<SuccessResponseDto> deleteHierarquiasZero(@PathVariable String nome, UnidadeDeSaudeAtivoRequestDto dto) {
        // Chama o serviço para realizar a atualização
        HierarquicoDoisResponseDto responseDto = service.deletHierarquicoDoisService(nome, dto);

        // Monta a mensagem de sucesso
        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        // Cria a resposta de sucesso
        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        // Retorna a resposta com status 200 OK
        return ResponseEntity.ok().body(successResponseDto);
    }
}
