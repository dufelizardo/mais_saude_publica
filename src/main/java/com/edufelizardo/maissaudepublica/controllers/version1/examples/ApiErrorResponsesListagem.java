package com.edufelizardo.maissaudepublica.controllers.version1.examples;

import com.edufelizardo.maissaudepublica.exceptions.datautilexception.ErrorExcepitionResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Respostas de erro comuns aos endpoints de listagem (GET sem filtro por nome),
 * repetidas hoje de forma idêntica nos 4 controllers de hierarquia.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "400", description = "Bad Request", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Bad Request:",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_400,
                description = "O servidor não consegue processar a requisição devido a um erro no cliente, como parâmetros inválidos ou malformados."))
})
@ApiResponse(responseCode = "401", description = "Unauthorized", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Unauthorized",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_401,
                description = "O cliente não está autenticado. Normalmente ocorre quando a requisição GET exige que o usuário esteja autenticado, mas ele não forneceu ou forneceu credenciais inválidas."))
})
@ApiResponse(responseCode = "403", description = "Forbidden:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Forbidden",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_403,
                description = "O cliente está autenticado, mas não tem permissão para acessar o recurso solicitado."))
})
@ApiResponse(responseCode = "404", description = "Not Found:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Not Found",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_404,
                description = "O recurso solicitado não foi encontrado no servidor."))
})
@ApiResponse(responseCode = "500", description = "Internal Server Error:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Internal Server Error",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_500,
                description = "Ocorre quando há um erro no servidor que impede o processamento da requisição."))
})
@ApiResponse(responseCode = "504", description = "Gateway Timeout:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Gateway Timeout",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_504,
                description = "O servidor que atua como gateway ou proxy não recebeu uma resposta a tempo de outro servidor upstream."))
})
public @interface ApiErrorResponsesListagem {
}
