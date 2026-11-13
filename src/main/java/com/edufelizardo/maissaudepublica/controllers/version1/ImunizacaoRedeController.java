package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DecisaoSuspensaoLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRemanejamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RecebimentoRemanejamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RemanejamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RevogacaoContraindicacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SuspensaoLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CadernetaVacinacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ImunizacaoRedeDtos;
import com.edufelizardo.maissaudepublica.services.version1.LoteRedeService;
import com.edufelizardo.maissaudepublica.services.version1.RemanejamentoVacinaService;
import com.edufelizardo.maissaudepublica.services.version1.VacinacaoService;
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
@RequestMapping(value = "/api/v1/")
@Tag(name = "Imunização na rede", description = "Remanejamento entre salas de vacina, alerta de ESAVI por lote com a suspensão na rede e a "
        + "revogação da contraindicação pelo CRIE (ver docs/adr/0206-imunizacao-remanejamento-lote-rede-crie.md).")
public class ImunizacaoRedeController {

    private static final String TAG = "Imunização na rede";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired private RemanejamentoVacinaService remanejamentoService;
    @Autowired private LoteRedeService loteRedeService;
    @Autowired private VacinacaoService vacinacaoService;

    // ── Remanejamento ──

    @RequerPermissao({"VACINACAO.REGISTRAR", "IMUNIZACAO.GERENCIAR"})
    @GetMapping(value = "sala-de-vacina/remanejamentos", produces = JSON)
    @Operation(summary = "Remanejamentos da unidade", description = "Os enviados e os recebidos (ou a receber) pela sala de vacina.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ImunizacaoRedeDtos.Remanejamento>> remanejamentos(@RequestParam UUID unidadeId) {
        return ResponseEntity.ok(remanejamentoService.daUnidade(unidadeId));
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PostMapping(value = "sala-de-vacina/remanejamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Envia o remanejamento", description = "Lote disponível e na validade, doses fora dos frascos abertos (422), destino com "
            + "sala de vacina (422); as doses saem do lote e ficam em trânsito.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ImunizacaoRedeDtos.Remanejamento> enviar(@Valid @RequestBody RemanejamentoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(remanejamentoService.enviar(dto));
    }

    @RequerPermissao({"VACINACAO.REGISTRAR"})
    @PostMapping(value = "remanejamento-imunobiologico/{uuid}/recebimento", produces = JSON, consumes = JSON)
    @Operation(summary = "Recebe o remanejamento", description = "Com a temperatura da caixa: fora de 2 a 8 °C, o lote chega sob suspeita.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ImunizacaoRedeDtos.Remanejamento> receber(@PathVariable UUID uuid, @Valid @RequestBody RecebimentoRemanejamentoRequestDto dto) {
        return ResponseEntity.ok(remanejamentoService.receber(uuid, dto));
    }

    @RequerPermissao({"VACINACAO.REGISTRAR"})
    @PostMapping(value = "remanejamento-imunobiologico/{uuid}/recusa", produces = JSON, consumes = JSON)
    @Operation(summary = "Recusa o remanejamento", description = "As doses voltam ao lote de origem.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ImunizacaoRedeDtos.Remanejamento> recusar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRemanejamentoRequestDto dto) {
        return ResponseEntity.ok(remanejamentoService.recusar(uuid, dto));
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PostMapping(value = "remanejamento-imunobiologico/{uuid}/cancelamento", produces = JSON)
    @Operation(summary = "Cancela o remanejamento", description = "Pela origem, enquanto enviado; as doses voltam ao lote.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ImunizacaoRedeDtos.Remanejamento> cancelar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(remanejamentoService.cancelar(uuid));
    }

    // ── Lote na rede ──

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @GetMapping(value = "imunizacao/alertas-lote", produces = JSON)
    @Operation(summary = "Alertas de ESAVI por lote", description = "Dois ou mais ESAVI graves em 30 dias, ou um com causalidade A2 no último ano, e os "
            + "lotes suspensos na rede; sem o paciente.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ImunizacaoRedeDtos.AlertaLote>> alertas() {
        return ResponseEntity.ok(loteRedeService.alertas());
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @GetMapping(value = "imunizacao/suspensoes-lote", produces = JSON)
    @Operation(summary = "Suspensões de lote na rede", description = "O histórico, da mais recente.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ImunizacaoRedeDtos.Suspensao>> suspensoes() {
        return ResponseEntity.ok(loteRedeService.suspensoes());
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PostMapping(value = "imunizacao/suspensao-lote", produces = JSON, consumes = JSON)
    @Operation(summary = "Suspende o lote na rede", description = "Da coordenação da rede (403 a quem gerencia só algumas unidades); uma vigente "
            + "por lote (409); todos os lotes com o número ficam sob suspeita.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ImunizacaoRedeDtos.Suspensao> suspender(@Valid @RequestBody SuspensaoLoteRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loteRedeService.suspender(dto));
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PostMapping(value = "imunizacao/suspensao-lote/{uuid}/decisao", produces = JSON, consumes = JSON)
    @Operation(summary = "Decide a suspensão", description = "LIBERAR (os lotes suspensos só pelo ESAVI voltam) ou INUTILIZAR (perda em cada sala).",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ImunizacaoRedeDtos.Suspensao> decidir(@PathVariable UUID uuid, @Valid @RequestBody DecisaoSuspensaoLoteRequestDto dto) {
        return ResponseEntity.ok(loteRedeService.decidir(uuid, dto));
    }

    // ── CRIE ──

    @RequerPermissao({"IMUNIZACAO.REAVALIAR"})
    @PostMapping(value = "contraindicacao-vacinal/{uuid}/revogacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Revoga a contraindicação vacinal", description = "Pela reavaliação no CRIE, com o parecer; devolve a caderneta.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadernetaVacinacaoDto> revogar(@PathVariable UUID uuid, @Valid @RequestBody RevogacaoContraindicacaoRequestDto dto) {
        return ResponseEntity.ok(vacinacaoService.revogarContraindicacao(uuid, dto));
    }
}
