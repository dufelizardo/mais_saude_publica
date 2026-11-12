package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AutoInfracaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CienciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DecisaoSanitariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoMedidaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.JulgamentoRecursoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MedidaSanitariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProtocoloSanitarioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcessoSanitarioDtos;
import com.edufelizardo.maissaudepublica.models.enuns.FaseProcessoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaSanitaria;
import com.edufelizardo.maissaudepublica.services.version1.ProcessoSanitarioService;
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
@RequestMapping(value = "/api/v1/vigilancia-sanitaria/")
@Tag(name = "Processo sanitário", description = "Auto de infração, processo administrativo sanitário (Lei 6.437/1977), penalidades e medidas "
        + "(ver a ADR-0200).")
public class ProcessoSanitarioController {

    private static final String TAG = "Processo sanitário";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private ProcessoSanitarioService service;

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR", "VISA.JULGAR"})
    @GetMapping(value = "processos", produces = JSON)
    @Operation(summary = "Processos sanitários", description = "estabelecimentoId e fase filtram.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ProcessoSanitarioDtos.Processo>> processos(@RequestParam(required = false) UUID estabelecimentoId,
                                                                          @RequestParam(required = false) FaseProcessoSanitario fase) {
        return ResponseEntity.ok(service.processos(estabelecimentoId, fase));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR", "VISA.JULGAR"})
    @GetMapping(value = "processo/{uuid}", produces = JSON)
    @Operation(summary = "Processo sanitário", description = "Com os prazos, as penalidades, os termos e as medidas.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ProcessoSanitarioDtos.Processo> processo(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.processo(uuid));
    }

    @RequerPermissao({"VISA.INSPECIONAR"})
    @PostMapping(value = "processo", produces = JSON, consumes = JSON)
    @Operation(summary = "Lavra o auto de infração", description = "Abre o processo; as irregularidades abertas passam a autuadas; com login, o fiscal "
            + "logado assina (403 se não estiver entre os fiscais).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProcessoSanitarioDtos.Processo> lavrar(@Valid @RequestBody AutoInfracaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.lavrar(dto));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @PostMapping(value = "processo/{uuid}/ciencia", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a ciência", description = "Do auto (abre os 15 dias de defesa) ou da decisão (abre os 15 dias de recurso).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProcessoSanitarioDtos.Processo> ciencia(@PathVariable UUID uuid, @Valid @RequestBody CienciaRequestDto dto) {
        return ResponseEntity.ok(service.ciencia(uuid, dto));
    }

    @RequerPermissao({"VISA.GERENCIAR"})
    @PostMapping(value = "processo/{uuid}/defesa", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a defesa", description = "Depois do prazo, recebida como intempestiva enquanto não houver decisão.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProcessoSanitarioDtos.Processo> defesa(@PathVariable UUID uuid, @Valid @RequestBody ProtocoloSanitarioRequestDto dto) {
        return ResponseEntity.ok(service.defesa(uuid, dto));
    }

    @RequerPermissao({"VISA.JULGAR"})
    @PostMapping(value = "processo/{uuid}/decisao", produces = JSON, consumes = JSON)
    @Operation(summary = "Decide o processo", description = "Arquivamento (encerra) ou penalidades (esperam a ciência e o prazo de recurso).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProcessoSanitarioDtos.Processo> decidir(@PathVariable UUID uuid, @Valid @RequestBody DecisaoSanitariaRequestDto dto) {
        return ResponseEntity.ok(service.decidir(uuid, dto));
    }

    @RequerPermissao({"VISA.GERENCIAR"})
    @PostMapping(value = "processo/{uuid}/recurso", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra o recurso", description = "Até 15 dias da ciência da decisão; fora do prazo, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProcessoSanitarioDtos.Processo> recurso(@PathVariable UUID uuid, @Valid @RequestBody ProtocoloSanitarioRequestDto dto) {
        return ResponseEntity.ok(service.recurso(uuid, dto));
    }

    @RequerPermissao({"VISA.JULGAR"})
    @PostMapping(value = "processo/{uuid}/julgamento-recurso", produces = JSON, consumes = JSON)
    @Operation(summary = "Julga o recurso", description = "Provido, parcialmente provido ou improvido; encerra o processo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProcessoSanitarioDtos.Processo> julgarRecurso(@PathVariable UUID uuid, @Valid @RequestBody JulgamentoRecursoRequestDto dto) {
        return ResponseEntity.ok(service.julgarRecurso(uuid, dto));
    }

    @RequerPermissao({"VISA.GERENCIAR"})
    @PostMapping(value = "processo/{uuid}/encerramento", produces = JSON)
    @Operation(summary = "Encerra sem recurso", description = "Depois do prazo de recurso; as penalidades passam a valer.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProcessoSanitarioDtos.Processo> encerrar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.encerrar(uuid));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR", "VISA.JULGAR"})
    @GetMapping(value = "medidas", produces = JSON)
    @Operation(summary = "Medidas sanitárias", description = "estabelecimentoId e situacao filtram.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ProcessoSanitarioDtos.Medida>> medidas(@RequestParam(required = false) UUID estabelecimentoId,
                                                                      @RequestParam(required = false) SituacaoMedidaSanitaria situacao) {
        return ResponseEntity.ok(service.medidas(estabelecimentoId, situacao));
    }

    @RequerPermissao({"VISA.INSPECIONAR"})
    @PostMapping(value = "medida", produces = JSON, consumes = JSON)
    @Operation(summary = "Interdição cautelar ou apreensão", description = "A cautelar vai até 90 dias (400); a apreensão pede produto e quantidade.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProcessoSanitarioDtos.Medida> registrarMedida(@Valid @RequestBody MedidaSanitariaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarMedida(dto));
    }

    @RequerPermissao({"VISA.GERENCIAR"})
    @PostMapping(value = "medida/{uuid}/encerramento", produces = JSON, consumes = JSON)
    @Operation(summary = "Desinterdição ou destinação", description = "Só a vigente (422); a desinterdição gera o termo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProcessoSanitarioDtos.Medida> encerrarMedida(@PathVariable UUID uuid, @Valid @RequestBody EncerramentoMedidaRequestDto dto) {
        return ResponseEntity.ok(service.encerrarMedida(uuid, dto));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR", "VISA.JULGAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "interdicoes", produces = JSON)
    @Operation(summary = "Interdições vigentes", description = "Para a Sala de Situação; só o estabelecimento, a medida e as datas.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ProcessoSanitarioDtos.Interdicao>> interdicoes() {
        return ResponseEntity.ok(service.interdicoes());
    }
}
