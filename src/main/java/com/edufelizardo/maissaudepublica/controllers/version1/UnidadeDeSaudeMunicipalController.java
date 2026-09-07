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
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MunicipalResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.MunicipalService;
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
@RequestMapping(value = "/api/v1/municipal/")
@Tag(name = "Municipal", description = "Endpoints para Gerenciar a Esfera Municipal de Saúde.")
public class UnidadeDeSaudeMunicipalController {

    @Autowired
    private MunicipalService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma Instituição da Esfera Municipal de Saúde",
            description = "Verifica a existencia de Instituções da Esfera Municipal.",
            tags = "Municipal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = MunicipalResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "MunicipalResponse",
                    value = ExampleConstants.MUNICIPAL_RESPONSE_EXAMPLE,
                    description = "O servidor consegue processar a requisição, e retorna no corpo da resposta as informações encontradas"))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<MunicipalResponseDto>> getAllMunicipal() {
        List<MunicipalResponseDto> responseDtos = service.getAll();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{nome}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma Instituição da Esfera Municipal de Saúde pelo seu Nome.",
            description = "Verifica a existencia de Instituções da Esfera Municipal.",
            tags = "Municipal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = MunicipalResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "MunicipalResponse",
                    value = ExampleConstants.MUNICIPAL_RESPONSE_FIND_EXAMPLE,
                    description = "O servidor consegue processar a requisição, e retorna no corpo da resposta as informações encontradas."))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<MunicipalResponseDto> findByMunicipal(@PathVariable String nome) {
        MunicipalResponseDto responseDto = service.findByNome(nome);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma Instituição da Esfera Municipal de Saúde.",
            description = "Cria uma Instituição da Esfera Municipal.",
            tags = "Municipal")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE,
                    description = "Cria uma Unidade de Saúde da Esfera Municipal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> createMunicipal(@Valid @RequestBody MunicipalRequestDto dto) {
        try {
            MunicipalResponseDto responseDto = service.create(dto);

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
    @Operation(summary = "Atualiza o nome de uma Instituição da Esfera Municipal de Saúde.",
            description = "Atualiza o nome de uma Instituição da Esfera Municipal.",
            tags = "Municipal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde da Esfera Municipal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateNomeMunicipal(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeNomeUpdateRequestDto dto) {
        MunicipalResponseDto responseDto = service.updateNome(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "contato/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza os contatos de uma Instituição da Esfera Municipal de Saúde.",
            description = "Atualiza os contatos de uma Instituição da Esfera Municipal.",
            tags = "Municipal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde da Esfera Municipal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateContatoMunicipal(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeEnderecoRequestDto dto) {
        MunicipalResponseDto responseDto = service.updateContato(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "horario-de-funcionamento/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o Horariode de funcionamento de uma Instituição da Esfera Municipal de Saúde.",
            description = "Atualiza o Horariode de funcionamento de uma Instituição da Esfera Municipal.",
            tags = "Municipal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde da Esfera Municipal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateHoraDeFuncionamentoMunicipal(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeHorarioDeFuncionamentoRequestDto dto) {
        MunicipalResponseDto responseDto = service.updateHorarioFuncionamento(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "horario-de-atendimento/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o Horario de atendimento de uma Instituição da Esfera Municipal de Saúde.",
            description = "Atualiza o Horario de atendimento de uma Instituição da Esfera Municipal.",
            tags = "Municipal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde da Esfera Municipal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateHoraDeAtendimentoMunicipal(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeHorarioDeAtendimentoRequestDto dto) {
        MunicipalResponseDto responseDto = service.updateHorarioAtendimento(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @DeleteMapping(value = "des-habilitar/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desabilita ou Habilita uma Instituição da Esfera Municipal de Saúde.",
            description = "Desabilita ou Habilita uma Instituição da Esfera Municipal.",
            tags = "Municipal")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza uma Unidade de Saúde da Esfera Municipal."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> deleteMunicipal(@PathVariable String nome, UnidadeDeSaudeAtivoRequestDto dto) {
        MunicipalResponseDto responseDto = service.desabilitar(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }
}
