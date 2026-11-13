package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FichaAphRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegistroAphRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FichaAphDtos;
import com.edufelizardo.maissaudepublica.services.version1.FichaAphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Ficha clínica do atendimento pré-hospitalar (ADR-0188). */
@RestController
@RequestMapping(value = "/api/v1/aph/")
@Tag(name = "Atendimento pré-hospitalar", description = "Ficha clínica do APH na ocorrência de urgência (ver docs/adr/0188-ficha-aph.md).")
public class FichaAphController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String TAG = "Atendimento pré-hospitalar";

    @Autowired
    private FichaAphService service;

    @RequerPermissao({"APH.REGISTRAR"})
    @GetMapping(value = "atendimentos", produces = JSON)
    @Operation(summary = "Atendimentos na cena", description = "Ocorrências com recurso enviado em curso, e as concluídas há até 24 horas com a ficha por finalizar.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<FichaAphDtos.Atendimento>> atendimentos(@RequestParam(required = false) UUID centralId) {
        return ResponseEntity.ok(service.atendimentos(centralId));
    }

    @RequerPermissao({"APH.REGISTRAR", "URGENCIA.RECEBER", "URGENCIA.REGULAR", "ATENDIMENTO.GERENCIAR"})
    @AuditarLeitura
    @GetMapping(value = "ocorrencia/{uuid}/ficha", produces = JSON)
    @Operation(summary = "Ficha da ocorrência", description = "Dado clínico: a central e a unidade de destino. Sem ficha: 404. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<FichaAphDtos.Ficha> daOcorrencia(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.daOcorrencia(uuid));
    }

    @RequerPermissao({"APH.REGISTRAR"})
    @PutMapping(value = "ocorrencia/{uuid}/ficha", produces = JSON, consumes = JSON)
    @Operation(summary = "Abre ou atualiza a ficha", description = "Só com recurso enviado e antes da finalização (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FichaAphDtos.Ficha> salvar(@PathVariable UUID uuid, @Valid @RequestBody FichaAphRequestDto dto) {
        return ResponseEntity.ok(service.salvar(uuid, dto));
    }

    @RequerPermissao({"APH.REGISTRAR"})
    @PostMapping(value = "ficha/{uuid}/sinais-vitais", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra uma série de sinais vitais", description = "Fora da faixa ou sem medida: 400. Ficha finalizada: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FichaAphDtos.Ficha> sinais(@PathVariable UUID uuid, @Valid @RequestBody RegistroAphRequestDto dto) {
        return ResponseEntity.ok(service.registrarSinais(uuid, dto));
    }

    @RequerPermissao({"APH.REGISTRAR"})
    @PostMapping(value = "ficha/{uuid}/medicamentos", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra um medicamento dado", description = "Nome, dose e via (400). Ficha finalizada: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FichaAphDtos.Ficha> medicamento(@PathVariable UUID uuid, @Valid @RequestBody RegistroAphRequestDto dto) {
        return ResponseEntity.ok(service.registrarMedicamento(uuid, dto));
    }

    @RequerPermissao({"APH.REGISTRAR"})
    @PostMapping(value = "ficha/{uuid}/finalizacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Finaliza a ficha", description = "Natureza e desfecho; óbito com hora e médico; recusa com quem recusou; remoção com quem recebeu (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FichaAphDtos.Ficha> finalizar(@PathVariable UUID uuid, @Valid @RequestBody RegistroAphRequestDto dto) {
        return ResponseEntity.ok(service.finalizar(uuid, dto));
    }

    @RequerPermissao({"APH.REGISTRAR"})
    @PostMapping(value = "ficha/{uuid}/adendo", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra um adendo", description = "Só na ficha finalizada (422), com o texto (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FichaAphDtos.Ficha> adendo(@PathVariable UUID uuid, @Valid @RequestBody RegistroAphRequestDto dto) {
        return ResponseEntity.ok(service.adendo(uuid, dto));
    }

    @RequerPermissao({"APH.REGISTRAR", "URGENCIA.RECEBER", "URGENCIA.REGULAR", "ATENDIMENTO.GERENCIAR"})
    @AuditarLeitura
    @GetMapping(value = "ficha/{uuid}/ficha.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Ficha em PDF", description = "Para entregar no destino. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> pdf(@PathVariable UUID uuid) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ficha-aph-" + uuid + ".pdf\"")
                .body(service.pdf(uuid));
    }
}
