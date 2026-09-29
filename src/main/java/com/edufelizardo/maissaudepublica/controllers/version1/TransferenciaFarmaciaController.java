package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TransferenciaFarmaciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransferenciaFarmaciaResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.TransferenciaFarmaciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/transferencia-farmacia/")
@Tag(name = "Transferência de Farmácia", description = "Transferência de estoque de um lote para outra unidade, lançada no livro de movimentação (ver docs/adr/0059-transferencia-de-estoque-entre-unidades.md).")
public class TransferenciaFarmaciaController {

    @Autowired
    private TransferenciaFarmaciaService service;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Transfere parte do saldo de um lote para outra unidade",
            description = "Lança TRANSFERENCIA_SAIDA no lote de origem e TRANSFERENCIA_ENTRADA no lote da mesma remessa "
                    + "(medicamento, número e validade) na unidade de destino, criado com saldo zero se ainda não existir. "
                    + "Saldo insuficiente ou lote vencido respondem 422. Transferências são imutáveis.",
            tags = "Transferência de Farmácia")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody TransferenciaFarmaciaRequestDto dto) {
        TransferenciaFarmaciaResponseDto responseDto = service.transferir(dto);

        String successMessage = "Transferência registrada com sucesso!";
        String details = "Lote: " + responseDto.getNumeroLote() + ", Quantidade: " + responseDto.getQuantidade()
                + ", De: " + responseDto.getUnidadeOrigemNome() + ", Para: " + responseDto.getUnidadeDestinoNome();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as transferências, da mais recente para a mais antiga", tags = "Transferência de Farmácia")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = TransferenciaFarmaciaResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TransferenciaFarmaciaResponseDto>> findAll() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma transferência pelo id", tags = "Transferência de Farmácia")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TransferenciaFarmaciaResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<TransferenciaFarmaciaResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }
}
