package br.com.eduafelizardo.mais_saude_publica.controller;

import br.com.eduafelizardo.mais_saude_publica.domain.Endereco;
import br.com.eduafelizardo.mais_saude_publica.domain.dto.EnderecoRecord;
import br.com.eduafelizardo.mais_saude_publica.service.CosumoAPI;
import br.com.eduafelizardo.mais_saude_publica.service.IConverteDados;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enderecos")
@Tag(name = "Endereços", description = "API para buscar informações de endereços via CEP")
public class EnderecoController {
    
    private static final Logger logger = LoggerFactory.getLogger(EnderecoController.class);
    private static final String VIACEP_URL_PATTERN = "https://viacep.com.br/ws/%s/json/";

    @Autowired
    private CosumoAPI cosumoAPI;

    @Autowired
    private IConverteDados converteDados;

    @GetMapping("/{cep}")
    @Operation(summary = "Buscar endereço por CEP",
            description = "Busca informações completas de um endereço a partir do CEP no formato XXXXXXXX (8 dígitos)")
    @ApiResponse(responseCode = "200", description = "Endereço encontrado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Endereco.class)))
    @ApiResponse(responseCode = "400", description = "CEP inválido ou mal formatado")
    @ApiResponse(responseCode = "404", description = "CEP não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<?> buscarEnderecoPorCep(
            @PathVariable 
            @Parameter(description = "CEP no formato XXXXXXXX ou XXXXX-XXX", example = "01310100")
            String cep) {
        
        try {
            // Validar CEP
            String cepLimpo = limparCep(cep);
            if (!validarCep(cepLimpo)) {
                logger.warn("CEP inválido fornecido: {}", cep);
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("CEP inválido. Use o formato XXXXXXXX ou XXXXX-XXX"));
            }

            // Construir URL da API ViaCEP
            String urlViaCep = String.format(VIACEP_URL_PATTERN, cepLimpo);
            logger.info("Buscando endereço para CEP: {}", cepLimpo);

            // Chamar API ViaCEP
            String json = cosumoAPI.obterEndereco(urlViaCep);

            // Converter JSON para EnderecoRecord
            EnderecoRecord enderecoRecord = converteDados.obterDados(json, EnderecoRecord.class);

            // Verificar se CEP foi encontrado
            if (enderecoRecord == null || enderecoRecord.cep() == null) {
                logger.warn("CEP não encontrado: {}", cepLimpo);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("CEP não encontrado na base de dados da ViaCEP"));
            }

            // Converter para entidade Endereco
            Endereco endereco = new Endereco(enderecoRecord);
            logger.info("Endereço encontrado com sucesso para CEP: {}", cepLimpo);

            return ResponseEntity.ok(endereco);

        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao processar CEP: {}", cep, e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Erro na validação: " + e.getMessage()));
        } catch (JsonProcessingException e) {
            logger.error("Erro ao processar JSON da resposta da API ViaCEP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao processar dados da API ViaCEP"));
        } catch (RuntimeException e) {
            logger.error("Erro ao chamar API ViaCEP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao buscar endereço: " + e.getMessage()));
        }
    }

    /**
     * Remove caracteres especiais do CEP, deixando apenas números
     */
    private String limparCep(String cep) {
        return cep.replaceAll("[^0-9]", "");
    }

    /**
     * Valida se o CEP tem 8 dígitos
     */
    private boolean validarCep(String cep) {
        return cep != null && cep.length() == 8 && cep.matches("\\d{8}");
    }

    /**
     * Classe auxiliar para respostas de erro
     */
    public static class ErrorResponse {
        private String mensagem;
        private long timestamp;

        public ErrorResponse(String mensagem) {
            this.mensagem = mensagem;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMensagem() {
            return mensagem;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}

