package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CondicaoOdontologicaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MarcacoesOdontogramaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CondicaoOdontologicaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.OdontogramaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RegistroOdontogramaDto;
import com.edufelizardo.maissaudepublica.services.version1.OdontogramaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Odontograma", description = "Catálogo de condições, marcações por dente (FDI) e face na avaliação odontológica, anulação e a "
        + "projeção do odontograma numa data (ver docs/adr/0135-saude-bucal-odontograma.md).")
public class OdontogramaController {

    private static final String TAG = "Odontograma";

    @Autowired
    private OdontogramaService service;

    @LiberadoParaAutenticados
    @GetMapping(value = "condicoes-odontologicas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Catálogo de condições do odontograma", description = "Ativas e inativas, com cor, sigla e alcance.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CondicaoOdontologicaDto>> condicoes() {
        return ResponseEntity.ok(service.condicoes());
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "condicao-odontologica", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra uma condição", description = "Código repetido: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CondicaoOdontologicaDto> criarCondicao(@Valid @RequestBody CondicaoOdontologicaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarCondicao(dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PutMapping(value = "condicao-odontologica/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza uma condição", description = "O código não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CondicaoOdontologicaDto> atualizarCondicao(@PathVariable UUID uuid, @Valid @RequestBody CondicaoOdontologicaRequestDto dto) {
        return ResponseEntity.ok(service.atualizarCondicao(uuid, dto));
    }

    @RequerPermissao({"ODONTOLOGIA.REGISTRAR"})
    @PostMapping(value = "avaliacao-odontologica/{uuid}/odontograma", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Marca o odontograma na avaliação", description = "Dente fora da FDI (400); face que o dente não tem, condição inativa "
            + "ou do alcance errado e avaliação retificada (422). Responde o odontograma de hoje.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<OdontogramaDto> marcar(@PathVariable UUID uuid, @Valid @RequestBody MarcacoesOdontogramaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.marcar(uuid, dto));
    }

    @RequerPermissao({"ODONTOLOGIA.REGISTRAR"})
    @PostMapping(value = "registro-odontograma/{uuid}/anulacao", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Anula uma marcação", description = "Corpo: {\"motivo\": \"...\"} (400 sem motivo). Já anulada: 422. Continua no "
            + "histórico e sai da projeção.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RegistroOdontogramaDto> anular(@PathVariable UUID uuid, @RequestBody Map<String, String> corpo) {
        return ResponseEntity.ok(service.anular(uuid, corpo.get("motivo")));
    }

    @AuditarLeitura
    @RequerPermissao({"PRONTUARIO.CONSULTAR", "ODONTOLOGIA.REGISTRAR"})
    @GetMapping(value = "paciente/{pacienteId}/odontograma", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Odontograma do paciente", description = "A projeção dos registros até a data (padrão: hoje). Regras do prontuário: "
            + "vínculo (403 VINCULO_ASSISTENCIAL_AUSENTE) e leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<OdontogramaDto> odontograma(@PathVariable UUID pacienteId,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.odontograma(pacienteId, data));
    }

    @AuditarLeitura
    @RequerPermissao({"PRONTUARIO.CONSULTAR", "ODONTOLOGIA.REGISTRAR"})
    @GetMapping(value = "paciente/{pacienteId}/odontograma/dente/{dente}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Histórico de um dente", description = "Todos os registros do dente, inclusive os anulados, com a origem.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<RegistroOdontogramaDto>> historico(@PathVariable UUID pacienteId, @PathVariable int dente) {
        return ResponseEntity.ok(service.historicoDoDente(pacienteId, dente));
    }
}
