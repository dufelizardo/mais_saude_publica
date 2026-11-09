package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DeslocamentoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DestinoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.OcorrenciaUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RecebimentoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReclassificacaoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegulacaoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VinculoPacienteUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CentralUrgenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DestinoUrgenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.OcorrenciaUrgenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelUrgenciaDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.services.version1.OcorrenciaUrgenciaService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping(value = "/api/v1/urgencia/")
@Tag(name = "Regulação de urgência", description = "Central de regulação das urgências (192): ocorrência, regulação médica, destino com vaga zero, "
        + "deslocamentos e recebimento no destino (ver docs/adr/0176-regulacao-de-urgencia.md).")
public class UrgenciaController {

    private static final String TAG = "Regulação de urgência";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private OcorrenciaUrgenciaService service;

    @RequerPermissao({"URGENCIA.RECEBER"})
    @PostMapping(value = "ocorrencia", produces = JSON, consumes = JSON)
    @Operation(summary = "Abre a ocorrência", description = "Pelo atendente da central. A central precisa da capacidade de regulação médica de urgência "
            + "(422). Chamado sem telefone, pedido de unidade sem a unidade ou origem crise: 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<OcorrenciaUrgenciaDto> abrir(@Valid @RequestBody OcorrenciaUrgenciaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(dto));
    }

    @RequerPermissao({"URGENCIA.REGULAR"})
    @PostMapping(value = "ocorrencia/{uuid}/regulacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Regula a ocorrência", description = "Gravidade, decisão e conduta; o recurso e o destino conforme a decisão (400). A orientação "
            + "fecha a ocorrência. Destino sem perfil Urgência ou igual à unidade que pediu: 422. Vaga zero pede justificativa.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<OcorrenciaUrgenciaDto> regular(@PathVariable UUID uuid, @Valid @RequestBody RegulacaoUrgenciaRequestDto dto) {
        return ResponseEntity.ok(service.regular(uuid, dto));
    }

    @RequerPermissao({"URGENCIA.REGULAR"})
    @PostMapping(value = "ocorrencia/{uuid}/destino", produces = JSON, consumes = JSON)
    @Operation(summary = "Define ou troca o destino", description = "Depois do envio, com a equipe no local; antes da chegada ao destino.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<OcorrenciaUrgenciaDto> destino(@PathVariable UUID uuid, @Valid @RequestBody DestinoUrgenciaRequestDto dto) {
        return ResponseEntity.ok(service.destino(uuid, dto));
    }

    @RequerPermissao({"URGENCIA.REGULAR"})
    @PostMapping(value = "ocorrencia/{uuid}/reclassificacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Reclassifica a gravidade", description = "Ocorrência regulada e aberta; a mesma gravidade: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<OcorrenciaUrgenciaDto> reclassificar(@PathVariable UUID uuid, @Valid @RequestBody ReclassificacaoUrgenciaRequestDto dto) {
        return ResponseEntity.ok(service.reclassificar(uuid, dto));
    }

    @RequerPermissao({"URGENCIA.RECEBER"})
    @PostMapping(value = "ocorrencia/{uuid}/deslocamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra um passo do deslocamento", description = "Envio, chegada ao local, saída do local e chegada ao destino, um de cada e "
            + "na ordem (422); repetido: 409. A hora, se informada, fica entre o último passo e agora.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<OcorrenciaUrgenciaDto> deslocamento(@PathVariable UUID uuid, @Valid @RequestBody DeslocamentoUrgenciaRequestDto dto) {
        return ResponseEntity.ok(service.deslocamento(uuid, dto));
    }

    @RequerPermissao({"ATENDIMENTO.GERENCIAR"})
    @PostMapping(value = "ocorrencia/{uuid}/recebimento", produces = JSON, consumes = JSON)
    @Operation(summary = "Recebe o paciente no destino", description = "Pela unidade de destino. Com recurso, depois da saída do local; fecha a ocorrência "
            + "como recebida no destino.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<OcorrenciaUrgenciaDto> receber(@PathVariable UUID uuid, @Valid @RequestBody RecebimentoUrgenciaRequestDto dto) {
        return ResponseEntity.ok(service.receber(uuid, dto));
    }

    @RequerPermissao({"URGENCIA.REGULAR"})
    @PostMapping(value = "ocorrencia/{uuid}/encerramento", produces = JSON, consumes = JSON)
    @Operation(summary = "Encerra com o desfecho", description = "Atendido ou óbito no local pedem a chegada ao local; antes da regulação, só sem "
            + "ocorrência ou desistência (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<OcorrenciaUrgenciaDto> encerrar(@PathVariable UUID uuid, @Valid @RequestBody EncerramentoUrgenciaRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(uuid, dto));
    }

    @RequerPermissao({"URGENCIA.RECEBER", "URGENCIA.REGULAR"})
    @PostMapping(value = "ocorrencia/{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a ocorrência", description = "Duplicada ou por engano, antes do envio do recurso (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<OcorrenciaUrgenciaDto> cancelar(@PathVariable UUID uuid, @Valid @RequestBody MotivoUrgenciaRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto));
    }

    @RequerPermissao({"URGENCIA.RECEBER", "URGENCIA.REGULAR", "ATENDIMENTO.GERENCIAR"})
    @PostMapping(value = "ocorrencia/{uuid}/paciente", produces = JSON, consumes = JSON)
    @Operation(summary = "Identifica o paciente", description = "Pela central ou pela unidade de destino; já identificado: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<OcorrenciaUrgenciaDto> paciente(@PathVariable UUID uuid, @Valid @RequestBody VinculoPacienteUrgenciaRequestDto dto) {
        return ResponseEntity.ok(service.vincularPaciente(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"URGENCIA.CONSULTAR", "URGENCIA.RECEBER", "URGENCIA.REGULAR"})
    @GetMapping(value = "painel", produces = JSON)
    @Operation(summary = "Painel da central", description = "Abertas, aguardando regulação primeiro e depois por gravidade e espera, com os números do dia. "
            + "Quem só consulta vê o andamento, sem dado clínico. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<PainelUrgenciaDto> painel(@RequestParam(required = false) UUID centralId) {
        return ResponseEntity.ok(service.painel(centralId));
    }

    @AuditarLeitura
    @RequerPermissao({"URGENCIA.CONSULTAR", "URGENCIA.RECEBER", "URGENCIA.REGULAR", "ATENDIMENTO.GERENCIAR"})
    @GetMapping(value = "ocorrencia", produces = JSON)
    @Operation(summary = "Ocorrências dos últimos dias", description = "Da central, de destino ou pedidas pelas unidades do escopo. Filtros: centralId, "
            + "situacao, destinoId, dias (7, até 90). Leitura auditada.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<OcorrenciaUrgenciaDto>> listar(@RequestParam(required = false) UUID centralId,
                                                              @RequestParam(required = false) SituacaoOcorrenciaUrgencia situacao,
                                                              @RequestParam(required = false) UUID destinoId,
                                                              @RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(service.listar(centralId, situacao, destinoId, dias));
    }

    @AuditarLeitura
    @RequerPermissao({"URGENCIA.CONSULTAR", "URGENCIA.RECEBER", "URGENCIA.REGULAR", "ATENDIMENTO.GERENCIAR"})
    @GetMapping(value = "ocorrencia/{uuid}", produces = JSON)
    @Operation(summary = "A ocorrência", description = "Com a linha do tempo; fora do escopo: 403. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<OcorrenciaUrgenciaDto> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscar(uuid));
    }

    @RequerPermissao({"URGENCIA.CONSULTAR", "URGENCIA.RECEBER", "URGENCIA.REGULAR"})
    @GetMapping(value = "destinos", produces = JSON)
    @Operation(summary = "Destinos possíveis", description = "Unidades com perfil Urgência vigente, com os leitos livres e quantas ocorrências já vão para "
            + "elas.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<DestinoUrgenciaDto>> destinos() {
        return ResponseEntity.ok(service.destinos());
    }

    @RequerPermissao({"URGENCIA.CONSULTAR", "URGENCIA.RECEBER", "URGENCIA.REGULAR", "SAUDE_MENTAL.CRISE"})
    @GetMapping(value = "centrais", produces = JSON)
    @Operation(summary = "Centrais de regulação", description = "Unidades com a capacidade de regulação médica de urgência vigente.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CentralUrgenciaDto>> centrais() {
        return ResponseEntity.ok(service.centrais());
    }
}
