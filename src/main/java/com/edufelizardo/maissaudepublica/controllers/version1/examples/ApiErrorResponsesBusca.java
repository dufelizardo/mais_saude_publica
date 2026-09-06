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
 * Respostas de erro comuns aos endpoints de busca por nome (GET/{nome}),
 * repetidas hoje de forma idêntica nos 4 controllers de hierarquia.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "400", description = "Bad Request:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Bad Request",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_400,
                description = "Se o ID fornecido na URL estiver malformado ou for inválido."))
})
@ApiResponse(responseCode = "401", description = "Unauthorized:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Unauthorized",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_401,
                description = "O cliente não está autenticado, mas o recurso exige autenticação."))
})
@ApiResponse(responseCode = "403", description = "Forbidden:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Forbidden",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_403,
                description = "O cliente não tem permissão para acessar o recurso solicitado."))
})
@ApiResponse(responseCode = "404", description = "Not Found", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Not Found",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_404,
                description = "O recurso com o ID fornecido não foi encontrado no banco de dados."))
})
@ApiResponse(responseCode = "500", description = "Internal Server Error:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Internal Server Error",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_500,
                description = "Ocorre um erro no servidor ao processar a requisição, como um problema de conexão com o banco de dados ou uma exceção inesperada."))
})
public @interface ApiErrorResponsesBusca {
}
