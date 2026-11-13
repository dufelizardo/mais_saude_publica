package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoQualidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AnaliseCausaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AndamentoAcaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NaoConformidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VerificacaoEficaciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NaoConformidadeDtos;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemNaoConformidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNaoConformidade;
import com.edufelizardo.maissaudepublica.services.version1.NaoConformidadeService;
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
@RequestMapping(value = "/api/v1/qualidade/")
@Tag(name = "Não conformidades", description = "Não conformidades, planos de ação 5W2H e verificação de eficácia (ver a ADR-0204).")
public class NaoConformidadeController {

    private static final String TAG = "Não conformidades";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private NaoConformidadeService service;

    @RequerPermissao({"QUALIDADE.GERIR"})
    @GetMapping(value = "nao-conformidades", produces = JSON)
    @Operation(summary = "Não conformidades", description = "Das unidades em que se gere; unidadeId, situacao e origem filtram.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<NaoConformidadeDtos.NaoConformidade>> listar(@RequestParam(required = false) UUID unidadeId,
                                                                          @RequestParam(required = false) SituacaoNaoConformidade situacao,
                                                                          @RequestParam(required = false) OrigemNaoConformidade origem) {
        return ResponseEntity.ok(service.listar(unidadeId, situacao, origem));
    }

    @RequerPermissao({"QUALIDADE.GERIR"})
    @GetMapping(value = "nao-conformidade/{uuid}", produces = JSON)
    @Operation(summary = "Não conformidade", description = "Com as ações de todos os ciclos e as verificações.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<NaoConformidadeDtos.NaoConformidade> detalhe(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.detalhe(uuid));
    }

    @RequerPermissao({"QUALIDADE.GERIR"})
    @PostMapping(value = "nao-conformidade", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a não conformidade", description = "A origem com o vínculo (400), da mesma unidade (422), sem outra em aberto (409).",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NaoConformidadeDtos.NaoConformidade> registrar(@Valid @RequestBody NaoConformidadeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @RequerPermissao({"QUALIDADE.GERIR"})
    @PostMapping(value = "nao-conformidade/{uuid}/analise", produces = JSON, consumes = JSON)
    @Operation(summary = "Análise de causa", description = "Com as categorias dos 6M.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NaoConformidadeDtos.NaoConformidade> analisar(@PathVariable UUID uuid, @Valid @RequestBody AnaliseCausaRequestDto dto) {
        return ResponseEntity.ok(service.analisar(uuid, dto));
    }

    @RequerPermissao({"QUALIDADE.GERIR"})
    @PostMapping(value = "nao-conformidade/{uuid}/acao", produces = JSON, consumes = JSON)
    @Operation(summary = "Ação do plano 5W2H", description = "No ciclo corrente; põe em tratamento.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NaoConformidadeDtos.NaoConformidade> adicionarAcao(@PathVariable UUID uuid, @Valid @RequestBody AcaoQualidadeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionarAcao(uuid, dto));
    }

    @RequerPermissao({"QUALIDADE.GERIR", "QUALIDADE.NOTIFICAR"})
    @PostMapping(value = "acao/{uuid}/andamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Andamento da ação", description = "Pela gestão ou pelo próprio responsável (403 aos outros); concluir pede a evidência.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NaoConformidadeDtos.Acao> andamento(@PathVariable UUID uuid, @Valid @RequestBody AndamentoAcaoRequestDto dto) {
        return ResponseEntity.ok(service.andamento(uuid, dto));
    }

    @RequerPermissao({"QUALIDADE.GERIR"})
    @PostMapping(value = "acao/{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a ação", description = "Só aberta (422), com o motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NaoConformidadeDtos.NaoConformidade> cancelarAcao(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.cancelarAcao(uuid, dto.getMotivo()));
    }

    @RequerPermissao({"QUALIDADE.GERIR"})
    @PostMapping(value = "nao-conformidade/{uuid}/verificacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Verificação de eficácia", description = "Eficaz encerra; não eficaz abre o ciclo seguinte.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NaoConformidadeDtos.NaoConformidade> verificar(@PathVariable UUID uuid, @Valid @RequestBody VerificacaoEficaciaRequestDto dto) {
        return ResponseEntity.ok(service.verificar(uuid, dto));
    }

    @RequerPermissao({"QUALIDADE.GERIR"})
    @PostMapping(value = "nao-conformidade/{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a não conformidade", description = "Aberta (422), com o motivo; as ações abertas também.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NaoConformidadeDtos.NaoConformidade> cancelar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto.getMotivo()));
    }

    @RequerPermissao({"QUALIDADE.GERIR", "QUALIDADE.NOTIFICAR"})
    @GetMapping(value = "minhas-acoes", produces = JSON)
    @Operation(summary = "Minhas ações", description = "As ações abertas de quem está logado.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<NaoConformidadeDtos.MinhaAcao>> minhasAcoes() {
        return ResponseEntity.ok(service.minhasAcoes());
    }

    @RequerPermissao({"QUALIDADE.GERIR"})
    @GetMapping(value = "nao-conformidades/sugestoes", produces = JSON)
    @Operation(summary = "Sugestões", description = "Incidentes com dano moderado ou maior e irregularidades da VISA nas unidades, sem não conformidade.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<NaoConformidadeDtos.Sugestao>> sugestoes() {
        return ResponseEntity.ok(service.sugestoes());
    }

    @RequerPermissao({"QUALIDADE.GERIR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "nao-conformidades/painel", produces = JSON)
    @Operation(summary = "Painel das não conformidades", description = "Só totais: por origem, gravidade e situação, ações atrasadas, eficácia.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<NaoConformidadeDtos.Painel> painel(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.painel(unidadeId));
    }
}
