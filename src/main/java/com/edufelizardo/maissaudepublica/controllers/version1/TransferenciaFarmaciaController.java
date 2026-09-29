package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CancelamentoTransferenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RecebimentoTransferenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TransferenciaFarmaciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransferenciaFarmaciaResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.StatusTransferenciaFarmacia;
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
@Tag(name = "Transferência de Farmácia", description = "Transferência de estoque entre unidades em duas etapas — envio (em trânsito) e recebimento conferido no destino, ou cancelamento — lançada no livro de movimentação (ver docs/adr/0061-transferencia-em-duas-etapas-envio-e-recebimento.md).")
public class TransferenciaFarmaciaController {

    @Autowired
    private TransferenciaFarmaciaService service;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Envia parte do saldo de um lote para outra unidade",
            description = "Lança TRANSFERENCIA_SAIDA no lote de origem e deixa a transferência EM_TRANSITO até o "
                    + "recebimento no destino. Saldo insuficiente ou lote vencido respondem 422; destino igual à origem, 400.",
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
        TransferenciaFarmaciaResponseDto responseDto = service.enviar(dto);

        String successMessage = "Transferência enviada com sucesso!";
        String details = "Id: " + responseDto.getUuid() + ", Lote: " + responseDto.getNumeroLote()
                + ", Quantidade: " + responseDto.getQuantidade() + ", De: " + responseDto.getUnidadeOrigemNome()
                + ", Para: " + responseDto.getUnidadeDestinoNome() + ", Status: " + responseDto.getStatus();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @PostMapping(value = "{uuid}/recebimento", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra o recebimento conferido de uma transferência em trânsito",
            description = "Lança TRANSFERENCIA_ENTRADA com a quantidade que chegou no lote da mesma remessa na unidade "
                    + "de destino. Quantidade menor que a enviada exige motivoDivergencia e justificativaDivergencia (400). "
                    + "Precisa ser outro profissional, não quem enviou (422). Só transferências EM_TRANSITO (422).",
            tags = "Transferência de Farmácia")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> receber(@PathVariable UUID uuid,
                                                      @Valid @RequestBody RecebimentoTransferenciaRequestDto dto) {
        TransferenciaFarmaciaResponseDto responseDto = service.receber(uuid, dto);

        String details = "Enviada: " + responseDto.getQuantidade() + ", Recebida: " + responseDto.getQuantidadeRecebida()
                + ", Status: " + responseDto.getStatus();
        return ResponseEntity.ok(new SuccessResponseDto("Recebimento registrado com sucesso!", details));
    }

    @PostMapping(value = "{uuid}/cancelamento", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancela uma transferência em trânsito",
            description = "Lança TRANSFERENCIA_ESTORNO no lote de origem, devolvendo o saldo. Motivo obrigatório. "
                    + "Só transferências EM_TRANSITO (422).",
            tags = "Transferência de Farmácia")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> cancelar(@PathVariable UUID uuid,
                                                       @Valid @RequestBody CancelamentoTransferenciaRequestDto dto) {
        TransferenciaFarmaciaResponseDto responseDto = service.cancelar(uuid, dto);

        String details = "Estornado ao lote de origem: " + responseDto.getQuantidade() + ", Status: " + responseDto.getStatus();
        return ResponseEntity.ok(new SuccessResponseDto("Transferência cancelada com sucesso!", details));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as transferências, da mais recente para a mais antiga",
            description = "Filtros opcionais: status (ex.: EM_TRANSITO), unidadeOrigemId e unidadeDestinoId — "
                    + "por exemplo, o que está a caminho de uma unidade para ela conferir.",
            tags = "Transferência de Farmácia")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = TransferenciaFarmaciaResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TransferenciaFarmaciaResponseDto>> findAll(
            @RequestParam(required = false) StatusTransferenciaFarmacia status,
            @RequestParam(required = false) UUID unidadeOrigemId,
            @RequestParam(required = false) UUID unidadeDestinoId) {
        return ResponseEntity.ok(service.listar(status, unidadeOrigemId, unidadeDestinoId));
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
