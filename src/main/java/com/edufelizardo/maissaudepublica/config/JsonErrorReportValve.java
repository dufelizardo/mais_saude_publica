package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.exceptions.ErrorExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ErrorReportValve;
import org.springframework.http.HttpStatus;

import java.io.IOException;

/**
 * Substitui a página HTML padrão do Tomcat para erros que nunca chegam ao Spring MVC (ex: URL com
 * percent-encoding inválido, como "%zz") por JSON no mesmo contrato usado pelo
 * GlobalExceptionHandler ({message, details}) — ver AQUAQE-216.
 *
 * Instanciado pelo próprio Tomcat via reflection (Container.setErrorReportValveClass), por isso
 * não pode receber dependências via injeção de construtor do Spring.
 */
public class JsonErrorReportValve extends ErrorReportValve {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void report(Request request, Response response, Throwable throwable) {
        int statusCode = response.getStatus();
        if (statusCode < 400 || response.getContentWritten() > 0) {
            return;
        }

        try {
            String reasonPhrase = HttpStatus.valueOf(statusCode).getReasonPhrase();
            ErrorExceptionResponse body = new ErrorExceptionResponse(
                    reasonPhrase,
                    "A solicitação não pôde ser processada pelo servidor.");
            byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(body);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
        } catch (IOException | IllegalArgumentException e) {
            // Este valve já está no caminho de tratamento de erro do container — se algo der
            // errado aqui (ex: status code fora do enum HttpStatus), melhor deixar sem corpo do
            // que propagar e mascarar o erro original.
        }
    }
}
