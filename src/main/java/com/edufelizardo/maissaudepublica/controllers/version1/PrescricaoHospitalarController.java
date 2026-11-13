package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoPrescricaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PrescricaoHospitalarRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PrescricaoDtos;
import com.edufelizardo.maissaudepublica.services.version1.PrescricaoHospitalarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Prescrição hospitalar diária, aprazamento e checagem (ADR-0187). */
@RestController
@RequestMapping(value = "/api/v1/prescricao-hospitalar/")
@Tag(name = "Prescrição hospitalar", description = "Prescrição de 24 horas, horários aprazados e checagem pela enfermagem (ver docs/adr/0187-prescricao-hospitalar.md).")
public class PrescricaoHospitalarController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String TAG = "Prescrição hospitalar";
    private static final String VER_1 = "INTERNACAO.CONSULTAR";

    @Autowired
    private PrescricaoHospitalarService service;

    @RequerPermissao({"PRESCRICAO.HOSPITALAR"})
    @PostMapping(produces = JSON, consumes = JSON)
    @Operation(summary = "Prescreve por 24 horas", description = "Sem itens: 400. Internação com alta: 422. Outra prescrição começando no mesmo início ou depois: 409. "
            + "A nova encerra a anterior no seu início.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PrescricaoDtos.Prescricao> prescrever(@Valid @RequestBody PrescricaoHospitalarRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.prescrever(dto));
    }

    @RequerPermissao({"PRESCRICAO.HOSPITALAR"})
    @PostMapping(value = "{uuid}/renovacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Renova a prescrição", description = "Copia os itens não suspensos para as próximas 24 horas (padrão: do fim da anterior).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PrescricaoDtos.Prescricao> renovar(@PathVariable UUID uuid, @Valid @RequestBody PrescricaoHospitalarRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.renovar(uuid, dto));
    }

    @RequerPermissao({VER_1, "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "PRESCRICAO.HOSPITALAR", "PRESCRICAO.CHECAR", "DIETA.CHECAR"})
    @AuditarLeitura
    @GetMapping(produces = JSON)
    @Operation(summary = "Prescrições da internação", description = "Com os itens, os horários e as checagens; da mais nova para a mais velha. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<PrescricaoDtos.Prescricao>> listar(@RequestParam UUID internacaoId) {
        return ResponseEntity.ok(service.listar(internacaoId));
    }

    @RequerPermissao({VER_1, "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "PRESCRICAO.HOSPITALAR", "PRESCRICAO.CHECAR", "DIETA.CHECAR"})
    @GetMapping(value = "posto", produces = JSON)
    @Operation(summary = "Horários do posto de enfermagem", description = "Das internações em curso (padrão: 6 horas antes e 6 depois; até 48), com os atrasados. "
            + "Quem só checa dieta vê só a dieta.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<PrescricaoDtos.HorarioDoPosto>> posto(@RequestParam(required = false) UUID unidadeId,
            @RequestParam(required = false) UUID setorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant ate) {
        return ResponseEntity.ok(service.posto(unidadeId, setorId, de, ate));
    }

    @RequerPermissao({VER_1, "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "PRESCRICAO.HOSPITALAR", "PRESCRICAO.CHECAR", "DIETA.CHECAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}", produces = JSON)
    @Operation(summary = "Detalhe da prescrição", description = "Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<PrescricaoDtos.Prescricao> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @RequerPermissao({"PRESCRICAO.HOSPITALAR"})
    @PostMapping(value = "itens/{uuid}/suspensao", produces = JSON, consumes = JSON)
    @Operation(summary = "Suspende o item", description = "Com motivo; os horários pendentes ficam cancelados.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PrescricaoDtos.Prescricao> suspender(@PathVariable UUID uuid, @Valid @RequestBody AcaoPrescricaoRequestDto dto) {
        return ResponseEntity.ok(service.suspenderItem(uuid, dto));
    }

    @RequerPermissao({"PRESCRICAO.CHECAR", "DIETA.CHECAR"})
    @PostMapping(value = "itens/{uuid}/checagem", produces = JSON, consumes = JSON)
    @Operation(summary = "Checa o \"se necessário\"", description = "Sem horário, com a prescrição vigente.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PrescricaoDtos.Prescricao> checarSeNecessario(@PathVariable UUID uuid, @Valid @RequestBody AcaoPrescricaoRequestDto dto) {
        return ResponseEntity.ok(service.checarSeNecessario(uuid, dto));
    }

    @RequerPermissao({"PRESCRICAO.CHECAR", "DIETA.CHECAR"})
    @PostMapping(value = "horarios/{uuid}/checagem", produces = JSON, consumes = JSON)
    @Operation(summary = "Checa o horário", description = "Feito ou não feito. Medicamento com lote: baixa no livro da Farmácia. Já checado: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PrescricaoDtos.Prescricao> checar(@PathVariable UUID uuid, @Valid @RequestBody AcaoPrescricaoRequestDto dto) {
        return ResponseEntity.ok(service.checar(uuid, dto));
    }

    @RequerPermissao({"PRESCRICAO.CHECAR", "DIETA.CHECAR"})
    @PostMapping(value = "horarios/{uuid}/reaprazamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Reapraza o horário pendente", description = "Dentro da vigência, com motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PrescricaoDtos.Prescricao> reaprazar(@PathVariable UUID uuid, @Valid @RequestBody AcaoPrescricaoRequestDto dto) {
        return ResponseEntity.ok(service.reaprazar(uuid, dto));
    }

    @RequerPermissao({"PRESCRICAO.CHECAR", "DIETA.CHECAR"})
    @PostMapping(value = "checagens/{uuid}/retificacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Retifica a checagem", description = "Com motivo; a anterior fica guardada e a baixa do lote dela é estornada (ADR-0062).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PrescricaoDtos.Prescricao> retificar(@PathVariable UUID uuid, @Valid @RequestBody AcaoPrescricaoRequestDto dto) {
        return ResponseEntity.ok(service.retificar(uuid, dto));
    }
}
