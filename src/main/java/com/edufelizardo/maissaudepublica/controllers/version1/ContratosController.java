package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AditivoContratualRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ContratacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.HomologacaoContratacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResultadoItemContratacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ContratosDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoContratacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoInstrumentoContratual;
import com.edufelizardo.maissaudepublica.services.version1.ContratosService;
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
@RequestMapping(value = "/api/v1/compras/")
@Tag(name = "Contratos", description = "Contratação, atas de registro de preços, contratos e aditivos (ver a ADR-0213).")
public class ContratosController {

    private static final String TAG = "Contratos";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private ContratosService service;

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @GetMapping(value = "contratacoes", produces = JSON)
    @Operation(summary = "Contratações", description = "Com os itens, o resultado e os instrumentos gerados.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ContratosDtos.Contratacao>> contratacoes(@RequestParam(required = false) SituacaoContratacao situacao) {
        return ResponseEntity.ok(service.contratacoes(situacao));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @GetMapping(value = "contratacao/{uuid}", produces = JSON)
    @Operation(summary = "Contratação", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ContratosDtos.Contratacao> contratacao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.contratacao(uuid));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "contratacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Abre a contratação", description = "Das solicitações cotadas (422), fora de outra em andamento (409); processo único (409).",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ContratosDtos.Contratacao> abrir(@Valid @RequestBody ContratacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(dto));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "item-contratacao/{uuid}/resultado", produces = JSON, consumes = JSON)
    @Operation(summary = "Resultado do item", description = "Adjudicado a fornecedor regular (422), fracassado ou deserto; acima da referência, justificativa.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ContratosDtos.Contratacao> resultado(@PathVariable UUID uuid, @Valid @RequestBody ResultadoItemContratacaoRequestDto dto) {
        return ResponseEntity.ok(service.resultado(uuid, dto));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "contratacao/{uuid}/homologacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Homologa", description = "Uma ata ou um contrato por fornecedor vencedor, com gestor e fiscal diferentes.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ContratosDtos.Contratacao> homologar(@PathVariable UUID uuid, @Valid @RequestBody HomologacaoContratacaoRequestDto dto) {
        return ResponseEntity.ok(service.homologar(uuid, dto));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "contratacao/{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a contratação", description = "Em andamento (422); as solicitações voltam a cotadas.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ContratosDtos.Contratacao> cancelar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto.getMotivo()));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @GetMapping(value = "instrumentos", produces = JSON)
    @Operation(summary = "Atas e contratos", description = "Com o saldo, os aditivos e os alertas; comAlerta filtra.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ContratosDtos.Instrumento>> instrumentos(@RequestParam(required = false) TipoInstrumentoContratual tipo,
                                                                        @RequestParam(required = false) Boolean comAlerta) {
        return ResponseEntity.ok(service.instrumentos(tipo, comAlerta));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "meus-contratos", produces = JSON)
    @Operation(summary = "Meus contratos", description = "As atas e os contratos em que a pessoa logada é o gestor ou o fiscal.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ContratosDtos.Instrumento>> meus() {
        return ResponseEntity.ok(service.meus());
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "instrumento/{uuid}", produces = JSON)
    @Operation(summary = "Ata ou contrato", description = "Para quem compra ou para o gestor e o fiscal designados (403 aos outros).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ContratosDtos.Instrumento> instrumento(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.instrumento(uuid));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "instrumento/{uuid}/aditivo", produces = JSON, consumes = JSON)
    @Operation(summary = "Termo aditivo", description = "Prazo (ata uma vez, até 2 anos; contínuo até 10), acréscimo e supressão até 25% (só contrato), "
            + "reequilíbrio.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ContratosDtos.Instrumento> aditar(@PathVariable UUID uuid, @Valid @RequestBody AditivoContratualRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.aditar(uuid, dto));
    }
}
