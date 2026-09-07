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
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FederalResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.FederalService;
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
@RequestMapping(value = "/api/v1/federal/")
@Tag(name = "Federal", description = "Endpoints para Gerenciar a Esfera Federal de Saúde.")
public class UnidadeDeSaudeFederalController {

    @Autowired
    private FederalService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma Instituição da Esfera Federal de Saúde",
            description = "Verifica a existencia de Instituções da Esfera Federal.",
            tags = "Federal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = FederalResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "FederalResponse",
                    value = ExampleConstants.FEDERAL_RESPONSE_EXAMPLE,
                    description = "O servidor consegue processar a requisição, e retorna no corpo da resposta as informações encontradas"))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<FederalResponseDto>> getAllFederal() {
        List<FederalResponseDto> responseDtos = service.getAll();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{nome}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma Instituição da Esfera Federal de Saúde pelo seu Nome.",
            description = "Verifica a existencia de Instituções da Esfera Federal.",
            tags = "Federal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = FederalResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "FederalResponse",
                    value = ExampleConstants.FEDERAL_RESPONSE_FIND_EXAMPLE,
                    description = "O servidor consegue processar a requisição, e retorna no corpo da resposta as informações encontradas."))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<FederalResponseDto> findByFederal(@PathVariable String nome) {
        FederalResponseDto responseDto = service.findByNome(nome);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma Instituição da Esfera Federal de Saúde.",
            description = "Cria uma Instituição da Esfera Federal.",
            tags = "Federal")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE,
                    description = "Cria uma Unidade de Saúde da Esfera Federal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> createFederal(@Valid @RequestBody FederalRequestDto dto) {
        try {
            FederalResponseDto responseDto = service.create(dto);

            String successMessage = "Unidade de Saúde criada com sucesso!";
            String details = "Nome: " + responseDto.getNome() + ", Tipo: " + responseDto.getTipo();

            SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

            return ResponseEntity.status(HttpStatus.CREATED).body(successResponseDto);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException("Já existe uma instituição registrada com este nome.", e);
        } catch (ResourceUnprocessableEntityException e) {
            throw e;
        } catch (ResourceBadRequestException e) {
            throw new ResourceBadRequestException("Não foi possível efetivar o cadastro", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro interno ao processar a solicitação", e);
        }
    }

    @PatchMapping(value = "{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o nome de uma Instituição da Esfera Federal de Saúde.",
            description = "Atualiza o nome de uma Instituição da Esfera Federal.",
            tags = "Federal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde da Esfera Federal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateNomeFederal(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeNomeUpdateRequestDto dto) {
        FederalResponseDto responseDto = service.updateNome(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "contato/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza os contatos de uma Instituição da Esfera Federal de Saúde.",
            description = "Atualiza os contatos de uma Instituição da Esfera Federal.",
            tags = "Federal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde da Esfera Federal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateContatoFederal(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeEnderecoRequestDto dto) {
        FederalResponseDto responseDto = service.updateContato(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "horario-de-funcionamento/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o Horariode de funcionamento de uma Instituição da Esfera Federal de Saúde.",
            description = "Atualiza o Horariode de funcionamento de uma Instituição da Esfera Federal.",
            tags = "Federal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde da Esfera Federal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateHoraDeFuncionamentoFederal(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeHorarioDeFuncionamentoRequestDto dto) {
        FederalResponseDto responseDto = service.updateHorarioFuncionamento(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "horario-de-atendimento/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o Horario de atendimento de uma Instituição da Esfera Federal de Saúde.",
            description = "Atualiza o Horario de atendimento de uma Instituição da Esfera Federal.",
            tags = "Federal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde da Esfera Federal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateHoraDeAtendimentoFederal(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeHorarioDeAtendimentoRequestDto dto) {
        FederalResponseDto responseDto = service.updateHorarioAtendimento(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @DeleteMapping(value = "des-habilitar/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desabilita ou Habilita uma Instituição da Esfera Federal de Saúde.",
            description = "Desabilita ou Habilita uma Instituição da Esfera Federal.",
            tags = "Federal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde da Esfera Federal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> deleteFederal(@PathVariable String nome, UnidadeDeSaudeAtivoRequestDto dto) {
        FederalResponseDto responseDto = service.desabilitar(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }
}
