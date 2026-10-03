package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
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
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.BloqueioLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IndicadoresLeitosDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LeitoMapaDto;
import com.edufelizardo.maissaudepublica.services.version1.LeitoService;

@RestController
@RequestMapping(value = "/api/v1/leito/")
@Tag(name = "Leitos", description = "Cadastro, bloqueio, liberação depois da higienização, mapa e indicadores (ver docs/adr/0098-leitos-e-internacao-backend.md).")
public class LeitoController {

    @Autowired
    private LeitoService service;

    @RequerPermissao({"LEITO.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um leito", description = "Em setor assistencial da própria unidade (outra unidade: 400; setor não assistencial: 422). "
            + "Identificação repetida na unidade: 409. Nasce livre.", tags = "Leitos")
    @ApiErrorResponsesMutacao
    public ResponseEntity<LeitoMapaDto> criar(@Valid @RequestBody LeitoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"LEITO.GERENCIAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um leito", description = "Identificação, tipo, sexo e uso; unidade e setor ficam. Leito ocupado não sai de uso nem muda o sexo: 422.", tags = "Leitos")
    @ApiErrorResponsesMutacao
    public ResponseEntity<LeitoMapaDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody LeitoRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"LEITO.GERENCIAR"})
    @PostMapping(value = "{uuid}/bloqueio", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Bloqueia um leito", description = "Livre ou em higienização, com motivo. Ocupado ou já bloqueado: 422.", tags = "Leitos")
    @ApiErrorResponsesMutacao
    public ResponseEntity<LeitoMapaDto> bloquear(@PathVariable UUID uuid, @Valid @RequestBody BloqueioLeitoRequestDto dto) {
        return ResponseEntity.ok(service.bloquear(uuid, dto.getMotivo()));
    }

    @RequerPermissao({"LEITO.GERENCIAR"})
    @PostMapping(value = "{uuid}/desbloqueio", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desbloqueia um leito", description = "Volta a livre. Leito não bloqueado: 422.", tags = "Leitos")
    @ApiErrorResponsesMutacao
    public ResponseEntity<LeitoMapaDto> desbloquear(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.desbloquear(uuid));
    }

    @RequerPermissao({"LEITO.GERENCIAR"})
    @PostMapping(value = "{uuid}/liberacao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Libera o leito depois da higienização", description = "Em higienização volta a livre. Outra situação: 422.", tags = "Leitos")
    @ApiErrorResponsesMutacao
    public ResponseEntity<LeitoMapaDto> liberar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.liberar(uuid));
    }

    @RequerPermissao({"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "LEITO.GERENCIAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Mapa de leitos", description = "Leitos das unidades do escopo (ou de unidadeId), com o paciente de cada ocupado. "
            + "Fora de uso só com todos=true.", tags = "Leitos")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<LeitoMapaDto>> mapa(@RequestParam(required = false) UUID unidadeId,
                                                   @RequestParam(defaultValue = "false") boolean todos) {
        return ResponseEntity.ok(service.mapa(unidadeId, todos));
    }

    @RequerPermissao({"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "LEITO.GERENCIAR"})
    @GetMapping(value = "indicadores", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Indicadores dos leitos", description = "Livres, ocupados, em higienização, bloqueados, taxa de ocupação e média de "
            + "permanência das altas dos últimos 30 dias, no escopo (ou em unidadeId).", tags = "Leitos")
    public ResponseEntity<IndicadoresLeitosDto> indicadores(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.indicadores(unidadeId));
    }
}
