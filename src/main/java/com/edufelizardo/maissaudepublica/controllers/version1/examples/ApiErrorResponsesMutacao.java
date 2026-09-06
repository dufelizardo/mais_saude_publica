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
 * Respostas de erro comuns aos endpoints que alteram estado (POST/PATCH/DELETE),
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
                description = "Se os dados enviados no corpo da requisição forem malformados ou violarem as regras de validação."))
})
@ApiResponse(responseCode = "401", description = "Unauthorized:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Unauthorized",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_401,
                description = "O cliente não está autenticado, mas a criação do recurso exige autenticação."))
})
@ApiResponse(responseCode = "403", description = "Forbidden:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Forbidden",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_403,
                description = "O cliente está autenticado, mas não tem permissão para criar o recurso."))
})
@ApiResponse(responseCode = "404", description = "Not Found:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Not Found",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_404,
                description = "Se o recurso relacionado à criação não for encontrado, como um ID de relacionamento inexistente passado no payload."))
})
@ApiResponse(responseCode = "409", description = "Conflict:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Conflict",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_409,
                description = "Pode ocorrer quando a criação entra em conflito com o estado atual do recurso, como tentar criar um recurso com um nome ou ID já existente, se houver uma restrição de unicidade."))
})
@ApiResponse(responseCode = "422", description = "Unprocessable Entity:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Unprocessable Entity",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_422,
                description = "Se os dados no corpo da requisição são semanticamente inválidos (por exemplo, regras de validação não atendidas)."))
})
@ApiResponse(responseCode = "500", description = "Internal Server Error:", content = {
        @Content(mediaType = "application/json", array = @ArraySchema(
                schema = @Schema(implementation = ErrorExcepitionResponse.class)
        ), examples = @ExampleObject(name = "Internal Server Error",
                summary = "ErrorExceptionResponse",
                value = ExampleConstants.ERROR_EXAMPLE_500,
                description = "Um erro inesperado no servidor ao processar a criação do recurso."))
})
public @interface ApiErrorResponsesMutacao {
}
