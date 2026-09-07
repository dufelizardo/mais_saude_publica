package com.edufelizardo.maissaudepublica.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorExceptionResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorExceptionResponse error = new ErrorExceptionResponse("Not Found", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceBadRequestException.class)
    public ResponseEntity<ErrorExceptionResponse> handleResourceBadRequest(ResourceBadRequestException ex) {
        ErrorExceptionResponse error = new ErrorExceptionResponse("Bad Request", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceUnauthorizedException.class)
    public ResponseEntity<ErrorExceptionResponse> handleResourceUnauthorized(ResourceUnauthorizedException ex) {
        ErrorExceptionResponse error = new ErrorExceptionResponse("Unauthorized", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorExceptionResponse> handleResourceConflict(ResourceConflictException ex) {
        ErrorExceptionResponse error = new ErrorExceptionResponse("Conflict", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Traduz violação de constraint única (ex: nome duplicado) para 409 Conflict. Necessário como
     * handler global porque o flush do Hibernate para um save() dentro de um método @Transactional
     * costuma ser adiado até o commit da transação — ou seja, ocorre DEPOIS que o método de serviço
     * já retornou, então um try/catch dentro do próprio service nunca chega a capturá-la.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorExceptionResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ErrorExceptionResponse error = new ErrorExceptionResponse("Conflict", "Já existe uma instituição registrada com este nome.");
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceUnprocessableEntityException.class)
    public ResponseEntity<ErrorExceptionResponse> handleResourceUnprocessableEntity(ResourceUnprocessableEntityException ex) {
        ErrorExceptionResponse error = new ErrorExceptionResponse("Unprocessable Entity", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorExceptionResponse> handleGenericException(Exception ex) {
        ErrorExceptionResponse error = new ErrorExceptionResponse("Internal Server Error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid (MethodArgumentNotValidException ex,
                                                                   HttpHeaders headers,
                                                                   HttpStatusCode status,
                                                                   WebRequest request) {
        // Extrai todas as mensagens de erro
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        // Cria uma resposta personalizada
        ErrorExceptionResponse errorResponse = new ErrorExceptionResponse("Erro de Validação", String.join(", ", errors));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Sem este override, o comportamento default de ResponseEntityExceptionHandler responde com
     * um ProblemDetail (RFC 7807) para corpo de requisição ilegível (JSON quebrado, ou "null"),
     * vazando um formato diferente do ErrorExceptionResponse usado no resto da API — ver AQUAQE-215.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                    HttpHeaders headers,
                                                                    HttpStatusCode status,
                                                                    WebRequest request) {
        ErrorExceptionResponse errorResponse = new ErrorExceptionResponse("Bad Request",
                "O corpo da requisição está ausente ou não pôde ser interpretado como JSON válido.");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
