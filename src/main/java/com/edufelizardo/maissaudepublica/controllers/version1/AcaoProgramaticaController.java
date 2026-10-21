package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoProgramaticaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.BuscaAtivaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RealizacaoAcaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoAcaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcaoProgramaticaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcoesProgramaticasDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.services.version1.AcaoProgramaticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Ações Programáticas", description = "Ações previstas nos programas de saúde: individuais e coletivas, busca ativa, ligação com a "
        + "Agenda e confirmação pelo profissional (ver docs/adr/0132-programas-acoes-programaticas.md).")
public class AcaoProgramaticaController {

    private static final String TAG = "Ações Programáticas";

    @Autowired
    private AcaoProgramaticaService service;

    @AuditarLeitura
    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "acoes-programaticas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ações programáticas", description = "As do escopo no período (padrão: a semana), mais as vencidas; prontas para "
            + "confirmar e vencidas primeiro. Indicadores: pendentes, vencidas, realizadas no mês e prontas para confirmar. Leitura auditada.",
            tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<AcoesProgramaticasDto> acoes(@RequestParam(required = false) UUID programaId, @RequestParam(required = false) UUID unidadeId,
                                                       @RequestParam(required = false) UUID equipeId,
                                                       @RequestParam(required = false) String responsavelMatricula,
                                                       @RequestParam(required = false) LocalDate de, @RequestParam(required = false) LocalDate ate,
                                                       @RequestParam(required = false) SituacaoAcaoProgramatica situacao) {
        return ResponseEntity.ok(service.acoes(programaId, unidadeId, equipeId, responsavelMatricula, de, ate, situacao));
    }

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PostMapping(value = "acao-programatica", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Prevê uma ação programática", description = "Individual pelo acompanhamento vigente; coletiva pela unidade, com "
            + "participantes acompanhados no programa (422). Consulta programada, visita e busca ativa são individuais (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcaoProgramaticaDto> prever(@Valid @RequestBody AcaoProgramaticaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.prever(dto));
    }

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/busca-ativa", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Gera a busca ativa do acompanhado", description = "Sem responsável, o ACS da microárea de adscrição. Uma pendente por "
            + "acompanhamento (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcaoProgramaticaDto> buscaAtiva(@PathVariable UUID uuid, @Valid @RequestBody BuscaAtivaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.buscaAtiva(uuid, dto));
    }

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PostMapping(value = "acao-programatica/{uuid}/realizacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Confirma a realização", description = "Data até hoje; o atendimento do mesmo paciente (422); a busca ativa pede o "
            + "resultado (400). Já realizada, não realizada ou cancelada: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcaoProgramaticaDto> realizar(@PathVariable UUID uuid, @Valid @RequestBody RealizacaoAcaoRequestDto dto) {
        return ResponseEntity.ok(service.realizar(uuid, dto));
    }

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PostMapping(value = "acao-programatica/{uuid}/situacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Não realizada ou cancelada", description = "Com o motivo (400). Só a pendente muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcaoProgramaticaDto> situacao(@PathVariable UUID uuid, @Valid @RequestBody SituacaoAcaoRequestDto dto) {
        return ResponseEntity.ok(service.mudarSituacao(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"PRONTUARIO.CONSULTAR", "PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "paciente/{pacienteId}/acoes-programaticas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ações programáticas do paciente", description = "Com as regras do prontuário: vínculo assistencial (403 "
            + "VINCULO_ASSISTENCIAL_AUSENTE) e auditoria de leitura; programa sensível só de quem acompanha na unidade.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<AcaoProgramaticaDto>> doPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.doPaciente(pacienteId));
    }
}
