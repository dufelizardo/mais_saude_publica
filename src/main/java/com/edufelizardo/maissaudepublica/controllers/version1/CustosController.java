package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CustosRequestDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CustosDtos;
import com.edufelizardo.maissaudepublica.services.version1.CustosService;
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
@RequestMapping(value = "/api/v1/financeiro/")
@Tag(name = "Custos", description = "Centros de custo, apuração mensal e indicadores de custo (ver a ADR-0220).")
public class CustosController {

    private static final String TAG = "Custos";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private CustosService service;

    @RequerPermissao({"FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "centros-custo", produces = JSON)
    @Operation(summary = "Centros de custo", description = "Por unidade e setor; o geral de cada unidade ativa nasce na primeira leitura.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<CustosDtos.Centro>> centros() {
        return ResponseEntity.ok(service.centros());
    }

    @RequerPermissao({"FINANCEIRO.GERENCIAR"})
    @PostMapping(value = "centro-custo", produces = JSON, consumes = JSON)
    @Operation(summary = "Cria o centro do setor", description = "O setor (404), um centro por setor (409); sem nome e tipo, os do setor.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CustosDtos.Centro> criar(@Valid @RequestBody CustosRequestDtos.NovoCentro dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"FINANCEIRO.GERENCIAR"})
    @PutMapping(value = "centro-custo/{uuid}", produces = JSON, consumes = JSON)
    @Operation(summary = "Altera o centro", description = "O nome, o tipo e se está ativo; o geral não se desativa (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CustosDtos.Centro> atualizar(@PathVariable UUID uuid, @Valid @RequestBody CustosRequestDtos.AtualizacaoCentro dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "apuracoes-custo", produces = JSON)
    @Operation(summary = "Apurações", description = "As competências apuradas, com o total e a situação.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<CustosDtos.ApuracaoResumo>> apuracoes() {
        return ResponseEntity.ok(service.apuracoes());
    }

    @RequerPermissao({"FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR"})
    @GetMapping(value = "apuracao-custo", produces = JSON)
    @Operation(summary = "Apuração da competência", description = "Por centro e categoria (400 no formato, 404 sem apuração).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CustosDtos.Apuracao> apuracao(@RequestParam String competencia) {
        return ResponseEntity.ok(service.apuracao(competencia));
    }

    @RequerPermissao({"FINANCEIRO.GERENCIAR"})
    @PostMapping(value = "apuracao-custo", produces = JSON, consumes = JSON)
    @Operation(summary = "Apura a competência", description = "Do mês encerrado (422); refaz a aberta, a fechada não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CustosDtos.Apuracao> apurar(@Valid @RequestBody CustosRequestDtos.Apurar dto) {
        return ResponseEntity.ok(service.apurar(dto.competencia()));
    }

    @RequerPermissao({"FINANCEIRO.GERENCIAR"})
    @PostMapping(value = "apuracao-custo/{uuid}/fechamento", produces = JSON)
    @Operation(summary = "Fecha a apuração", description = "Vira fotografia; a fechada não fecha de novo (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CustosDtos.Apuracao> fechar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.fechar(uuid));
    }

    @RequerPermissao({"FINANCEIRO.CONSULTAR", "FINANCEIRO.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "indicadores-custo", produces = JSON)
    @Operation(summary = "Indicadores de custo", description = "Por unidade: custo por atendimento e por paciente-dia contra a média do tipo (404 sem apuração).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CustosDtos.Indicadores> indicadores(@RequestParam String competencia) {
        return ResponseEntity.ok(service.indicadores(competencia));
    }
}
