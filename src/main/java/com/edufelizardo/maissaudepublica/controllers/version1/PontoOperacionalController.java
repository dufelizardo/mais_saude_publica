package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PontoOperacionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PosicaoUnidadeMovelRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RotaProgramadaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LocaisDeRealizacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PontosDaUnidadeDto;
import com.edufelizardo.maissaudepublica.services.version1.PontoOperacionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/unidade-saude/")
@Tag(name = "Modelo Operacional")
public class PontoOperacionalController {

    private static final String TAG = "Modelo Operacional";

    @Autowired
    private PontoOperacionalService service;

    @RequerPermissao({"ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(value = "id/{uuid}/pontos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Pontos operacionais da unidade", description = "Pontos vigentes e encerrados; na unidade móvel, rotas e localização.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<PontosDaUnidadeDto> listar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.daUnidade(uuid));
    }

    @RequerPermissao({"ATENDIMENTO.GERENCIAR", "ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(value = "id/{uuid}/locais-de-realizacao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Locais de realização", description = "Pontos ativos da unidade e o domicílio atual do paciente, para o atendimento.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<LocaisDeRealizacaoDto> locais(@PathVariable UUID uuid, @RequestParam(required = false) UUID pacienteId) {
        return ResponseEntity.ok(service.locais(uuid, pacienteId));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/ponto", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui um ponto operacional", description = "Base só na unidade móvel (422) e uma vigente (409); nome repetido entre os ativos: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PontosDaUnidadeDto> incluirPonto(@PathVariable UUID uuid, @Valid @RequestBody PontoOperacionalRequestDto dto) {
        return ResponseEntity.ok(service.incluirPonto(uuid, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PatchMapping(value = "id/{uuid}/ponto/{pontoId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta o ponto", description = "Nome, referência e posição; encerrado não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PontosDaUnidadeDto> atualizarPonto(@PathVariable UUID uuid, @PathVariable UUID pontoId, @Valid @RequestBody PontoOperacionalRequestDto dto) {
        return ResponseEntity.ok(service.atualizarPonto(uuid, pontoId, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/ponto/{pontoId}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra o ponto", description = "Com motivo; ponto de rota vigente não encerra (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PontosDaUnidadeDto> encerrarPonto(@PathVariable UUID uuid, @PathVariable UUID pontoId, @Valid @RequestBody EncerramentoRequestDto dto) {
        return ResponseEntity.ok(service.encerrarPonto(uuid, pontoId, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/rota", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui uma rota programada", description = "Só na unidade móvel (422); pontos ativos da unidade, sem repetir (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PontosDaUnidadeDto> incluirRota(@PathVariable UUID uuid, @Valid @RequestBody RotaProgramadaRequestDto dto) {
        return ResponseEntity.ok(service.incluirRota(uuid, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PatchMapping(value = "id/{uuid}/rota/{rotaId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta a rota", description = "Encerrada não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PontosDaUnidadeDto> atualizarRota(@PathVariable UUID uuid, @PathVariable UUID rotaId, @Valid @RequestBody RotaProgramadaRequestDto dto) {
        return ResponseEntity.ok(service.atualizarRota(uuid, rotaId, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/rota/{rotaId}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra a rota", description = "Com motivo; o fim fica entre o início e hoje (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PontosDaUnidadeDto> encerrarRota(@PathVariable UUID uuid, @PathVariable UUID rotaId, @Valid @RequestBody EncerramentoRequestDto dto) {
        return ResponseEntity.ok(service.encerrarRota(uuid, rotaId, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/posicao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra onde a unidade móvel está", description = "Informado por quem opera, sem GPS. O ponto atual de novo: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PontosDaUnidadeDto> registrarPosicao(@PathVariable UUID uuid, @Valid @RequestBody PosicaoUnidadeMovelRequestDto dto) {
        return ResponseEntity.ok(service.registrarPosicao(uuid, dto));
    }
}
