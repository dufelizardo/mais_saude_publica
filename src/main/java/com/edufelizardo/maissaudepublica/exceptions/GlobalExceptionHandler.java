package com.edufelizardo.maissaudepublica.exceptions;

import com.edufelizardo.maissaudepublica.exceptions.datautilexception.ErrorExcepitionResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorExcepitionResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorExcepitionResponse error = new ErrorExcepitionResponse("Not Found", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorExcepitionResponse> handleGenericException(Exception ex) {
        ErrorExcepitionResponse error = new ErrorExcepitionResponse("Internal Server Error", ex.getMessage());
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
        ErrorExcepitionResponse errorResponse = new ErrorExcepitionResponse("Erro de Validação", String.join(", ", errors));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
