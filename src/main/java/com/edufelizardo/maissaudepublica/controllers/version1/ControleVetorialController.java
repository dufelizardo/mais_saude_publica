package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoControleVetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CancelamentoAcaoVetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LaboratorioFocoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RealizacaoAcaoVetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoFocoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VistoriaVetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcaoControleVetorialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.BloqueioPendenteDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FocoVetorialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ImovelVetorialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IndicesVetoriaisDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VistoriaVetorialDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoControleVetorial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoFoco;
import com.edufelizardo.maissaudepublica.services.version1.ControleVetorialService;
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
@Tag(name = "Controle de vetores", description = "Vistoria do agente de endemias, focos, índices do ciclo (IIP e Breteau) e ações de controle "
        + "(ver a ADR-0162).")
public class ControleVetorialController {

    private static final String TAG = "Controle de vetores";

    @Autowired
    private ControleVetorialService service;

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR"})
    @GetMapping(value = "vetores/imoveis", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Imóveis para a vistoria", description = "Da área ou da microárea (sem nenhuma, 400), com a busca no endereço e a última "
            + "vistoria; sem família nem morador.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ImovelVetorialDto>> imoveis(@RequestParam(required = false) UUID areaId, @RequestParam(required = false) UUID microareaId,
                                                           @RequestParam(required = false) String busca) {
        return ResponseEntity.ok(service.imoveis(areaId, microareaId, busca));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR"})
    @GetMapping(value = "vetores/pendencias", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Pendências de revisita", description = "Imóveis cuja última vistoria do ciclo atual ficou fechada ou recusada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ImovelVetorialDto>> pendencias(@RequestParam(required = false) UUID areaId) {
        return ResponseEntity.ok(service.pendencias(areaId));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR"})
    @PostMapping(value = "vetores/vistoria", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a vistoria", description = "Depósitos por tipo do PNCD, tratamento e focos; fechado ou recusado não leva depósito "
            + "(422); focos de um tipo não passam dos depósitos desse tipo (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<VistoriaVetorialDto> registrarVistoria(@Valid @RequestBody VistoriaVetorialRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarVistoria(dto));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "vetores/vistorias", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Vistorias", description = "Do ciclo (sem ciclo, o atual), da área ou do imóvel.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<VistoriaVetorialDto>> vistorias(@RequestParam(required = false) Integer ano, @RequestParam(required = false) Integer ciclo,
                                                               @RequestParam(required = false) UUID areaId, @RequestParam(required = false) UUID imovelId) {
        return ResponseEntity.ok(service.vistorias(ano, ciclo, areaId, imovelId));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "vetores/focos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Focos", description = "Com o imóvel e o local; situacao e areaId filtram.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<FocoVetorialDto>> focos(@RequestParam(required = false) SituacaoFoco situacao, @RequestParam(required = false) UUID areaId) {
        return ResponseEntity.ok(service.focos(situacao, areaId));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR"})
    @PostMapping(value = "vetores/foco/{uuid}/laboratorio", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Resultado do laboratório", description = "Só do foco com amostra (422); negativo não conta nos índices.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FocoVetorialDto> laboratorio(@PathVariable UUID uuid, @Valid @RequestBody LaboratorioFocoRequestDto dto) {
        return ResponseEntity.ok(service.laboratorio(uuid, dto));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR"})
    @PostMapping(value = "vetores/foco/{uuid}/situacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Trata ou elimina o foco", description = "Só do ativo (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FocoVetorialDto> situacaoDoFoco(@PathVariable UUID uuid, @Valid @RequestBody SituacaoFocoRequestDto dto) {
        return ResponseEntity.ok(service.situacaoDoFoco(uuid, dto));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "vetores/indices", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Índices do ciclo", description = "IIP e Breteau por área e no município, com a classificação do Ministério da Saúde; sem "
            + "ciclo, o atual (1 a 6; fora disso, 400).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<IndicesVetoriaisDto> indices(@RequestParam(required = false) Integer ano, @RequestParam(required = false) Integer ciclo) {
        return ResponseEntity.ok(service.indices(ano, ciclo));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "vetores/acoes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ações de controle", description = "situacao filtra.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<AcaoControleVetorialDto>> acoes(@RequestParam(required = false) SituacaoAcaoControleVetorial situacao) {
        return ResponseEntity.ok(service.acoes(situacao));
    }

    @RequerPermissao({"ZOONOSES.GERENCIAR"})
    @PostMapping(value = "vetores/acao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Planeja a ação de controle", description = "Nas microáreas; o caso de arbovirose (outro agravo, 422; já com ação, 409) e o "
            + "surto são opcionais.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcaoControleVetorialDto> planejar(@Valid @RequestBody AcaoControleVetorialRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.planejar(dto));
    }

    @RequerPermissao({"ZOONOSES.GERENCIAR"})
    @PostMapping(value = "vetores/acao/{uuid}/realizacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a ação como realizada", description = "Da planejada (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcaoControleVetorialDto> realizar(@PathVariable UUID uuid, @Valid @RequestBody RealizacaoAcaoVetorialRequestDto dto) {
        return ResponseEntity.ok(service.realizar(uuid, dto));
    }

    @RequerPermissao({"ZOONOSES.GERENCIAR"})
    @PostMapping(value = "vetores/acao/{uuid}/cancelamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancela a ação", description = "Da planejada (422), com o motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcaoControleVetorialDto> cancelar(@PathVariable UUID uuid, @Valid @RequestBody CancelamentoAcaoVetorialRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR"})
    @GetMapping(value = "vetores/bloqueios-pendentes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Bloqueios pendentes", description = "Casos de dengue, chikungunya e zika dos últimos 30 dias, não descartados e sem ação de "
            + "controle: o lugar e as datas, sem o paciente. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<BloqueioPendenteDto>> bloqueiosPendentes() {
        return ResponseEntity.ok(service.bloqueiosPendentes());
    }
}
