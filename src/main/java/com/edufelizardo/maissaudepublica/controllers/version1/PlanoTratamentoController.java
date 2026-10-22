package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ItemPlanoTratamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PlanoTratamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoItemPlanoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PlanoTratamentoDto;
import com.edufelizardo.maissaudepublica.services.version1.PlanoTratamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Plano de tratamento odontológico", description = "O plano com os itens previstos, executados pelos procedimentos, e o "
        + "tratamento concluído (ver docs/adr/0137-saude-bucal-plano-de-tratamento.md).")
public class PlanoTratamentoController {

    private static final String TAG = "Plano de tratamento odontológico";

    @Autowired
    private PlanoTratamentoService service;

    @RequerPermissao({"ODONTOLOGIA.PLANEJAR"})
    @PostMapping(value = "plano-tratamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Abre o plano de tratamento", description = "No atendimento de odontologia (422), um aberto por paciente (409). Sem "
            + "diagnóstico ou prioridade, valem os da avaliação de origem; sem ela, a prioridade é obrigatória (400). Os itens seguem o "
            + "complemento odontológico (dente e faces).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PlanoTratamentoDto> abrir(@Valid @RequestBody PlanoTratamentoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(dto));
    }

    @RequerPermissao({"ODONTOLOGIA.PLANEJAR"})
    @PostMapping(value = "plano-tratamento/{uuid}/item", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui um item no plano aberto", description = "Plano encerrado: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PlanoTratamentoDto> incluirItem(@PathVariable UUID uuid, @Valid @RequestBody ItemPlanoTratamentoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.incluirItem(uuid, dto));
    }

    @RequerPermissao({"ODONTOLOGIA.PLANEJAR"})
    @PostMapping(value = "item-plano-tratamento/{uuid}/situacao", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Muda a situação de um item", description = "EM_ANDAMENTO (do planejado), ENCAMINHADO (com a solicitação de regulação, "
            + "opcional) ou CANCELADO (com motivo, 400). Concluído só pelo procedimento realizado (400); concluído e cancelado não mudam (422).",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PlanoTratamentoDto> mudarSituacao(@PathVariable UUID uuid, @Valid @RequestBody SituacaoItemPlanoRequestDto dto) {
        return ResponseEntity.ok(service.mudarSituacao(uuid, dto));
    }

    @RequerPermissao({"ODONTOLOGIA.PLANEJAR"})
    @PostMapping(value = "plano-tratamento/{uuid}/conclusao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Conclui o tratamento", description = "O tratamento concluído: nenhum item planejado ou em andamento, e ao menos um "
            + "concluído (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PlanoTratamentoDto> concluir(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.concluir(uuid));
    }

    @RequerPermissao({"ODONTOLOGIA.PLANEJAR"})
    @PostMapping(value = "plano-tratamento/{uuid}/abandono", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Abandona o plano", description = "Corpo: {\"motivo\": \"...\"} (400 sem motivo).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PlanoTratamentoDto> abandonar(@PathVariable UUID uuid, @RequestBody Map<String, String> corpo) {
        return ResponseEntity.ok(service.abandonar(uuid, corpo.get("motivo")));
    }

    @AuditarLeitura
    @RequerPermissao({"PRONTUARIO.CONSULTAR", "ODONTOLOGIA.REGISTRAR", "ODONTOLOGIA.PLANEJAR"})
    @GetMapping(value = "paciente/{pacienteId}/planos-tratamento", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Planos de tratamento do paciente", description = "O mais recente primeiro, com os itens. Regras do prontuário: vínculo "
            + "e leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PlanoTratamentoDto>> planos(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.planosDoPaciente(pacienteId));
    }
}
