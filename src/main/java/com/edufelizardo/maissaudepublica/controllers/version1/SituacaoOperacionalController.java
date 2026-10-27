package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EsperaAtendimentoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.OcorrenciaEscalaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.OcupacaoAgendaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SituacaoAgoraDto;
import com.edufelizardo.maissaudepublica.services.version1.SituacaoOperacionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Situação operacional", description = "Fila, espera, ocupação das agendas, escala e alertas, só agregados "
        + "(ver docs/adr/0144-bi-situacao-operacional.md).")
public class SituacaoOperacionalController {

    private static final String TAG = "Situação operacional";

    @Autowired
    private SituacaoOperacionalService service;

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "situacao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Situação agora", description = "Em atendimento, aguardando a triagem e a consulta, a maior espera e os alertas "
            + "(fila, espera, agenda e escala). Sem unidade, as unidades do acesso.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<SituacaoAgoraDto> agora(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.agora(unidadeId));
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "situacao/espera", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Tempo de espera no período", description = "Da chegada ao primeiro contato e à consulta: média, mediana e "
            + "percentil 90, só dos atendimentos com chegada registrada. Padrão: o mês corrente; até um ano (400).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<EsperaAtendimentoDto> espera(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                                       @RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.espera(de, ate, unidadeId));
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "situacao/ocupacao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ocupação das agendas", description = "Vagas ofertadas e ocupadas por especialidade a partir de hoje "
            + "(padrão 7 dias, até 31), com a mesma montagem da Agenda.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<OcupacaoAgendaDto> ocupacao(@RequestParam(required = false) UUID unidadeId,
                                                      @RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(service.ocupacao(unidadeId, dias));
    }

    @RequerPermissao({"INDICADOR.CONSULTAR"})
    @GetMapping(value = "situacao/escala", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ocorrências da escala", description = "Turnos a partir de agora (padrão 7 dias, até 31) sem profissional ou com "
            + "o profissional afastado. Licença médica aparece só como \"Afastado\".", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<OcorrenciaEscalaDto>> escala(@RequestParam(required = false) UUID unidadeId,
                                                            @RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(service.escala(unidadeId, dias));
    }
}
