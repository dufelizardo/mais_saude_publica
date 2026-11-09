package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PlantaoViaturaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoVeiculoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VeiculoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransporteDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoVeiculo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoVeiculo;
import com.edufelizardo.maissaudepublica.services.version1.TransporteService;
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
@RequestMapping(value = "/api/v1/transporte/")
@Tag(name = "Transporte · frota", description = "Frota, situação com histórico e plantão das viaturas (ver docs/adr/0178-transporte-sanitario.md).")
public class TransporteController {

    private static final String TAG = "Transporte · frota";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private TransporteService service;

    @RequerPermissao({"TRANSPORTE.CONSULTAR", "TRANSPORTE.GERENCIAR"})
    @GetMapping(value = "veiculo", produces = JSON)
    @Operation(summary = "Frota", description = "Das unidades do escopo, com o plantão vigente. Filtros: unidadeId, situacao, tipo.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TransporteDtos.Veiculo>> listar(@RequestParam(required = false) UUID unidadeId,
                                                               @RequestParam(required = false) SituacaoVeiculo situacao,
                                                               @RequestParam(required = false) TipoVeiculo tipo) {
        return ResponseEntity.ok(service.listar(unidadeId, situacao, tipo));
    }

    @RequerPermissao({"TRANSPORTE.CONSULTAR", "TRANSPORTE.GERENCIAR"})
    @GetMapping(value = "veiculo/{uuid}", produces = JSON)
    @Operation(summary = "O veículo", description = "Com o histórico de situações e os plantões.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<TransporteDtos.Veiculo> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscar(uuid));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PostMapping(value = "veiculo", produces = JSON, consumes = JSON)
    @Operation(summary = "Cadastra um veículo", description = "Placa (padrão antigo ou Mercosul, 400) e prefixo únicos (409); viatura do SAMU em unidade "
            + "com o perfil Atenção móvel (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteDtos.Veiculo> criar(@Valid @RequestBody VeiculoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PutMapping(value = "veiculo/{uuid}", produces = JSON, consumes = JSON)
    @Operation(summary = "Atualiza um veículo", description = "A quilometragem não volta (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteDtos.Veiculo> atualizar(@PathVariable UUID uuid, @Valid @RequestBody VeiculoRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PostMapping(value = "veiculo/{uuid}/situacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Muda a situação", description = "Manutenção, disponível ou inativo, com motivo. Em serviço vem do SAMU (400); veículo em serviço "
            + "ou já na situação: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteDtos.Veiculo> situacao(@PathVariable UUID uuid, @Valid @RequestBody SituacaoVeiculoRequestDto dto) {
        return ResponseEntity.ok(service.mudarSituacao(uuid, dto));
    }

    @RequerPermissao({"TRANSPORTE.CONSULTAR", "TRANSPORTE.GERENCIAR"})
    @GetMapping(value = "plantao", produces = JSON)
    @Operation(summary = "Plantões", description = "Das últimas 24 horas aos próximos dias (7, até 14). Filtro: unidadeId.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TransporteDtos.Plantao>> plantoes(@RequestParam(required = false) UUID unidadeId,
                                                                 @RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(service.plantoes(unidadeId, dias));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PostMapping(value = "plantao", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra um plantão", description = "Até 24 horas (400), com a tripulação mínima do tipo (422); viatura ou tripulante em outro "
            + "plantão no período: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteDtos.Plantao> criarPlantao(@Valid @RequestBody PlantaoViaturaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarPlantao(dto));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PostMapping(value = "plantao/{uuid}/encerramento", produces = JSON)
    @Operation(summary = "Encerra o plantão agora", description = "O futuro fica cancelado; o já terminado: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteDtos.Plantao> encerrarPlantao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.encerrarPlantao(uuid));
    }
}
