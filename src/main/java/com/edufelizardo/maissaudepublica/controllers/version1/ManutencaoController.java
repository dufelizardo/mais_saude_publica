package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ManutencaoDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOrdemServico;
import com.edufelizardo.maissaudepublica.services.version1.ManutencaoService;
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
@Tag(name = "Manutenção", description = "Planos de manutenção, chamados e ordens de serviço dos bens (ver a ADR-0216).")
public class ManutencaoController {

    private static final String TAG = "Manutenção";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private ManutencaoService service;

    @RequerPermissao({"MANUTENCAO.EXECUTAR"})
    @PostMapping(value = "bem/{uuid}/plano", produces = JSON, consumes = JSON)
    @Operation(summary = "Cria o plano de manutenção", description = "Preventiva, calibração ou segurança elétrica (corretiva, 400); um ativo por tipo (409); bem a tombar ou baixado (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ManutencaoDtos.DoBem> criarPlano(@PathVariable UUID uuid, @Valid @RequestBody PlanoManutencaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarPlano(uuid, dto));
    }

    @RequerPermissao({"MANUTENCAO.EXECUTAR"})
    @PostMapping(value = "plano/{uuid}/desativacao", produces = JSON)
    @Operation(summary = "Desativa o plano", description = "O plano já inativo dá 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ManutencaoDtos.DoBem> desativarPlano(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.desativarPlano(uuid));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "bem/{uuid}/manutencao", produces = JSON)
    @Operation(summary = "Manutenção do bem", description = "Planos, ordens, custo acumulado, dias parado e calibração; para quem vê o bem ou para o responsável.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ManutencaoDtos.DoBem> doBem(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.doBemConsultado(uuid));
    }

    @LiberadoParaAutenticados
    @PostMapping(value = "bem/{uuid}/chamado", produces = JSON, consumes = JSON)
    @Operation(summary = "Abre o chamado", description = "Por quem está logado; bem a tombar ou baixado (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ManutencaoDtos.Ordem> abrirChamado(@PathVariable UUID uuid, @Valid @RequestBody ChamadoManutencaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrirChamado(uuid, dto));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "meus-chamados", produces = JSON)
    @Operation(summary = "Meus chamados", description = "Os chamados abertos por quem está logado.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ManutencaoDtos.Ordem>> meusChamados() {
        return ResponseEntity.ok(service.meusChamados());
    }

    @RequerPermissao({"MANUTENCAO.EXECUTAR"})
    @GetMapping(value = "ordens-servico", produces = JSON)
    @Operation(summary = "Ordens de serviço", description = "Das unidades visíveis, com as dos planos vencidos já geradas.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ManutencaoDtos.Ordem>> ordens(@RequestParam(required = false) SituacaoOrdemServico situacao,
                                                             @RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.ordens(situacao, unidadeId));
    }

    @RequerPermissao({"MANUTENCAO.EXECUTAR"})
    @GetMapping(value = "manutencao/resumo", produces = JSON)
    @Operation(summary = "Resumo da manutenção", description = "Ordens abertas, em execução e com bem parado; planos vencendo em 30 dias; calibrações vencidas.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ManutencaoDtos.Resumo> resumo() {
        return ResponseEntity.ok(service.resumo());
    }

    @RequerPermissao({"MANUTENCAO.EXECUTAR"})
    @GetMapping(value = "manutencao/contratos-servico", produces = JSON)
    @Operation(summary = "Contratos de serviço", description = "As atas e os contratos vigentes com item de serviço, para a execução por contrato.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ManutencaoDtos.ContratoServico>> contratosDeServico() {
        return ResponseEntity.ok(service.contratosDeServico());
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "ordem-servico/{uuid}", produces = JSON)
    @Operation(summary = "Ordem de serviço", description = "Para a manutenção da unidade ou para quem abriu o chamado (403 aos outros).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ManutencaoDtos.Ordem> ordem(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.ordem(uuid));
    }

    @RequerPermissao({"MANUTENCAO.EXECUTAR"})
    @PostMapping(value = "ordem-servico/{uuid}/triagem", produces = JSON, consumes = JSON)
    @Operation(summary = "Triagem", description = "A prioridade, só da ordem aberta (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ManutencaoDtos.Ordem> triar(@PathVariable UUID uuid, @Valid @RequestBody TriagemOrdemServicoRequestDto dto) {
        return ResponseEntity.ok(service.triar(uuid, dto));
    }

    @RequerPermissao({"MANUTENCAO.EXECUTAR"})
    @PostMapping(value = "ordem-servico/{uuid}/inicio", produces = JSON, consumes = JSON)
    @Operation(summary = "Inicia a execução", description = "Interna com o técnico (400) ou por contrato vigente com item de serviço (400/422); o bem e o veículo ficam em manutenção.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ManutencaoDtos.Ordem> iniciar(@PathVariable UUID uuid, @Valid @RequestBody InicioOrdemServicoRequestDto dto) {
        return ResponseEntity.ok(service.iniciar(uuid, dto));
    }

    @RequerPermissao({"MANUTENCAO.EXECUTAR"})
    @PostMapping(value = "ordem-servico/{uuid}/conclusao", produces = JSON, consumes = JSON)
    @Operation(summary = "Conclui a ordem", description = "O serviço, as peças (do Estoque, sem saldo 422) e o custo; a calibração pede o certificado e a validade (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ManutencaoDtos.Ordem> concluir(@PathVariable UUID uuid, @Valid @RequestBody ConclusaoOrdemServicoRequestDto dto) {
        return ResponseEntity.ok(service.concluir(uuid, dto));
    }

    @RequerPermissao({"MANUTENCAO.EXECUTAR"})
    @PostMapping(value = "ordem-servico/{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a ordem", description = "Aberta ou em execução (422), com o motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ManutencaoDtos.Ordem> cancelar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto));
    }
}
