package br.com.eduafelizardo.mais_saude_publica.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CosumoAPI {
    private static final Logger logger = LoggerFactory.getLogger(CosumoAPI.class);
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * Consome a API ViaCEP e retorna o JSON com os dados do endereço
     *
     * @param enderecoURL URL da API ViaCEP (ex: https://viacep.com.br/ws/02942000/json/)
     * @return String contendo o JSON da resposta
     * @throws IllegalArgumentException se a URL for nula ou inválida
     * @throws RuntimeException se houver erro na requisição HTTP
     */
    public String obterEndereco(String enderecoURL) {
        if (enderecoURL == null || enderecoURL.isBlank()) {
            throw new IllegalArgumentException("URL do endereço não pode ser nula ou vazia");
        }

        logger.info("Buscando endereço em: {}", enderecoURL);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(enderecoURL))
                    .timeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("Erro na API ViaCEP. Status code: {}", response.statusCode());
                throw new RuntimeException("Erro ao consultar API ViaCEP. Status: " + response.statusCode());
            }

            String json = response.body();
            
            if (json == null || json.isBlank()) {
                logger.error("Resposta vazia da API ViaCEP");
                throw new RuntimeException("Resposta vazia recebida da API ViaCEP");
            }

            logger.debug("Endereço obtido com sucesso");
            return json;

        } catch (IOException e) {
            logger.error("Erro de IO ao consultar API ViaCEP", e);
            throw new RuntimeException("Erro de conexão ao consultar API ViaCEP", e);
        } catch (InterruptedException e) {
            logger.error("Requisição interrompida", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Requisição para API ViaCEP foi interrompida", e);
        }
    }
}
