package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AmostraAguaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ControleRiscoAmbientalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MedidaAmostraAguaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PontoColetaAguaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RiscoAmbientalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AmostraAguaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PontoColetaAguaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RiscoAmbientalDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRiscoAmbiental;
import com.edufelizardo.maissaudepublica.services.version1.VigilanciaAmbientalService;
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
@Tag(name = "Vigilância ambiental", description = "Qualidade da água (pontos, amostras e conformidade pela Portaria GM/MS 888/2021) e risco "
        + "ambiental permanente (ver a ADR-0164).")
public class VigilanciaAmbientalController {

    private static final String TAG = "Vigilância ambiental";

    @Autowired
    private VigilanciaAmbientalService service;

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "ambiental/pontos-agua", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Pontos de coleta de água", description = "Com a última amostra e se estava conforme.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PontoColetaAguaDto>> pontos() {
        return ResponseEntity.ok(service.pontos());
    }

    @RequerPermissao({"ZOONOSES.GERENCIAR"})
    @PostMapping(value = "ambiental/ponto-agua", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra o ponto de coleta", description = "Rede, solução alternativa coletiva ou individual.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PontoColetaAguaDto> criarPonto(@Valid @RequestBody PontoColetaAguaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarPonto(dto));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "ambiental/amostras-agua", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Amostras de água", description = "pontoId, naoConformes e desde filtram.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<AmostraAguaDto>> amostras(@RequestParam(required = false) UUID pontoId, @RequestParam(required = false) Boolean naoConformes,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde) {
        return ResponseEntity.ok(service.amostras(pontoId, naoConformes, desde));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR"})
    @PostMapping(value = "ambiental/amostra-agua", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a amostra", description = "A conformidade sai da Portaria GM/MS 888/2021; ao menos um parâmetro (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AmostraAguaDto> registrarAmostra(@Valid @RequestBody AmostraAguaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarAmostra(dto));
    }

    @RequerPermissao({"ZOONOSES.GERENCIAR"})
    @PostMapping(value = "ambiental/amostra-agua/{uuid}/medida", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Medida da amostra fora do padrão", description = "A conforme, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AmostraAguaDto> registrarMedida(@PathVariable UUID uuid, @Valid @RequestBody MedidaAmostraAguaRequestDto dto) {
        return ResponseEntity.ok(service.registrarMedida(uuid, dto));
    }

    @RequerPermissao({"ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "ambiental/riscos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Riscos ambientais", description = "situacao filtra.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<RiscoAmbientalDto>> riscos(@RequestParam(required = false) SituacaoRiscoAmbiental situacao) {
        return ResponseEntity.ok(service.riscos(situacao));
    }

    @RequerPermissao({"ZOONOSES.GERENCIAR"})
    @PostMapping(value = "ambiental/risco", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra o risco ambiental", description = "Área contaminada, descarte irregular ou fonte poluidora; nasce ativo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RiscoAmbientalDto> registrarRisco(@Valid @RequestBody RiscoAmbientalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarRisco(dto));
    }

    @RequerPermissao({"ZOONOSES.GERENCIAR"})
    @PostMapping(value = "ambiental/risco/{uuid}/controle", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Marca o risco como controlado", description = "Do ativo (422), com o motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RiscoAmbientalDto> controlar(@PathVariable UUID uuid, @Valid @RequestBody ControleRiscoAmbientalRequestDto dto) {
        return ResponseEntity.ok(service.controlar(uuid, dto));
    }
}
