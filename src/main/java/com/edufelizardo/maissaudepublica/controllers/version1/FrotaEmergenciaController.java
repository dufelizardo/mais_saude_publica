package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CessaoVeiculoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FrotaEmergenciaDtos;
import com.edufelizardo.maissaudepublica.services.version1.FrotaEmergenciaService;
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
@RequestMapping(value = "/api/v1/transporte/emergencia/")
@Tag(name = "Transporte nas emergências", description = "Pedidos de veículo das emergências, cessão e devolução pela frota "
        + "(ver docs/adr/0192-transporte-nas-emergencias.md).")
public class FrotaEmergenciaController {

    private static final String TAG = "Transporte nas emergências";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private FrotaEmergenciaService service;

    @RequerPermissao({"TRANSPORTE.CONSULTAR", "TRANSPORTE.GERENCIAR"})
    @GetMapping(value = "pedidos", produces = JSON)
    @Operation(summary = "Pedidos de veículo das emergências", description = "Os abertos e os que ainda têm veículo cedido, com as cessões.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<FrotaEmergenciaDtos.Pedido>> pedidos() {
        return ResponseEntity.ok(service.pedidos());
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PostMapping(value = "mobilizacao/{mobilizacaoId}/cessao", produces = JSON, consumes = JSON)
    @Operation(summary = "Cede um veículo à emergência", description = "Pedido de veículo aberto da emergência ativa (422), até a quantidade (422). "
            + "Já cedido, 409; inativo, em manutenção ou em serviço, 422. Fica em serviço; devolve os avisos de viagens a reprogramar.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FrotaEmergenciaDtos.CessaoFeita> ceder(@PathVariable UUID mobilizacaoId, @Valid @RequestBody CessaoVeiculoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ceder(mobilizacaoId, dto));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PostMapping(value = "cessao/{uuid}/devolucao", produces = JSON, consumes = JSON)
    @Operation(summary = "Devolve o veículo", description = "Com a quilometragem, que não volta (422); o veículo volta a disponível. Já devolvido, 422.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<FrotaEmergenciaDtos.Cessao> devolver(@PathVariable UUID uuid, @Valid @RequestBody CessaoVeiculoRequestDto.Devolucao dto) {
        return ResponseEntity.ok(service.devolver(uuid, dto));
    }
}
