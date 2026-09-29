package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AdministracaoMedicamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoAdministracaoMedicamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AdministracaoMedicamentoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.AdministracaoMedicamentoService;
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
@RequestMapping(value = "/api/v1/administracao-medicamento/")
@Tag(name = "Administração de Medicamento", description = "Checagem de enfermagem dos medicamentos prescritos: administrado (baixa o lote pelo livro da Farmácia) ou não administrado, com motivo (ver docs/adr/0064-administracao-de-medicamento-enfermagem-e-farmacia.md).")
public class AdministracaoMedicamentoController {

    @Autowired
    private AdministracaoMedicamentoService service;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a checagem de um medicamento prescrito",
            description = "ADMINISTRADO: loteId, dose, via e quantidade; o lote precisa ser do medicamento (400), da unidade do "
                    + "atendimento e dentro da validade (422), e é baixado pelo livro da Farmácia (saldo insuficiente → 422). "
                    + "NAO_ADMINISTRADO: motivoNaoAdministracao (OUTRO exige observação). A consulta é a prescrição: precisa ser "
                    + "do atendimento (400) e vigente (422).",
            tags = "Administração de Medicamento")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody AdministracaoMedicamentoRequestDto dto) {
        AdministracaoMedicamentoResponseDto r = service.registrar(dto);

        String details = "Medicamento: " + r.getMedicamentoNome() + ", Situação: " + r.getSituacao()
                + (r.getQuantidade() != null ? ", Quantidade: " + r.getQuantidade() : "");
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Administração registrada com sucesso!", details));
    }

    @PostMapping(value = "{uuid}/retificacao", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retifica uma administração de medicamento",
            description = "Grava uma nova versão com o motivo (ADR-0062); se a anterior baixou estoque, a baixa é estornada "
                    + "(ADMINISTRACAO_ESTORNO) antes da nova. Só a versão vigente (422); mesmo atendimento e prescrição (400).",
            tags = "Administração de Medicamento")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = SuccessResponseDto.class)
            ), examples = @ExampleObject(name = "Success",
                    summary = "SuccessResponse",
                    value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> retificar(@PathVariable UUID uuid,
                                                        @Valid @RequestBody RetificacaoAdministracaoMedicamentoRequestDto dto) {
        AdministracaoMedicamentoResponseDto r = service.retificar(uuid, dto);

        String details = "Nova versão: " + r.getUuid() + ", Corrige: " + r.getRetificacaoDeUuid();
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Administração retificada com sucesso!", details));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as administrações, da mais recente para a mais antiga", tags = "Administração de Medicamento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = AdministracaoMedicamentoResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<AdministracaoMedicamentoResponseDto>> findAll() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping(value = "atendimento/{atendimentoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as administrações de um atendimento", tags = "Administração de Medicamento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(
                    schema = @Schema(implementation = AdministracaoMedicamentoResponseDto.class)))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<List<AdministracaoMedicamentoResponseDto>> findByAtendimento(@PathVariable UUID atendimentoId) {
        return ResponseEntity.ok(service.listarPorAtendimento(atendimentoId));
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma administração pelo id", tags = "Administração de Medicamento")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AdministracaoMedicamentoResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<AdministracaoMedicamentoResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }
}
