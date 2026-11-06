package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AjusteLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CamaraRefrigeracaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EntradaLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LeituraTemperaturaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerdaLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CadeiaFrioDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CamaraRefrigeracaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EstoqueSalaVacinaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LeituraTemperaturaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LoteImunobiologicoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MovimentacaoImunobiologicoDto;
import com.edufelizardo.maissaudepublica.services.version1.CadeiaFrioService;
import com.edufelizardo.maissaudepublica.services.version1.EstoqueVacinaService;
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
@RequestMapping(value = "/api/v1/")
@Tag(name = "Sala de vacina", description = "Estoque de imunobiológicos e cadeia de frio da unidade com sala de vacina "
        + "(ver docs/adr/0148-imunizacao-sala-de-vacina.md).")
public class SalaDeVacinaController {

    private static final String TAG = "Sala de vacina";

    @Autowired
    private EstoqueVacinaService estoqueService;

    @Autowired
    private CadeiaFrioService cadeiaFrioService;

    // ── Estoque ──

    @RequerPermissao({"VACINACAO.REGISTRAR", "IMUNIZACAO.GERENCIAR"})
    @GetMapping(value = "sala-de-vacina/estoque", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Estoque da sala de vacina", description = "O saldo aplicável por vacina, os lotes, os frascos abertos e o mês "
            + "(aplicadas, perdas técnica e física). Apura os frascos abertos vencidos.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<EstoqueSalaVacinaDto> estoque(@RequestParam UUID unidadeId) {
        return ResponseEntity.ok(estoqueService.estoque(unidadeId));
    }

    @RequerPermissao({"VACINACAO.REGISTRAR"})
    @PostMapping(value = "sala-de-vacina/entrada", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Entrada de imunobiológico", description = "Cria o lote ou soma ao existente (mesmo número e validade, 422 se "
            + "diverge). Só na unidade com sala de vacina (422); lote vencido não entra (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LoteImunobiologicoDto> entrada(@Valid @RequestBody EntradaLoteRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estoqueService.entrada(dto));
    }

    @RequerPermissao({"VACINACAO.REGISTRAR"})
    @PostMapping(value = "lote-imunobiologico/{uuid}/perda", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Perda física", description = "Quebra, falha de equipamento, vencimento do lote... Não passa do saldo (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LoteImunobiologicoDto> perda(@PathVariable UUID uuid, @Valid @RequestBody PerdaLoteRequestDto dto) {
        return ResponseEntity.ok(estoqueService.perda(uuid, dto));
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PostMapping(value = "lote-imunobiologico/{uuid}/ajuste", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajuste de inventário", description = "O saldo contado, com a justificativa.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LoteImunobiologicoDto> ajuste(@PathVariable UUID uuid, @Valid @RequestBody AjusteLoteRequestDto dto) {
        return ResponseEntity.ok(estoqueService.ajuste(uuid, dto));
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PostMapping(value = "lote-imunobiologico/{uuid}/avaliacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Avalia o lote sob suspeita", description = "LIBERAR (com o parecer) ou INUTILIZAR (o saldo sai como perda física "
            + "por excursão de temperatura).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LoteImunobiologicoDto> avaliar(@PathVariable UUID uuid, @Valid @RequestBody AvaliacaoLoteRequestDto dto) {
        return ResponseEntity.ok(cadeiaFrioService.avaliar(uuid, dto));
    }

    @RequerPermissao({"VACINACAO.REGISTRAR", "IMUNIZACAO.GERENCIAR"})
    @GetMapping(value = "lote-imunobiologico/{uuid}/movimentacoes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Livro do lote", description = "Os lançamentos do lote, com o saldo depois de cada um.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<MovimentacaoImunobiologicoDto>> movimentacoes(@PathVariable UUID uuid) {
        return ResponseEntity.ok(estoqueService.movimentacoes(uuid));
    }

    // ── Cadeia de frio ──

    @RequerPermissao({"VACINACAO.REGISTRAR", "IMUNIZACAO.GERENCIAR"})
    @GetMapping(value = "sala-de-vacina/cadeia-de-frio", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadeia de frio do dia", description = "As câmaras com a última leitura, as leituras do dia (padrão: hoje), os "
            + "turnos sem leitura e os lotes sob suspeita.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CadeiaFrioDto> cadeiaDeFrio(@RequestParam UUID unidadeId,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(cadeiaFrioService.painel(unidadeId, data));
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PostMapping(value = "camara-refrigeracao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra uma câmara", description = "Na unidade com sala de vacina (422); faixa padrão de 2 a 8 °C; nome único "
            + "na unidade (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CamaraRefrigeracaoDto> criarCamara(@Valid @RequestBody CamaraRefrigeracaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadeiaFrioService.criarCamara(dto));
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PutMapping(value = "camara-refrigeracao/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta uma câmara", description = "Nome, faixa e ativa.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CamaraRefrigeracaoDto> atualizarCamara(@PathVariable UUID uuid, @Valid @RequestBody CamaraRefrigeracaoRequestDto dto) {
        return ResponseEntity.ok(cadeiaFrioService.atualizarCamara(uuid, dto));
    }

    @RequerPermissao({"VACINACAO.REGISTRAR"})
    @PostMapping(value = "camara-refrigeracao/{uuid}/leitura", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a leitura de temperatura", description = "Atual, mínima e máxima (em ordem, 400). Fora da faixa é "
            + "excursão: os lotes disponíveis da câmara ficam sob suspeita.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LeituraTemperaturaDto> leitura(@PathVariable UUID uuid, @Valid @RequestBody LeituraTemperaturaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadeiaFrioService.registrarLeitura(uuid, dto));
    }
}
