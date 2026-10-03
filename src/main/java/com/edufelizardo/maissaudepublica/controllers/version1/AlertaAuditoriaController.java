package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AnaliseAlertaAuditoriaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AlertaAuditoriaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ResumoAlertasAuditoriaDto;
import com.edufelizardo.maissaudepublica.models.enuns.SeveridadeAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlertaAuditoria;
import com.edufelizardo.maissaudepublica.services.version1.AlertaAuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/auditoria/alerta/")
@Tag(name = "Alertas da auditoria", description = "Padrões suspeitos achados na trilha de auditoria (ver docs/adr/0096-alertas-da-auditoria.md).")
public class AlertaAuditoriaController {

    @Autowired
    private AlertaAuditoriaService service;

    @RequerPermissao({"AUDITORIA.CONSULTAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os alertas da auditoria",
            description = "Do mais recente ao mais antigo, com filtros opcionais de situação, tipo e severidade. Mesmo escopo da trilha "
                    + "(ADR-0071): alerta sem unidade só para quem audita a rede inteira.",
            tags = "Alertas da auditoria")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<AlertaAuditoriaResponseDto>> listar(@RequestParam(required = false) StatusAlertaAuditoria status,
                                                                   @RequestParam(required = false) TipoAlertaAuditoria tipo,
                                                                   @RequestParam(required = false) SeveridadeAlertaAuditoria severidade) {
        return ResponseEntity.ok(service.listar(status, tipo, severidade));
    }

    @RequerPermissao({"AUDITORIA.CONSULTAR"})
    @GetMapping(value = "resumo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Resume os alertas da auditoria",
            description = "Abertos, abertos de severidade alta, detectados nos últimos 7 dias e procedentes, no escopo de quem pergunta.",
            tags = "Alertas da auditoria")
    public ResponseEntity<ResumoAlertasAuditoriaDto> resumo() {
        return ResponseEntity.ok(service.resumo());
    }

    @RequerPermissao({"AUDITORIA.CONSULTAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um alerta da auditoria", description = "Fora do escopo, 403. Inexistente, 404.", tags = "Alertas da auditoria")
    @ApiErrorResponsesBusca
    public ResponseEntity<AlertaAuditoriaResponseDto> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @RequerPermissao({"AUDITORIA.CONSULTAR"})
    @PostMapping(value = "{uuid}/analise", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Conclui a análise de um alerta",
            description = "PROCEDENTE ou IMPROCEDENTE, com parecer de 10 a 2000 caracteres (400). Já analisado, ou alerta sobre "
                    + "quem analisa, 422. A análise entra na trilha.",
            tags = "Alertas da auditoria")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AlertaAuditoriaResponseDto> analisar(@PathVariable UUID uuid,
                                                               @Valid @RequestBody AnaliseAlertaAuditoriaRequestDto dto) {
        return ResponseEntity.ok(service.analisar(uuid, dto));
    }

    @RequerPermissao({"AUDITORIA.CONSULTAR"})
    @PostMapping(value = "deteccao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Roda a detecção de alertas agora",
            description = "A mesma rotina que roda a cada 5 minutos, na hora. Devolve quantos alertas novos foram abertos.",
            tags = "Alertas da auditoria")
    public ResponseEntity<Map<String, Integer>> detectar() {
        return ResponseEntity.ok(Map.of("novos", service.detectarAgora()));
    }
}
