package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AdmissaoRegulacaoLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoSolicitacaoLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReservaLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SolicitacaoLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SolicitacaoLeitoDtos;
import com.edufelizardo.maissaudepublica.services.version1.RegulacaoLeitoService;
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

/** Central de regulação de leitos entre unidades (ADR-0184). */
@RestController
@RequestMapping(value = "/api/v1/regulacao-leito/")
@Tag(name = "Regulação de leitos", description = "Pedido de leito, fila da central, vagas compatíveis, reserva com prazo ou vaga zero, e a admissão ou recusa no destino (ver docs/adr/0184-regulacao-de-leitos.md).")
public class RegulacaoLeitoController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String TAG = "Regulação de leitos";

    @Autowired
    private RegulacaoLeitoService service;

    @RequerPermissao({"INTERNACAO.SOLICITAR_VAGA"})
    @PostMapping(produces = JSON, consumes = JSON)
    @Operation(summary = "Pede leito à central", description = "Na unidade de origem. Um pedido em andamento por paciente (409). Paciente inativo ou "
            + "internação de origem encerrada: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SolicitacaoLeitoDtos.Solicitacao> solicitar(@Valid @RequestBody SolicitacaoLeitoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.solicitar(dto));
    }

    @RequerPermissao({"INTERNACAO.SOLICITAR_VAGA"})
    @PostMapping(value = "{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela o pedido", description = "Pela origem, até a admissão, com motivo; desfaz a reserva.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SolicitacaoLeitoDtos.Solicitacao> cancelar(@PathVariable UUID uuid, @Valid @RequestBody MotivoSolicitacaoLeitoRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto));
    }

    @RequerPermissao({"LEITO.REGULAR"})
    @GetMapping(value = "fila", produces = JSON)
    @Operation(summary = "Fila da central de leitos", description = "Pedidos em andamento, por prioridade e hora do pedido, com a posição dos que aguardam.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<SolicitacaoLeitoDtos.Solicitacao>> fila() {
        return ResponseEntity.ok(service.fila());
    }

    @RequerPermissao({"LEITO.REGULAR"})
    @GetMapping(value = "{uuid}/vagas", produces = JSON)
    @Operation(summary = "Vagas compatíveis", description = "Leitos livres de outras unidades com o tipo pedido e o sexo e a faixa etária do paciente.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<SolicitacaoLeitoDtos.Vaga>> vagas(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.vagas(uuid));
    }

    @RequerPermissao({"LEITO.REGULAR"})
    @PostMapping(value = "{uuid}/reserva", produces = JSON, consumes = JSON)
    @Operation(summary = "Reserva o leito ou manda em vaga zero", description = "Leito livre compatível de outra unidade, por um prazo (padrão 6 h, até 24). "
            + "Sem leito: vaga zero, com unidade e justificativa (400). Incompatível ou fora da fila: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SolicitacaoLeitoDtos.Solicitacao> reservar(@PathVariable UUID uuid, @Valid @RequestBody ReservaLeitoRequestDto dto) {
        return ResponseEntity.ok(service.reservar(uuid, dto));
    }

    @RequerPermissao({"LEITO.REGULAR"})
    @PostMapping(value = "{uuid}/negacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Nega o pedido", description = "Com motivo, enquanto aguarda vaga (422 com reserva).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SolicitacaoLeitoDtos.Solicitacao> negar(@PathVariable UUID uuid, @Valid @RequestBody MotivoSolicitacaoLeitoRequestDto dto) {
        return ResponseEntity.ok(service.negar(uuid, dto));
    }

    @RequerPermissao({"INTERNACAO.GERENCIAR"})
    @GetMapping(value = "a-receber", produces = JSON)
    @Operation(summary = "Pacientes a receber", description = "Pedidos com reserva válida ou vaga zero para as unidades do escopo.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<SolicitacaoLeitoDtos.Solicitacao>> aReceber() {
        return ResponseEntity.ok(service.aReceber());
    }

    @RequerPermissao({"INTERNACAO.GERENCIAR"})
    @PostMapping(value = "{uuid}/admissao", produces = JSON, consumes = JSON)
    @Operation(summary = "Admite no destino", description = "Cria a internação no leito reservado (na vaga zero, num leito livre da unidade, 400 sem ele). "
            + "A internação da origem fecha por transferência. Reserva vencida: 422. Faixa etária: 422 sem justificativa.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SolicitacaoLeitoDtos.Solicitacao> admitir(@PathVariable UUID uuid, @Valid @RequestBody AdmissaoRegulacaoLeitoRequestDto dto) {
        return ResponseEntity.ok(service.admitir(uuid, dto));
    }

    @RequerPermissao({"INTERNACAO.GERENCIAR"})
    @PostMapping(value = "{uuid}/recusa", produces = JSON, consumes = JSON)
    @Operation(summary = "Recusa no destino", description = "Com motivo: o pedido volta à fila na posição original e o leito volta a livre.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SolicitacaoLeitoDtos.Solicitacao> recusar(@PathVariable UUID uuid, @Valid @RequestBody MotivoSolicitacaoLeitoRequestDto dto) {
        return ResponseEntity.ok(service.recusar(uuid, dto));
    }

    @RequerPermissao({"INTERNACAO.SOLICITAR_VAGA", "LEITO.REGULAR", "INTERNACAO.GERENCIAR"})
    @GetMapping(produces = JSON)
    @Operation(summary = "Pedidos de leito do paciente", description = "Do mais recente ao mais antigo, nas unidades do escopo.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<SolicitacaoLeitoDtos.Solicitacao>> doPaciente(@RequestParam UUID pacienteId) {
        return ResponseEntity.ok(service.doPaciente(pacienteId));
    }

    @RequerPermissao({"INTERNACAO.SOLICITAR_VAGA", "LEITO.REGULAR", "INTERNACAO.GERENCIAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}", produces = JSON)
    @Operation(summary = "Detalhe do pedido de leito", description = "Com os eventos. Leitura auditada. Fora do escopo da origem e do destino: 403.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<SolicitacaoLeitoDtos.Solicitacao> buscarPorId(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }
}
