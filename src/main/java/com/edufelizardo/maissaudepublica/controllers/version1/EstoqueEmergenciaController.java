package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReservaEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EstoqueEmergenciaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoReservaEstoque;
import com.edufelizardo.maissaudepublica.services.version1.EstoqueEmergenciaService;
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
@RequestMapping(value = "/api/v1/estoque/")
@Tag(name = "Estoque nas emergências", description = "Reserva de material para a emergência e consumo extraordinário "
        + "(ver docs/adr/0191-estoque-nas-emergencias.md).")
public class EstoqueEmergenciaController {

    private static final String TAG = "Estoque nas emergências";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private EstoqueEmergenciaService service;

    @RequerPermissao({"ESTOQUE.CONSULTAR", "ESTOQUE.MOVIMENTAR", "ESTOQUE.GERENCIAR", "EMERGENCIA.COORDENAR"})
    @GetMapping(value = "reserva", produces = JSON)
    @Operation(summary = "Reservas para emergências", description = "As ativas primeiro; pela unidade, pela emergência e pela situação.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EstoqueEmergenciaDtos.Reserva>> reservas(@RequestParam(required = false) UUID unidadeId,
                                                                        @RequestParam(required = false) UUID emergenciaId,
                                                                        @RequestParam(required = false) SituacaoReservaEstoque situacao) {
        return ResponseEntity.ok(service.reservas(unidadeId, emergenciaId, situacao));
    }

    @RequerPermissao({"ESTOQUE.CONSULTAR", "ESTOQUE.MOVIMENTAR", "ESTOQUE.GERENCIAR", "EMERGENCIA.COORDENAR"})
    @GetMapping(value = "emergencias", produces = JSON)
    @Operation(summary = "Emergências abertas", description = "As ativas e em desmobilização, para a reserva e o consumo extraordinário.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EstoqueEmergenciaDtos.EmergenciaAberta>> emergencias() {
        return ResponseEntity.ok(service.emergenciasAbertas());
    }

    @RequerPermissao({"ESTOQUE.MOVIMENTAR"})
    @PostMapping(value = "reserva", produces = JSON, consumes = JSON)
    @Operation(summary = "Reserva material para a emergência", description = "Na unidade, com a emergência ativa (422), até o que está livre "
            + "dentro da validade (422). As saídas comuns deixam de tirar do reservado.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EstoqueEmergenciaDtos.Reserva> reservar(@Valid @RequestBody ReservaEstoqueRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.reservar(dto));
    }

    @RequerPermissao({"ESTOQUE.MOVIMENTAR"})
    @PostMapping(value = "reserva/{uuid}/liberacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Libera a reserva", description = "O que resta volta a ficar livre; com o motivo. Já liberada ou consumida, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EstoqueEmergenciaDtos.Reserva> liberar(@PathVariable UUID uuid, @Valid @RequestBody ReservaEstoqueRequestDto.Liberacao dto) {
        return ResponseEntity.ok(service.liberar(uuid, dto));
    }
}
