package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.datautilexception.ErrorExcepitionResponse;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.HierarquicoTresResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.HierarquicoTresService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping(value = "/api/v1/hierarquico-tres/")
@Tag(name = "Hierarquico 3", description = "Endpoints para Gerenciar Hierárquia Nível 3.")
public class UnidadeDeSaudeHierarquicoTresController {
    @Autowired
    private HierarquicoTresService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma Instituição Hierárquica Nível Tres de Saúde",
            description = "Verifica a existencia de Instituções Hierárquica Nível 3.",
            tags = "Hierarquico 3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = HierarquicoTresResponseDto.class)
                    ), examples = @ExampleObject(name = "Success",
                            summary = "HierarquicoUmResponse",
                            value = ExampleConstants.HIERARQUICO_TRES_RESPONSE_EXAMPLE,
                            description = "O servidor consegue processar a requisição, e retorna no corpo da resposta as informações encontradas"))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Bad Request:",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_400,
                            description = "O servidor não consegue processar a requisição devido a um erro no cliente, como parâmetros inválidos ou malformados."))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unauthorized",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_401,
                            description = "O cliente não está autenticado. Normalmente ocorre quando a requisição GET exige que o usuário esteja autenticado, mas ele não forneceu ou forneceu credenciais inválidas."))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Forbidden",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_403,
                            description = " O cliente está autenticado, mas não tem permissão para acessar o recurso solicitado."))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Not Found",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_404,
                            description = "O recurso solicitado não foi encontrado no servidor."))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Internal Server Error",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_500,
                            description = "Ocorre quando há um erro no servidor que impede o processamento da requisição."))
            }),
            @ApiResponse(responseCode = "504", description = "Gateway Timeout:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Gateway Timeout",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_504,
                            description = "O servidor que atua como gateway ou proxy não recebeu uma resposta a tempo de outro servidor upstream."))
            })
    })
    public ResponseEntity<List<HierarquicoTresResponseDto>> getAllHierarquicoTres () {
        List<HierarquicoTresResponseDto> responseDtos = service.getAllHierarquiaTresService();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping(value = "{nome}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma Instituição Hierárquica Nível Tres de Saúde pelo seu Nome.",
            description = "Verifica a existencia de Instituções Hierárquica Nível 3.",
            tags = "Hierarquico 3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = HierarquicoTresResponseDto.class)
                    ), examples = @ExampleObject(name = "Success",
                            summary = "HierarquicoZeroResponse",
                            value = ExampleConstants.HIERARQUICO_TRES_RESPONSE_FIND_EXAMPLE,
                            description = "O servidor consegue processar a requisição, e retorna no corpo da resposta as informações encontradas."))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Bad Request",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_400,
                            description = "Se o ID fornecido na URL estiver malformado ou for inválido."))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unauthorized",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_401,
                            description = "O cliente não está autenticado, mas o recurso exige autenticação."))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Forbidden",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_403,
                            description = "O cliente não tem permissão para acessar o recurso solicitado."))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Not Found",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_404,
                            description = "O recurso com o ID fornecido não foi encontrado no banco de dados."))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Internal Server Error",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_500,
                            description = "Ocorre um erro no servidor ao processar a requisição, como um problema de conexão com o banco de dados ou uma exceção inesperada."))
            })
    })
    public ResponseEntity<HierarquicoTresResponseDto> findByHierarquicoTres (@PathVariable String nome) {
        HierarquicoTresResponseDto responseDto = service.findByNomeHierarquicoTresService(nome);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma Instituição Hierárquica Nível Tres de Saúde.",
            description = "Cria uma Instituções Hierárquica Nível 3.",
            tags = "Hierarquico 3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = SuccessResponseDto.class)
                    ), examples = @ExampleObject(name = "Success",
                            summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE,
                            description = "Cria uma Unidade de Saúde Nível 0."))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Bad Request",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_400,
                            description = "Se os dados enviados no corpo da requisição forem malformados ou violarem as regras de validação."))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unauthorized",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_401,
                            description = "O cliente não está autenticado, mas a criação do recurso exige autenticação."))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Forbidden",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_403,
                            description = "O cliente está autenticado, mas não tem permissão para criar o recurso."))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Not Found",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_404,
                            description = "Se o recurso relacionado à criação não for encontrado, como um ID de relacionamento inexistente passado no payload."))
            }),
            @ApiResponse(responseCode = "409", description = "Conflict:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Conflict",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_409,
                            description = "Pode ocorrer quando a criação entra em conflito com o estado atual do recurso, como tentar criar um recurso com um nome ou ID já existente, se houver uma restrição de unicidade."))
            }),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unprocessable Entity",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_422,
                            description = "Se os dados no corpo da requisição são semanticamente inválidos (por exemplo, regras de validação não atendidas)."))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Internal Server Error",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_500,
                            description = "Um erro inesperado no servidor ao processar a criação do recurso."))
            })
    })
    public ResponseEntity<SuccessResponseDto> createHierarquicoTres (@Valid @RequestBody HierarquicoTresRequestDto dto) {
        try {
            HierarquicoTresResponseDto responseDto = service.createHierarquicoTresService(dto);
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

    @PatchMapping(value = "{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o nome de uma Instituição Hierárquica Nível Tres de Saúde.",
            description = "Atualiza o nome de uma Instituções Hierárquica Nível 3.",
            tags = "Hierarquico 3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = SuccessResponseDto.class)
                    ), examples = @ExampleObject(name = "Success",
                            summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                            description = "Atualiza uma Unidade de Saúde Nível 0."))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Bad Request",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_400,
                            description = "Se os dados enviados no corpo da requisição forem malformados ou violarem as regras de validação."))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unauthorized",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_401,
                            description = "O cliente não está autenticado, mas a criação do recurso exige autenticação."))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Forbidden",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_403,
                            description = "O cliente está autenticado, mas não tem permissão para criar o recurso."))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Not Found",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_404,
                            description = "Se o recurso relacionado à criação não for encontrado, como um ID de relacionamento inexistente passado no payload."))
            }),
            @ApiResponse(responseCode = "409", description = "Conflict:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Conflict",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_409,
                            description = "Pode ocorrer quando a criação entra em conflito com o estado atual do recurso, como tentar criar um recurso com um nome ou ID já existente, se houver uma restrição de unicidade."))
            }),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unprocessable Entity",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_422,
                            description = "Se os dados no corpo da requisição são semanticamente inválidos (por exemplo, regras de validação não atendidas)."))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Internal Server Error",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_500,
                            description = "Um erro inesperado no servidor ao processar a criação do recurso."))
            })
    })
    public ResponseEntity<SuccessResponseDto> updateNomeHierarquiaTres (@PathVariable String nome,
                                                                     @Valid @RequestBody UnidadeDeSaudeNomeUpdateRequestDto dto) {
        HierarquicoTresResponseDto responseDto = service.updateHierarquicoTresNomeService(nome, dto);
        // Monta a mensagem de sucesso
        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        // Cria a resposta de sucesso
        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        // Retorna a resposta com status 200 OK
        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "contato/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza os contatos de uma Instituição Hierárquica Nível Tres de Saúde.",
            description = "Atualiza os contatos de uma Instituções Hierárquica Nível 3",
            tags = "Hierarquico 3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = SuccessResponseDto.class)
                    ), examples = @ExampleObject(name = "Success",
                            summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                            description = "Atualiza uma Unidade de Saúde Nível 0."))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Bad Request",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_400,
                            description = "Se os dados enviados no corpo da requisição forem malformados ou violarem as regras de validação."))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unauthorized",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_401,
                            description = "O cliente não está autenticado, mas a criação do recurso exige autenticação."))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Forbidden",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_403,
                            description = "O cliente está autenticado, mas não tem permissão para criar o recurso."))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Not Found",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_404,
                            description = "Se o recurso relacionado à criação não for encontrado, como um ID de relacionamento inexistente passado no payload."))
            }),
            @ApiResponse(responseCode = "409", description = "Conflict:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Conflict",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_409,
                            description = "Pode ocorrer quando a criação entra em conflito com o estado atual do recurso, como tentar criar um recurso com um nome ou ID já existente, se houver uma restrição de unicidade."))
            }),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unprocessable Entity",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_422,
                            description = "Se os dados no corpo da requisição são semanticamente inválidos (por exemplo, regras de validação não atendidas)."))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Internal Server Error",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_500,
                            description = "Um erro inesperado no servidor ao processar a criação do recurso."))
            })
    })
    public ResponseEntity<SuccessResponseDto> updateContatoHierarquiasTres(@PathVariable String nome, @Valid @RequestBody UnidadeDeSaudeEnderecoRequestDto dto) {
        HierarquicoTresResponseDto responseDto = service.updateHierarquicoTresContatoService(nome, dto);

        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "horario-de-funcionamento/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o Horariode de funcionamento de uma Instituição Hierárquica Nível Tres de Saúde.",
            description = "Atualiza o Horariode de funcionamento de uma Instituções Hierárquica Nível 3.",
            tags = "Hierarquico 3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = SuccessResponseDto.class)
                    ), examples = @ExampleObject(name = "Success",
                            summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                            description = "Atualiza uma Unidade de Saúde Nível 0."))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Bad Request",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_400,
                            description = "Se os dados enviados no corpo da requisição forem malformados ou violarem as regras de validação."))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unauthorized",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_401,
                            description = "O cliente não está autenticado, mas a criação do recurso exige autenticação."))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Forbidden",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_403,
                            description = "O cliente está autenticado, mas não tem permissão para criar o recurso."))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Not Found",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_404,
                            description = "Se o recurso relacionado à criação não for encontrado, como um ID de relacionamento inexistente passado no payload."))
            }),
            @ApiResponse(responseCode = "409", description = "Conflict:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Conflict",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_409,
                            description = "Pode ocorrer quando a criação entra em conflito com o estado atual do recurso, como tentar criar um recurso com um nome ou ID já existente, se houver uma restrição de unicidade."))
            }),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unprocessable Entity",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_422,
                            description = "Se os dados no corpo da requisição são semanticamente inválidos (por exemplo, regras de validação não atendidas)."))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Internal Server Error",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_500,
                            description = "Um erro inesperado no servidor ao processar a criação do recurso."))
            })
    })
    public ResponseEntity<SuccessResponseDto> updateHoraDeFuncionamentoHierarquiasZero(@PathVariable String nome,
                                                                                       @Valid @RequestBody UnidadeDeSaudeHorarioDeFuncionamentoRequestDto dto) {
        // Chama o serviço para realizar a atualização
        HierarquicoTresResponseDto responseDto = service.updateHierarquicoTresHorarioFuncionamentoService(nome, dto);
        // Monta a mensagem de sucesso
        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        // Cria a resposta de sucesso
        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        // Retorna a resposta com status 200 OK
        return ResponseEntity.ok().body(successResponseDto);
    }

    @PatchMapping(value = "horario-de-atendimento/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o Horario de atendimento de uma Instituição Hierárquica Nível Tres de Saúde.",
            description = "Atualiza o Horario de atendimento de uma Instituções Hierárquica Nível 3.",
            tags = "Hierarquico 3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = SuccessResponseDto.class)
                    ), examples = @ExampleObject(name = "Success",
                            summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                            description = "Atualiza uma Unidade de Saúde Nível 0."))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Bad Request",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_400,
                            description = "Se os dados enviados no corpo da requisição forem malformados ou violarem as regras de validação."))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unauthorized",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_401,
                            description = "O cliente não está autenticado, mas a criação do recurso exige autenticação."))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Forbidden",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_403,
                            description = "O cliente está autenticado, mas não tem permissão para criar o recurso."))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Not Found",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_404,
                            description = "Se o recurso relacionado à criação não for encontrado, como um ID de relacionamento inexistente passado no payload."))
            }),
            @ApiResponse(responseCode = "409", description = "Conflict:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Conflict",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_409,
                            description = "Pode ocorrer quando a criação entra em conflito com o estado atual do recurso, como tentar criar um recurso com um nome ou ID já existente, se houver uma restrição de unicidade."))
            }),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unprocessable Entity",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_422,
                            description = "Se os dados no corpo da requisição são semanticamente inválidos (por exemplo, regras de validação não atendidas)."))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Internal Server Error",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_500,
                            description = "Um erro inesperado no servidor ao processar a criação do recurso."))
            })
    })
    public ResponseEntity<SuccessResponseDto> updateHoraDeAtendimentoHierarquiasZero(@PathVariable String nome,
                                                                                     @Valid @RequestBody UnidadeDeSaudeHorarioDeAtendimentoRequestDto dto) {
        // Chama o serviço para realizar a atualização
        HierarquicoTresResponseDto responseDto = service.updateHierarquicoTresHorarioAtendimentoService(nome, dto);
        // Monta a mensagem de sucesso
        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        // Cria a resposta de sucesso
        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        // Retorna a resposta com status 200 OK
        return ResponseEntity.ok().body(successResponseDto);
    }

    @DeleteMapping(value = "des-habilitar/{nome}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desabilita ou Habilita uma Instituição Hierárquica Nível Tres de Saúde.",
            description = "Desabilita ou Habilita  uma Instituções Hierárquica Nível 3.",
            tags = "Hierarquico 3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = SuccessResponseDto.class)
                    ), examples = @ExampleObject(name = "Success",
                            summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE,
                            description = "Atualiza uma Unidade de Saúde Nível 0."))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Bad Request",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_400,
                            description = "Se os dados enviados no corpo da requisição forem malformados ou violarem as regras de validação."))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unauthorized",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_401,
                            description = "O cliente não está autenticado, mas a criação do recurso exige autenticação."))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Forbidden",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_403,
                            description = "O cliente está autenticado, mas não tem permissão para criar o recurso."))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Not Found",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_404,
                            description = "Se o recurso relacionado à criação não for encontrado, como um ID de relacionamento inexistente passado no payload."))
            }),
            @ApiResponse(responseCode = "409", description = "Conflict:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Conflict",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_409,
                            description = "Pode ocorrer quando a criação entra em conflito com o estado atual do recurso, como tentar criar um recurso com um nome ou ID já existente, se houver uma restrição de unicidade."))
            }),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Unprocessable Entity",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_422,
                            description = "Se os dados no corpo da requisição são semanticamente inválidos (por exemplo, regras de validação não atendidas)."))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error:", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = ErrorExcepitionResponse.class)
                    ), examples = @ExampleObject(name = "Internal Server Error",
                            summary = "ErrorExceptionResponse",
                            value = ExampleConstants.ERROR_EXAMPLE_500,
                            description = "Um erro inesperado no servidor ao processar a criação do recurso."))
            })
    })
    public ResponseEntity<SuccessResponseDto> deleteHierarquiasZero(@PathVariable String nome, UnidadeDeSaudeAtivoRequestDto dto) {
        // Chama o serviço para realizar a atualização
        HierarquicoTresResponseDto responseDto = service.desableHierarquicoTresService(nome, dto);

        // Monta a mensagem de sucesso
        String successMessage = "Unidade de Saúde atualizada com sucesso!";
        String details = "Nome: " + responseDto.getNome();

        // Cria a resposta de sucesso
        SuccessResponseDto successResponseDto = new SuccessResponseDto(successMessage, details);

        // Retorna a resposta com status 200 OK
        return ResponseEntity.ok().body(successResponseDto);
    }
}
