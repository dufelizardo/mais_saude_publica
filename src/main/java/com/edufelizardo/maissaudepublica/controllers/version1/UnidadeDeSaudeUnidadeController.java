package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.UnidadeSaudeResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.UnidadeSaudeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/unidade-saude/")
@Tag(name = "Unidade de Saúde", description = "Endpoints para Gerenciar Unidades de Saúde (UBS/Hospital).")
public class UnidadeDeSaudeUnidadeController {

    @Autowired
    private UnidadeSaudeService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca as Unidades de Saúde",
            description = "Verifica a existencia de Unidades de Saúde (UBS/Hospital).",
            tags = "Unidade de Saúde")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = UnidadeSaudeResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "UnidadeSaudeResponse",
                    value = ExampleConstants.UNIDADE_SAUDE_RESPONSE_EXAMPLE,
                    description = "O servidor consegue processar a requisição, e retorna no corpo da resposta as informações encontradas"))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<UnidadeSaudeResponseDto>> getAllUnidadeSaude() {
        List<UnidadeSaudeResponseDto> responseDtos = service.getAll();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{nome}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma Unidade de Saúde pelo seu Nome.",
            description = "Verifica a existencia de Unidades de Saúde (UBS/Hospital).",
            tags = "Unidade de Saúde")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = UnidadeSaudeResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "UnidadeSaudeResponse",
                    value = ExampleConstants.UNIDADE_SAUDE_RESPONSE_FIND_EXAMPLE,
                    description = "O servidor consegue processar a requisição, e retorna no corpo da resposta as informações encontradas."))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<UnidadeSaudeResponseDto> findByUnidadeSaude(@PathVariable String nome) {
        UnidadeSaudeResponseDto responseDto = service.findByNome(nome);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma Unidade de Saúde.",
            description = "Cria uma Unidade de Saúde (UBS/Hospital).",
            tags = "Unidade de Saúde")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE,
                    description = "Cria uma Unidade de Saúde."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> createUnidadeSaude(@Valid @RequestBody UnidadeSaudeRequestDto dto) {
        try {
            UnidadeSaudeResponseDto responseDto = service.create(dto);

            String successMessage = "Unidade de Saúde criada com sucesso!";
            String details = "Nome: " + responseDto.getNome() + ", Tipo: " + responseDto.getTipo();

            SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

            return ResponseEntity.status(HttpStatus.CREATED).body(successResponseDto);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException("Já existe uma instituição registrada com este nome.", e);
        } catch (ResourceBadRequestException e) {
            throw new ResourceBadRequestException("Não foi possível efetivar o cadastro", e);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (ResourceUnprocessableEntityException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro interno ao processar a solicitação", e);
        }
    }

    @PatchMapping(value = "{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o nome de uma Unidade de Saúde.",
            description = "Atualiza o nome de uma Unidade de Saúde.",
            tags = "Unidade de Saúde")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateNomeUnidadeSaude(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeNomeUpdateRequestDto dto) {
        UnidadeSaudeResponseDto responseDto = service.updateNome(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "contato/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza os contatos de uma Unidade de Saúde.",
            description = "Atualiza os contatos de uma Unidade de Saúde.",
            tags = "Unidade de Saúde")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateContatoUnidadeSaude(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeEnderecoRequestDto dto) {
        UnidadeSaudeResponseDto responseDto = service.updateContato(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "horario-de-funcionamento/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o Horario de funcionamento de uma Unidade de Saúde.",
            description = "Atualiza o Horario de funcionamento de uma Unidade de Saúde.",
            tags = "Unidade de Saúde")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateHoraDeFuncionamentoUnidadeSaude(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeHorarioDeFuncionamentoRequestDto dto) {
        UnidadeSaudeResponseDto responseDto = service.updateHorarioFuncionamento(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "horario-de-atendimento/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o Horario de atendimento de uma Unidade de Saúde.",
            description = "Atualiza o Horario de atendimento de uma Unidade de Saúde.",
            tags = "Unidade de Saúde")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateHoraDeAtendimentoUnidadeSaude(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeHorarioDeAtendimentoRequestDto dto) {
        UnidadeSaudeResponseDto responseDto = service.updateHorarioAtendimento(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "supervisao-regional/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza a supervisão regional de uma Unidade de Saúde.",
            description = "Vincula uma Unidade de Saúde a uma unidade REGIONAL de supervisão técnica (vínculo lateral, não-hierárquico).",
            tags = "Unidade de Saúde")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateSupervisaoRegionalUnidadeSaude(@PathVariable String nome, @Valid @RequestBody UnidadeSaudeSupervisaoRegionalRequestDto dto) {
        UnidadeSaudeResponseDto responseDto = service.updateSupervisaoRegional(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "responsavel/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o responsável de uma Unidade de Saúde.",
            description = "Informa o CPF do responsável pela Unidade de Saúde; o vínculo com o Profissional é resolvido na hora se ele já existir, ou depois por reconciliação (ver ADR-0014).",
            tags = "Unidade de Saúde")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateResponsavelUnidadeSaude(@PathVariable String nome, @Valid @RequestBody UnidadeSaudeResponsavelRequestDto dto) {
        UnidadeSaudeResponseDto responseDto = service.updateResponsavel(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @DeleteMapping(value = "des-habilitar/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desabilita ou Habilita uma Unidade de Saúde.",
            description = "Desabilita ou Habilita uma Unidade de Saúde.",
            tags = "Unidade de Saúde")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> deleteUnidadeSaude(@PathVariable String nome, UnidadeDeSaudeAtivoRequestDto dto) {
        UnidadeSaudeResponseDto responseDto = service.desabilitar(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }
}
