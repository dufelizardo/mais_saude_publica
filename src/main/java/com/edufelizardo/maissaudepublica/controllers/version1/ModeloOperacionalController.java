package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CapacidadeDaUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerfilDaUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ModeloOperacionalUnidadeDto;
import com.edufelizardo.maissaudepublica.services.version1.ModeloOperacionalService;
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
public class ModeloOperacionalController {

    @Autowired
    private ModeloOperacionalService service;

    @RequerPermissao({"ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(value = "id/{uuid}/modelo-operacional", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Modelo operacional da unidade", description = "Perfis e capacidades vigentes e o histórico dos encerrados. Fora do "
            + "escopo: 403.", tags = "Modelo Operacional")
    @ApiErrorResponsesBusca
    public ResponseEntity<ModeloOperacionalUnidadeDto> modelo(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.modelo(uuid));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/modelo-operacional/perfil", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui um perfil operacional na unidade", description = "Já vigente: 409. Perfil inativo ou nível de gestão: 422. "
            + "Início no futuro: 400.", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<ModeloOperacionalUnidadeDto> incluirPerfil(@PathVariable UUID uuid, @Valid @RequestBody PerfilDaUnidadeRequestDto dto) {
        return ResponseEntity.ok(service.incluirPerfil(uuid, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/modelo-operacional/perfil/{associacaoId}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra um perfil da unidade", description = "Com motivo; o fim fica entre o início e hoje (400). Com capacidades do "
            + "grupo vigentes, ou já encerrado: 422.", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<ModeloOperacionalUnidadeDto> encerrarPerfil(@PathVariable UUID uuid, @PathVariable UUID associacaoId,
                                                                      @Valid @RequestBody EncerramentoRequestDto dto) {
        return ResponseEntity.ok(service.encerrarPerfil(uuid, associacaoId, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/modelo-operacional/capacidade", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui uma capacidade assistencial na unidade", description = "A de um grupo exige o perfil vigente (422). Já vigente: "
            + "409. Capacidade inativa ou nível de gestão: 422.", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<ModeloOperacionalUnidadeDto> incluirCapacidade(@PathVariable UUID uuid, @Valid @RequestBody CapacidadeDaUnidadeRequestDto dto) {
        return ResponseEntity.ok(service.incluirCapacidade(uuid, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/modelo-operacional/capacidade/{associacaoId}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra uma capacidade da unidade", description = "Com motivo; o fim fica entre o início e hoje (400). Já encerrada: 422.",
            tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<ModeloOperacionalUnidadeDto> encerrarCapacidade(@PathVariable UUID uuid, @PathVariable UUID associacaoId,
                                                                          @Valid @RequestBody EncerramentoRequestDto dto) {
        return ResponseEntity.ok(service.encerrarCapacidade(uuid, associacaoId, dto));
    }
}
