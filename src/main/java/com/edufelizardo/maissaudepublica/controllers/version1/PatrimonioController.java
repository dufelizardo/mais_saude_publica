package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PatrimonioDtos;
import com.edufelizardo.maissaudepublica.models.enuns.ClasseBem;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoBem;
import com.edufelizardo.maissaudepublica.services.version1.PatrimonioService;
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
@Tag(name = "Patrimônio", description = "Bens patrimoniais, tombamento, responsáveis e transferências (ver a ADR-0215).")
public class PatrimonioController {

    private static final String TAG = "Patrimônio";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private PatrimonioService service;

    @RequerPermissao({"PATRIMONIO.CONSULTAR", "PATRIMONIO.GERENCIAR"})
    @GetMapping(value = "bens", produces = JSON)
    @Operation(summary = "Bens", description = "Das unidades visíveis; filtra por unidade, classe e situação, e busca no tombamento, na descrição e no número de série.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PatrimonioDtos.Bem>> bens(@RequestParam(required = false) UUID unidadeId, @RequestParam(required = false) ClasseBem classe,
                                                         @RequestParam(required = false) SituacaoBem situacao, @RequestParam(required = false) String busca) {
        return ResponseEntity.ok(service.bens(unidadeId, classe, situacao, busca));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "meus-bens", produces = JSON)
    @Operation(summary = "Meus bens", description = "Os bens sob a responsabilidade de quem está logado.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PatrimonioDtos.Bem>> meus() {
        return ResponseEntity.ok(service.meus());
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "bem/{uuid}", produces = JSON)
    @Operation(summary = "Ficha do bem", description = "Com os responsáveis e as transferências; para quem vê a unidade ou para o responsável atual (403 aos outros).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<PatrimonioDtos.Detalhe> bem(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.bem(uuid));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "bem", produces = JSON, consumes = JSON)
    @Operation(summary = "Cadastra o bem", description = "Doação, cessão, transferência de outro ente ou acervo antigo: já tombado, com o responsável. Aquisição futura (400); setor de outra unidade (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Detalhe> cadastrar(@Valid @RequestBody BemPatrimonialRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(dto));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PutMapping(value = "bem/{uuid}", produces = JSON, consumes = JSON)
    @Operation(summary = "Corrige o bem", description = "Descrição, identificação, setor e observação; o bem baixado não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Detalhe> editar(@PathVariable UUID uuid, @Valid @RequestBody EdicaoBemRequestDto dto) {
        return ResponseEntity.ok(service.editar(uuid, dto));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "bem/{uuid}/tombamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Tomba o bem comprado", description = "Só o bem a tombar (422): número, classe, identificação, setor e responsável.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Detalhe> tombar(@PathVariable UUID uuid, @Valid @RequestBody TombamentoBemRequestDto dto) {
        return ResponseEntity.ok(service.tombar(uuid, dto));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "bem/{uuid}/responsavel", produces = JSON, consumes = JSON)
    @Operation(summary = "Troca o responsável", description = "O termo anterior se encerra; a mesma pessoa (422); bem a tombar ou baixado (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Detalhe> trocarResponsavel(@PathVariable UUID uuid, @Valid @RequestBody ResponsavelBemRequestDto dto) {
        return ResponseEntity.ok(service.trocarResponsavel(uuid, dto));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "bem/{uuid}/situacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Em uso ou ocioso", description = "Outras situações (400); bem a tombar, em manutenção ou baixado (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Detalhe> mudarSituacao(@PathVariable UUID uuid, @Valid @RequestBody SituacaoBemRequestDto dto) {
        return ResponseEntity.ok(service.mudarSituacao(uuid, dto));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "bem/{uuid}/veiculo", produces = JSON, consumes = JSON)
    @Operation(summary = "Liga o bem ao veículo da frota", description = "Só da classe veículo (422); veículo de outro bem (409) ou de outra unidade (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Detalhe> vincularVeiculo(@PathVariable UUID uuid, @Valid @RequestBody VeiculoDoBemRequestDto dto) {
        return ResponseEntity.ok(service.vincularVeiculo(uuid, dto));
    }

    @RequerPermissao({"PATRIMONIO.CONSULTAR", "PATRIMONIO.GERENCIAR"})
    @GetMapping(value = "transferencias", produces = JSON)
    @Operation(summary = "Transferências", description = "Das unidades visíveis, na origem ou no destino; pendentes deixa só as que esperam o aceite.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PatrimonioDtos.Transferencia>> transferencias(@RequestParam(required = false) Boolean pendentes) {
        return ResponseEntity.ok(service.transferencias(pendentes));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "bem/{uuid}/transferencia", produces = JSON, consumes = JSON)
    @Operation(summary = "Envia o bem para outra unidade", description = "Bem em uso ou ocioso (422); destino igual à origem (400); uma transferência por vez (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Detalhe> transferir(@PathVariable UUID uuid, @Valid @RequestBody TransferenciaBemRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.transferir(uuid, dto));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "transferencia/{uuid}/aceite", produces = JSON, consumes = JSON)
    @Operation(summary = "Aceita o bem", description = "Pelo destino: muda a unidade, o setor (do destino, 422) e o responsável. Só a pendente (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Detalhe> aceitar(@PathVariable UUID uuid, @Valid @RequestBody AceiteTransferenciaBemRequestDto dto) {
        return ResponseEntity.ok(service.aceitar(uuid, dto));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "transferencia/{uuid}/recusa", produces = JSON, consumes = JSON)
    @Operation(summary = "Recusa o bem", description = "Pelo destino, com o motivo; o bem fica na origem. Só a pendente (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Detalhe> recusar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.recusar(uuid, dto));
    }

    @RequerPermissao({"PATRIMONIO.GERENCIAR"})
    @PostMapping(value = "transferencia/{uuid}/cancelamento", produces = JSON)
    @Operation(summary = "Cancela o envio", description = "Pela origem. Só a pendente (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PatrimonioDtos.Detalhe> cancelar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.cancelar(uuid));
    }
}
