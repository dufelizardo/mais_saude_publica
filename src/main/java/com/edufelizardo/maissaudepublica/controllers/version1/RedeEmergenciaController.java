package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RedeEmergenciaDtos;
import com.edufelizardo.maissaudepublica.services.version1.RedeEmergenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Emergências · rede", description = "Onde tem na rede, compromissos nas unidades afetadas e prioridade da emergência na Regulação "
        + "(ver docs/adr/0194-emergencias-redistribuicao-e-regulacao.md).")
public class RedeEmergenciaController {

    private static final String TAG = "Emergências · rede";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private RedeEmergenciaService service;

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @GetMapping(value = "emergencia/{uuid}/disponibilidade", produces = JSON)
    @Operation(summary = "Onde tem na rede", description = "O saldo dentro da validade do medicamento (Farmácia) ou do material (Estoque, já sem "
            + "a reserva) por unidade, as afetadas marcadas. Um dos dois (400).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<RedeEmergenciaDtos.Disponibilidade>> disponibilidade(@PathVariable UUID uuid, @RequestParam(required = false) UUID medicamentoId,
                                                                                    @RequestParam(required = false) UUID materialId) {
        return ResponseEntity.ok(service.disponibilidade(uuid, medicamentoId, materialId));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @GetMapping(value = "emergencia/{uuid}/rede", produces = JSON)
    @Operation(summary = "Compromissos e regulações", description = "Os agendamentos e as regulações dos próximos 30 dias nas unidades afetadas fora "
            + "de operação e as regulações priorizadas. Pede também PACIENTE.CONSULTAR.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<RedeEmergenciaDtos.Rede> rede(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.rede(uuid));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @GetMapping(value = "emergencia/{uuid}/regulacoes", produces = JSON)
    @Operation(summary = "Regulações em aberto do paciente", description = "Para escolher qual priorizar.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<RedeEmergenciaDtos.Regulacao>> regulacoes(@PathVariable UUID uuid, @RequestParam UUID pacienteId) {
        return ResponseEntity.ok(service.regulacoesDoPaciente(uuid, pacienteId));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @PostMapping(value = "emergencia/{uuid}/regulacao/{solicitacaoId}/prioridade", produces = JSON)
    @Operation(summary = "Prioriza a regulação", description = "Emergência ativa (422); solicitação em aberto (422) de paciente deslocado ou da área "
            + "(422); já priorizada (409). Sobe dentro da mesma cor; o regulador decide.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RedeEmergenciaDtos.Rede> priorizar(@PathVariable UUID uuid, @PathVariable UUID solicitacaoId) {
        return ResponseEntity.ok(service.priorizar(uuid, solicitacaoId));
    }

    @RequerPermissao({"EMERGENCIA.COORDENAR"})
    @DeleteMapping(value = "emergencia/{uuid}/regulacao/{solicitacaoId}/prioridade", produces = JSON)
    @Operation(summary = "Retira a prioridade", description = "Só a desta emergência (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RedeEmergenciaDtos.Rede> retirar(@PathVariable UUID uuid, @PathVariable UUID solicitacaoId) {
        return ResponseEntity.ok(service.retirar(uuid, solicitacaoId));
    }
}
