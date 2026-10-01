package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LoteAtualizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LoteResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.LoteService;
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
@RequestMapping(value = "/api/v1/lote/")
@Tag(name = "Lote", description = "Endpoints para gerenciar lotes de medicamentos em estoque por unidade de saúde (ver docs/adr/0050-lote-segunda-entidade-da-farmacia.md).")
public class LoteController {

    @Autowired
    private LoteService service;

    @RequerPermissao({"FARMACIA.CONSULTAR", "FARMACIA.DISPENSAR", "MEDICACAO.ADMINISTRAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os lotes cadastrados", tags = "Lote")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = LoteResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "LoteResponse",
                    value = ExampleConstants.LOTE_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<LoteResponseDto>> getAll() {
        List<LoteResponseDto> responseDtos = service.listar();
        if (responseDtos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum item foi encontrado.");
        }
        return ResponseEntity.ok(responseDtos);
    }

    @RequerPermissao({"FARMACIA.CONSULTAR", "FARMACIA.DISPENSAR", "MEDICACAO.ADMINISTRAR"})
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um lote pelo id", tags = "Lote")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LoteResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<LoteResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @RequerPermissao({"FARMACIA.GERENCIAR_ESTOQUE"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a entrada de um lote",
            description = "Registra a entrada de uma remessa (medicamento, número do lote e validade) em uma unidade de saúde. "
                    + "Cada remessa tem um único lote por unidade: se ele já existe, a quantidade é somada a ele e a resposta é 200; "
                    + "se não, o lote é criado e a resposta é 201 (ver docs/adr/0060-uma-remessa-um-lote-por-unidade.md).",
            tags = "Lote")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody LoteRequestDto dto) {
        LoteService.EntradaDeLote entrada = service.criar(dto);
        LoteResponseDto responseDto = entrada.lote();

        String details = "Medicamento: " + responseDto.getMedicamentoNome() + ", Quantidade: " + responseDto.getQuantidade();
        if (!entrada.loteNovo()) {
            // Mesma remessa já tinha lote nesta unidade: a entrada somou nele (ADR-0060).
            return ResponseEntity.ok(new SuccessResponseDto("Entrada registrada no lote já existente!", details));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Lote criado com sucesso!", details));
    }

    @RequerPermissao({"FARMACIA.GERENCIAR_ESTOQUE"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Corrige número do lote e validade",
            description = "Só corrige o que foi digitado errado na entrada. Quantidade muda apenas pelo livro de "
                    + "movimentação (/api/v1/movimentacao-farmacia/); medicamento e unidade não mudam (ADR-0057).",
            tags = "Lote")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_UPDATE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> update(@PathVariable UUID uuid, @Valid @RequestBody LoteAtualizacaoRequestDto dto) {
        LoteResponseDto responseDto = service.atualizar(uuid, dto);

        String successMessage = "Lote atualizado com sucesso!";
        String details = "Lote: " + responseDto.getNumeroLote() + ", Validade: " + responseDto.getValidade();

        return ResponseEntity.ok(new SuccessResponseDto(successMessage, details));
    }
}
