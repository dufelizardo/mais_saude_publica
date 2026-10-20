package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AreaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtribuicaoMicroareaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CoordenadasUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoAtribuicaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MicroareaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResponsabilidadeTerritorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SegmentoTerritorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TerritorioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AreaDetalheDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ArvoreTerritorialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TerritorioDaEquipeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.UnidadeNoMapaDto;
import com.edufelizardo.maissaudepublica.services.version1.TerritorioService;
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
@RequestMapping(value = "/api/v1/territorio/")
@Tag(name = "Território", description = "Segmento, território, área e microárea com GeoJSON; equipe da área e ACS da microárea como histórico; "
        + "coordenadas das unidades (ver docs/adr/0125-territorio-fundacao.md).")
public class TerritorioController {

    private static final String TAG = "Território";
    private static final String GERENCIAR = "TERRITORIO.GERENCIAR";

    @Autowired
    private TerritorioService service;

    @LiberadoParaAutenticados
    @GetMapping(value = "arvore", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "O território inteiro", description = "Segmento → território → área (equipe) → microáreas (ACS), com indicadores e "
            + "avisos: sem ACS, sem desenho e população acima de 750 (CNES).", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<ArvoreTerritorialDto> arvore() {
        return ResponseEntity.ok(service.arvore());
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "area/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Uma área", description = "Com as microáreas, o histórico de equipes e de ACS e os ACS da equipe sem microárea.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<AreaDetalheDto> area(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.area(uuid));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "equipe/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "A área de uma equipe", description = "A área vigente com as microáreas e os ACS; sem área, area nula.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<TerritorioDaEquipeDto> daEquipe(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.daEquipe(uuid));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "unidades", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Unidades no mapa", description = "As unidades ativas com latitude e longitude (nulas enquanto não informadas).", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<UnidadeNoMapaDto>> unidades() {
        return ResponseEntity.ok(service.unidades());
    }

    @RequerPermissao({GERENCIAR})
    @PostMapping(value = "segmento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um segmento", description = "Código repetido: 409; GeoJSON inválido: 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ArvoreTerritorialDto.Segmento> criarSegmento(@Valid @RequestBody SegmentoTerritorialRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarSegmento(dto));
    }

    @RequerPermissao({GERENCIAR})
    @PatchMapping(value = "segmento/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um segmento", description = "Código repetido: 409; GeoJSON inválido: 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ArvoreTerritorialDto.Segmento> atualizarSegmento(@PathVariable UUID uuid, @Valid @RequestBody SegmentoTerritorialRequestDto dto) {
        return ResponseEntity.ok(service.atualizarSegmento(uuid, dto));
    }

    @RequerPermissao({GERENCIAR})
    @PostMapping(value = "territorio", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um território", description = "Com a finalidade. Nome repetido no segmento: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ArvoreTerritorialDto.TerritorioNo> criarTerritorio(@Valid @RequestBody TerritorioRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarTerritorio(dto));
    }

    @RequerPermissao({GERENCIAR})
    @PatchMapping(value = "territorio/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um território", description = "O segmento não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ArvoreTerritorialDto.TerritorioNo> atualizarTerritorio(@PathVariable UUID uuid, @Valid @RequestBody TerritorioRequestDto dto) {
        return ResponseEntity.ok(service.atualizarTerritorio(uuid, dto));
    }

    @RequerPermissao({GERENCIAR})
    @PostMapping(value = "area", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra uma área", description = "Código repetido no território: 409; território inativo: 422; GeoJSON inválido: 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AreaDetalheDto> criarArea(@Valid @RequestBody AreaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarArea(dto));
    }

    @RequerPermissao({GERENCIAR})
    @PatchMapping(value = "area/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita uma área", description = "Um desenho que deixe microárea de fora: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AreaDetalheDto> atualizarArea(@PathVariable UUID uuid, @Valid @RequestBody AreaRequestDto dto) {
        return ResponseEntity.ok(service.atualizarArea(uuid, dto));
    }

    @RequerPermissao({GERENCIAR})
    @PostMapping(value = "microarea", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra uma microárea", description = "Código repetido na área: 409. Fora da área ou sobreposta a outra: 422. "
            + "Devolve a área.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AreaDetalheDto> criarMicroarea(@Valid @RequestBody MicroareaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarMicroarea(dto));
    }

    @RequerPermissao({GERENCIAR})
    @PatchMapping(value = "microarea/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita uma microárea", description = "Fora da área ou sobreposta a outra: 422. Devolve a área.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AreaDetalheDto> atualizarMicroarea(@PathVariable UUID uuid, @Valid @RequestBody MicroareaRequestDto dto) {
        return ResponseEntity.ok(service.atualizarMicroarea(uuid, dto));
    }

    @RequerPermissao({GERENCIAR})
    @PostMapping(value = "area/{uuid}/responsabilidade", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atribui ou troca a equipe da área", description = "Uma equipe vigente por área e uma área por equipe (409). Na troca, "
            + "motivo obrigatório (400); a anterior e os ACS dela saem na data de início.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AreaDetalheDto> atribuirEquipe(@PathVariable UUID uuid, @Valid @RequestBody ResponsabilidadeTerritorialRequestDto dto) {
        return ResponseEntity.ok(service.atribuirEquipe(uuid, dto));
    }

    @RequerPermissao({GERENCIAR})
    @PostMapping(value = "microarea/{uuid}/atribuicao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atribui ou troca o ACS da microárea", description = "Membro vigente, com função ACS, da equipe da área (422). Um ACS "
            + "por microárea e uma microárea por ACS (409). Na troca, motivo obrigatório (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AreaDetalheDto> atribuirAcs(@PathVariable UUID uuid, @Valid @RequestBody AtribuicaoMicroareaRequestDto dto) {
        return ResponseEntity.ok(service.atribuirAcs(uuid, dto));
    }

    @RequerPermissao({GERENCIAR})
    @PostMapping(value = "microarea/{uuid}/atribuicao/encerrar", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Deixa a microárea sem ACS", description = "Com motivo; o fim fica entre o início e hoje (400). Sem ACS: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AreaDetalheDto> encerrarAcs(@PathVariable UUID uuid, @Valid @RequestBody EncerramentoAtribuicaoRequestDto dto) {
        return ResponseEntity.ok(service.encerrarAcs(uuid, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PutMapping(value = "unidade/{uuid}/coordenadas", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Define as coordenadas da unidade", description = "Latitude e longitude em WGS84, juntas ou nenhuma (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<UnidadeNoMapaDto> coordenadas(@PathVariable UUID uuid, @Valid @RequestBody CoordenadasUnidadeRequestDto dto) {
        return ResponseEntity.ok(service.definirCoordenadas(uuid, dto));
    }
}
