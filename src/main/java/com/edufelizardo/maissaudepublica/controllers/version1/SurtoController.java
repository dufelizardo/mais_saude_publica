package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CasosSurtoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MedidaControleRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoSurtoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SurtoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MedidaControleDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SurtoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SurtosDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSurto;
import com.edufelizardo.maissaudepublica.services.version1.SurtoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/vigilancia/")
@Tag(name = "Surtos e medidas de controle", description = "Surto (casos do mesmo agravo num território ou estabelecimento), medidas de controle "
        + "do surto ou do caso e a linha do tempo (ver docs/adr/0131-vigilancia-surto-e-medidas.md).")
public class SurtoController {

    private static final String TAG = "Surtos e medidas de controle";

    @Autowired
    private SurtoService service;

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @GetMapping(value = "surtos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Surtos", description = "Os do escopo (os de agravo sensível só para a vigilância) e os indicadores: abertos, "
            + "confirmados e medidas com prazo vencido.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<SurtosDto> surtos(@RequestParam(required = false) UUID unidadeId, @RequestParam(required = false) UUID agravoId,
                                            @RequestParam(required = false) SituacaoSurto situacao) {
        return ResponseEntity.ok(service.surtos(unidadeId, agravoId, situacao));
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @GetMapping(value = "surto/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Detalhe do surto", description = "Casos, microáreas, medidas, curva por semana epidemiológica e linha do tempo.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<SurtoDto> surto(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.surto(uuid));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "surto", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Abre o surto", description = "Suspeito, já com os casos informados (do mesmo agravo: 422; em outro surto: 409). "
            + "Institucional pede o estabelecimento (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SurtoDto> abrir(@Valid @RequestBody SurtoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(dto));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PutMapping(value = "surto/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza o surto", description = "O agravo e a unidade não mudam (422). Encerrado: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SurtoDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody SurtoRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "surto/{uuid}/situacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Muda a situação do surto", description = "Encerrar pede o surto confirmado ou descartado (422), a conclusão (400) e "
            + "nenhuma medida em aberto (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SurtoDto> situacao(@PathVariable UUID uuid, @Valid @RequestBody SituacaoSurtoRequestDto dto) {
        return ResponseEntity.ok(service.mudarSituacao(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "surto/{uuid}/casos", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui casos no surto", description = "Do mesmo agravo (422); cada caso num surto só (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SurtoDto> incluir(@PathVariable UUID uuid, @Valid @RequestBody CasosSurtoRequestDto dto) {
        return ResponseEntity.ok(service.incluirCasos(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @DeleteMapping(value = "surto/{uuid}/casos/{notificacaoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retira um caso do surto", description = "A notificação continua; só sai do surto.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SurtoDto> retirar(@PathVariable UUID uuid, @PathVariable UUID notificacaoId) {
        return ResponseEntity.ok(service.retirarCaso(uuid, notificacaoId));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "surto/{uuid}/medida", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Medida de controle do surto", description = "Planejada, salvo outra situação informada.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<MedidaControleDto> medidaDoSurto(@PathVariable UUID uuid, @Valid @RequestBody MedidaControleRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.medidaDoSurto(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "notificacao/{uuid}/medida", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Medida de controle do caso", description = "Sem surto (ex.: bloqueio vacinal). Notificação encerrada: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<MedidaControleDto> medidaDoCaso(@PathVariable UUID uuid, @Valid @RequestBody MedidaControleRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.medidaDoCaso(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @PatchMapping(value = "medida/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza a medida", description = "Concluir pede o resultado (400); concluída ou cancelada não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<MedidaControleDto> medida(@PathVariable UUID uuid, @Valid @RequestBody MedidaControleRequestDto dto) {
        return ResponseEntity.ok(service.atualizarMedida(uuid, dto));
    }
}
