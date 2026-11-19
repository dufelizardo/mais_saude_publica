package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.InventarioDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PatrimonioDtos;
import com.edufelizardo.maissaudepublica.services.version1.InventarioPatrimonioService;
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
@RequestMapping(value = "/api/v1/patrimonio/")
@Tag(name = "Inventário e baixa", description = "Inventário anual, baixa de bens e painel do Patrimônio (ver a ADR-0217).")
public class InventarioPatrimonioController {

    private static final String TAG = "Inventário e baixa";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private InventarioPatrimonioService service;

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "inventario", produces = JSON, consumes = JSON)
    @Operation(summary = "Abre o inventário", description = "Comissão de três profissionais diferentes (400), ativos (422); um aberto por unidade (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<InventarioDtos.Inventario> abrir(@Valid @RequestBody InventarioPatrimonioRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(dto));
    }

    @RequerPermissao({"PATRIMONIO.CONSULTAR", "PATRIMONIO.GERENCIAR"})
    @GetMapping(value = "inventarios", produces = JSON)
    @Operation(summary = "Inventários", description = "Das unidades visíveis.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<InventarioDtos.Inventario>> inventarios(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.inventarios(unidadeId));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "meus-inventarios", produces = JSON)
    @Operation(summary = "Meus inventários", description = "Os inventários em que quem está logado é da comissão.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<InventarioDtos.Inventario>> meus() {
        return ResponseEntity.ok(service.meus());
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "inventario/{uuid}", produces = JSON)
    @Operation(summary = "Inventário", description = "Com os itens e os achados; para a comissão ou para quem vê a unidade (403 aos outros).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<InventarioDtos.Inventario> inventario(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.inventario(uuid));
    }

    @LiberadoParaAutenticados
    @PostMapping(value = "inventario-item/{uuid}/conferencia", produces = JSON, consumes = JSON)
    @Operation(summary = "Confere o bem", description = "Pela comissão ou pela gestão da unidade; inventário fechado (422); conservação e local (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<InventarioDtos.Inventario> conferir(@PathVariable UUID uuid, @Valid @RequestBody ConferenciaInventarioRequestDto dto) {
        return ResponseEntity.ok(service.conferir(uuid, dto));
    }

    @LiberadoParaAutenticados
    @PostMapping(value = "inventario/{uuid}/achado", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra o bem a mais", description = "Achado fora da lista; pela comissão ou pela gestão; inventário fechado (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<InventarioDtos.Inventario> registrarAchado(@PathVariable UUID uuid, @Valid @RequestBody AchadoInventarioRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarAchado(uuid, dto));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "inventario/{uuid}/fechamento", produces = JSON)
    @Operation(summary = "Fecha o inventário", description = "Tudo conferido (422); corrige o setor, abre a transferência e sugere a baixa.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<InventarioDtos.Inventario> fechar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.fechar(uuid));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "bem/{uuid}/baixa", produces = JSON, consumes = JSON)
    @Operation(summary = "Pede a baixa", description = "Sem ordem de serviço nem transferência aberta (422); acima de R$ 10 mil, espera a confirmação.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Baixa> pedirBaixa(@PathVariable UUID uuid, @Valid @RequestBody BaixaBemRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.pedirBaixa(uuid, dto));
    }

    @RequerPermissao({"PATRIMONIO.CONSULTAR", "PATRIMONIO.GERENCIAR", "PATRIMONIO.CONFIRMAR_BAIXA"})
    @GetMapping(value = "baixas", produces = JSON)
    @Operation(summary = "Baixas", description = "Das unidades visíveis; pendentes deixa só as que esperam a confirmação.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PatrimonioDtos.Baixa>> baixas(@RequestParam(required = false) Boolean pendentes) {
        return ResponseEntity.ok(service.baixas(pendentes));
    }

    @RequerPermissao({"PATRIMONIO.CONFIRMAR_BAIXA"})
    @PostMapping(value = "baixa/{uuid}/confirmacao", produces = JSON)
    @Operation(summary = "Confirma a baixa", description = "Só a pendente (422); quem pediu não confirma (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Baixa> confirmar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.confirmarBaixa(uuid));
    }

    @RequerPermissao({"PATRIMONIO.CONFIRMAR_BAIXA"})
    @PostMapping(value = "baixa/{uuid}/recusa", produces = JSON, consumes = JSON)
    @Operation(summary = "Recusa a baixa", description = "Só a pendente (422), com o motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Baixa> recusar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.recusarBaixa(uuid, dto));
    }

    @RequerPermissao({"PATRIMONIO.CONSULTAR", "PATRIMONIO.GERENCIAR"})
    @GetMapping(value = "painel", produces = JSON)
    @Operation(summary = "Painel do Patrimônio", description = "Bens e valores por unidade e classe, inventários, baixas e manutenção.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<InventarioDtos.Painel> painel() {
        return ResponseEntity.ok(service.painel());
    }
}
