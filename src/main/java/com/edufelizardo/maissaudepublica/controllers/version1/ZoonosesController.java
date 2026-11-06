package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AnimalZoonosesRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DesfechoObservacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ExameAnimalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ObservacaoAnimalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResultadoExameAnimalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VacinacaoAntirrabicaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AnimalZoonosesDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ExameAnimalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ObservacaoAnimalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VacinacaoAntirrabicaDto;
import com.edufelizardo.maissaudepublica.models.enuns.EspecieAnimal;
import com.edufelizardo.maissaudepublica.models.enuns.TipoExameAnimal;
import com.edufelizardo.maissaudepublica.services.version1.ZoonosesService;
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
@RequestMapping(value = "/api/v1/")
@Tag(name = "Zoonoses", description = "Animal de interesse, observação do agressor, exames (raiva e leishmaniose) e vacinação antirrábica animal "
        + "(ver a ADR-0163).")
public class ZoonosesController {

    private static final String TAG = "Zoonoses";

    @Autowired
    private ZoonosesService service;

    @AuditarLeitura
    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR"})
    @GetMapping(value = "zoonoses/animais", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Animais", description = "Os ativos; a busca olha o nome, o tutor e o endereço. Leitura auditada (tutor).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<AnimalZoonosesDto>> animais(@RequestParam(required = false) String busca, @RequestParam(required = false) EspecieAnimal especie) {
        return ResponseEntity.ok(service.animais(busca, especie));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR"})
    @PostMapping(value = "zoonoses/animal", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra o animal", description = "O imóvel do Território ou o endereço (um dos dois, 400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AnimalZoonosesDto> registrarAnimal(@Valid @RequestBody AnimalZoonosesRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarAnimal(dto));
    }

    @AuditarLeitura
    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR"})
    @GetMapping(value = "zoonoses/observacoes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Observações de animal agressor", description = "abertas filtra; notificacaoId traz as do atendimento antirrábico. Leitura "
            + "auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ObservacaoAnimalDto>> observacoes(@RequestParam(required = false) Boolean abertas,
                                                                 @RequestParam(required = false) UUID notificacaoId) {
        return ResponseEntity.ok(service.observacoes(abertas, notificacaoId));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR"})
    @PostMapping(value = "zoonoses/observacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Abre a observação do agressor", description = "Cão ou gato (422), 10 dias a partir da agressão; o atendimento antirrábico "
            + "é opcional (outro agravo, 422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ObservacaoAnimalDto> abrirObservacao(@Valid @RequestBody ObservacaoAnimalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrirObservacao(dto));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR"})
    @PostMapping(value = "zoonoses/observacao/{uuid}/desfecho", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desfecho da observação", description = "Sadio só ao fim dos 10 dias (422); encerrada não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ObservacaoAnimalDto> desfecho(@PathVariable UUID uuid, @Valid @RequestBody DesfechoObservacaoRequestDto dto) {
        return ResponseEntity.ok(service.desfecho(uuid, dto));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR"})
    @GetMapping(value = "zoonoses/exames", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Exames de animais", description = "tipo e pendentes filtram.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ExameAnimalDto>> exames(@RequestParam(required = false) TipoExameAnimal tipo, @RequestParam(required = false) Boolean pendentes) {
        return ResponseEntity.ok(service.exames(tipo, pendentes));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR"})
    @PostMapping(value = "zoonoses/exame", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a coleta", description = "Raiva ou leishmaniose; a observação, se houver, é do mesmo animal (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ExameAnimalDto> coletar(@Valid @RequestBody ExameAnimalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.coletar(dto));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR"})
    @PostMapping(value = "zoonoses/exame/{uuid}/resultado", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Resultado do exame", description = "O desfecho do cão só na leishmaniose reagente (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ExameAnimalDto> resultado(@PathVariable UUID uuid, @Valid @RequestBody ResultadoExameAnimalRequestDto dto) {
        return ResponseEntity.ok(service.resultado(uuid, dto));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "zoonoses/vacinacao-antirrabica", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Vacinação antirrábica animal", description = "Os totais do ano (sem ano, o atual), por área, e os registros.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<VacinacaoAntirrabicaDto> vacinacao(@RequestParam(required = false) Integer ano) {
        return ResponseEntity.ok(service.vacinacao(ano));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR"})
    @PostMapping(value = "zoonoses/vacinacao-antirrabica", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a vacinação do posto", description = "Cães e gatos vacinados no dia (ao menos um, 400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<VacinacaoAntirrabicaDto> registrarVacinacao(@Valid @RequestBody VacinacaoAntirrabicaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarVacinacao(dto));
    }
}
