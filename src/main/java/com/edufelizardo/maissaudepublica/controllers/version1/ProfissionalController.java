package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalAtivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalContatoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProfissionalResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.ProfissionalService;
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
@RequestMapping(value = "/api/v1/profissional/")
@Tag(name = "Profissional", description = "Endpoints para Gerenciar Profissionais (RH) e seu vínculo com Unidades de Saúde.")
public class ProfissionalController {

    @Autowired
    private ProfissionalService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca os Profissionais cadastrados",
            description = "Verifica a existencia de Profissionais.",
            tags = "Profissional")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = ProfissionalResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "ProfissionalResponse",
                    value = ExampleConstants.PROFISSIONAL_RESPONSE_EXAMPLE,
                    description = "O servidor consegue processar a requisição, e retorna no corpo da resposta as informações encontradas"))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ProfissionalResponseDto>> getAllProfissional() {
        List<ProfissionalResponseDto> responseDtos = service.getAll();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{cpf}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um Profissional pelo seu CPF.",
            description = "Verifica a existencia de um Profissional.",
            tags = "Profissional")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = ProfissionalResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "ProfissionalResponse",
                    value = ExampleConstants.PROFISSIONAL_RESPONSE_FIND_EXAMPLE,
                    description = "O servidor consegue processar a requisição, e retorna no corpo da resposta as informações encontradas."))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<ProfissionalResponseDto> findByCpf(@PathVariable String cpf) {
        ProfissionalResponseDto responseDto = service.findByCpf(cpf);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um Profissional.",
            description = "Cria um Profissional e reconcilia automaticamente o vínculo com Unidades de Saúde pendentes que já referenciavam este CPF.",
            tags = "Profissional")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE,
                    description = "Cria um Profissional."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> createProfissional(@Valid @RequestBody ProfissionalRequestDto dto) {
        try {
            ProfissionalResponseDto responseDto = service.create(dto);

            String successMessage = "Profissional criado com sucesso!";
            String details = "Nome: " + responseDto.getNome() + ", Matrícula: " + responseDto.getMatricula();

            SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

            return ResponseEntity.status(HttpStatus.CREATED).body(successResponseDto);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException("Não foi possível concluir o cadastro (conflito de dados).", e);
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

    @PatchMapping(value = "contato/{cpf}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza os contatos de um Profissional.",
            description = "Atualiza os contatos de um Profissional.",
            tags = "Profissional")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza um Profissional."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> updateContatoProfissional(@PathVariable String cpf, @Valid @RequestBody ProfissionalContatoRequestDto dto) {
        ProfissionalResponseDto responseDto = service.updateContato(cpf, dto);

        String successMessage = "Profissional atualizado com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @DeleteMapping(value = "des-habilitar/{cpf}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desabilita ou Habilita um Profissional.",
            description = "Desabilita ou Habilita um Profissional.",
            tags = "Profissional")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                    description = "Atualiza um Profissional."))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> deleteProfissional(@PathVariable String cpf, ProfissionalAtivoRequestDto dto) {
        ProfissionalResponseDto responseDto = service.desabilitar(cpf, dto);

        String successMessage = "Profissional atualizado com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }
}
