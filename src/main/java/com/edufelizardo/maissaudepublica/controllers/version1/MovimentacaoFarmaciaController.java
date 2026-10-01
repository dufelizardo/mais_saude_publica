package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MovimentacaoFarmaciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MovimentacaoFarmaciaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.MovimentacaoFarmaciaService;
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
@RequestMapping(value = "/api/v1/movimentacao-farmacia/")
@Tag(name = "Movimentação de Farmácia", description = "Livro de movimentação do estoque de lotes: perdas, ajustes de inventário e extrato por lote (ver docs/adr/0057-livro-de-movimentacao-do-estoque-da-farmacia.md).")
public class MovimentacaoFarmaciaController {

    @Autowired
    private MovimentacaoFarmaciaService service;

    @RequerPermissao({"FARMACIA.GERENCIAR_ESTOQUE"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra uma perda ou um ajuste de inventário",
            description = "PERDA: quantidade e motivoPerda (OUTRO exige justificativa). AJUSTE_INVENTARIO: saldoContado "
                    + "e justificativa. Entrada e dispensação são lançadas pelo próprio sistema. Lançamentos são imutáveis.",
            tags = "Movimentação de Farmácia")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody MovimentacaoFarmaciaRequestDto dto) {
        MovimentacaoFarmaciaResponseDto responseDto = service.registrar(dto);

        String successMessage = "Movimentação registrada com sucesso!";
        String details = "Tipo: " + responseDto.getTipo() + ", Variação: " + responseDto.getQuantidade()
                + ", Saldo: " + responseDto.getSaldoApos();

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto(successMessage, details));
    }

    @RequerPermissao({"FARMACIA.CONSULTAR"})
    @GetMapping(value = "lote/{loteId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Extrato de um lote, do lançamento mais antigo ao mais recente", tags = "Movimentação de Farmácia")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = MovimentacaoFarmaciaResponseDto.class)))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<MovimentacaoFarmaciaResponseDto>> extratoDoLote(@PathVariable UUID loteId) {
        return ResponseEntity.ok(service.extratoDoLote(loteId));
    }

    @RequerPermissao({"FARMACIA.CONSULTAR"})
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma movimentação pelo id", tags = "Movimentação de Farmácia")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MovimentacaoFarmaciaResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<MovimentacaoFarmaciaResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }
}
