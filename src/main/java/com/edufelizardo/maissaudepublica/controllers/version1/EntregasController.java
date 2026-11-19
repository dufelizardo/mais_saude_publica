package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.OcorrenciaFornecedorRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.OrdemFornecimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RecebimentoCompraRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RecebimentoDefinitivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SancaoFornecedorRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EntregasDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaFornecedor;
import com.edufelizardo.maissaudepublica.services.version1.EntregasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/compras/")
@Tag(name = "Entregas", description = "Ordens de fornecimento, recebimento, ocorrências com o fornecedor e o painel de Compras (ver a ADR-0214).")
public class EntregasController {

    private static final String TAG = "Entregas";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private EntregasService service;

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "instrumento/{uuid}/ordem", produces = JSON, consumes = JSON)
    @Operation(summary = "Emite a ordem de fornecimento", description = "Instrumento vigente e itens dentro do saldo (422); o saldo fica reservado.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EntregasDtos.Ordem> emitir(@PathVariable UUID uuid, @Valid @RequestBody OrdemFornecimentoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.emitir(uuid, dto));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "instrumento/{uuid}/ordens", produces = JSON)
    @Operation(summary = "Ordens da ata ou do contrato", description = "Para quem compra e para o gestor e o fiscal (403 aos outros).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<EntregasDtos.Ordem>> ordens(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.ordensDo(uuid));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "ordem/{uuid}", produces = JSON)
    @Operation(summary = "Ordem de fornecimento", description = "Com os itens, os recebimentos e as ocorrências.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<EntregasDtos.Ordem> ordem(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.ordem(uuid));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "ordem/{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a ordem", description = "Aberta e sem recebimento provisório pendente (422); o não aceito volta ao saldo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EntregasDtos.Ordem> cancelar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto.getMotivo()));
    }

    @LiberadoParaAutenticados
    @PostMapping(value = "ordem/{uuid}/recebimento", produces = JSON, consumes = JSON)
    @Operation(summary = "Recebimento provisório", description = "Pelo fiscal ou por quem compra (403): nota fiscal, quantidades até o pendente, lote e validade.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EntregasDtos.Ordem> receber(@PathVariable UUID uuid, @Valid @RequestBody RecebimentoCompraRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.receber(uuid, dto));
    }

    @LiberadoParaAutenticados
    @PostMapping(value = "recebimento/{uuid}/definitivo", produces = JSON, consumes = JSON)
    @Operation(summary = "Recebimento definitivo", description = "O aceito de cada item entra no Estoque ou na Farmácia; o recusado pede o motivo e vira ocorrência.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EntregasDtos.Ordem> definitivo(@PathVariable UUID uuid, @Valid @RequestBody RecebimentoDefinitivoRequestDto dto) {
        return ResponseEntity.ok(service.definitivo(uuid, dto));
    }

    @LiberadoParaAutenticados
    @PostMapping(value = "ordem/{uuid}/ocorrencia", produces = JSON, consumes = JSON)
    @Operation(summary = "Ocorrência com o fornecedor", description = "Pelo fiscal ou por quem compra; o atraso nasce sozinho (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EntregasDtos.Ordem> ocorrencia(@PathVariable UUID uuid, @Valid @RequestBody OcorrenciaFornecedorRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarOcorrencia(uuid, dto));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @GetMapping(value = "ocorrencias", produces = JSON)
    @Operation(summary = "Ocorrências com fornecedores", description = "Com os atrasos de hoje já registrados.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<EntregasDtos.Ocorrencia>> ocorrencias(@RequestParam(required = false) SituacaoOcorrenciaFornecedor situacao) {
        return ResponseEntity.ok(service.ocorrencias(situacao));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "ocorrencia/{uuid}/encerramento", produces = JSON, consumes = JSON)
    @Operation(summary = "Encerra a ocorrência", description = "Aberta (422), com a resposta do fornecedor.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EntregasDtos.Ocorrencia> encerrar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(uuid, dto.getMotivo()));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @PostMapping(value = "ocorrencia/{uuid}/sancao", produces = JSON, consumes = JSON)
    @Operation(summary = "Converte a ocorrência em sanção", description = "Aberta (422); a sanção segue as regras do art. 156.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EntregasDtos.Ocorrencia> sancionar(@PathVariable UUID uuid, @Valid @RequestBody SancaoFornecedorRequestDto dto) {
        return ResponseEntity.ok(service.sancionar(uuid, dto));
    }

    @RequerPermissao({"COMPRAS.GERENCIAR"})
    @GetMapping(value = "painel", produces = JSON)
    @Operation(summary = "Painel de Compras", description = "Andamento, ordens atrasadas, contratos vencendo e desempenho dos fornecedores; até 1 ano (400).",
            tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<EntregasDtos.Painel> painel(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.painel(inicio, fim));
    }
}
