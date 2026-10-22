package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoRedeApoioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.IntervencaoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PessoaRedeApoioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProjetoTerapeuticoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProjetoTerapeuticoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TipoIntervencaoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.services.version1.ProjetoTerapeuticoService;
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
@RequestMapping(value = "/api/v1/saude-mental/")
@Tag(name = "Saúde Mental · PTS", description = "Projeto Terapêutico Singular versionado, intervenções e rede de apoio do caso, com o sigilo "
        + "do caso (ver docs/adr/0121-saude-mental-pts-intervencoes.md).")
public class ProjetoTerapeuticoController {

    @Autowired
    private ProjetoTerapeuticoService service;

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR", "SAUDE_MENTAL.ACOLHER"})
    @GetMapping(value = "tipos-intervencao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Tipos de intervenção", description = "O catálogo de tipos de intervenção em saúde mental.", tags = "Saúde Mental · PTS")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TipoIntervencaoSaudeMentalDto>> tipos() {
        return ResponseEntity.ok(service.tipos());
    }

    @AuditarLeitura
    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @GetMapping(value = "acompanhamento/{uuid}/pts", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Versões do PTS", description = "Todas as versões, a vigente primeiro. Pede vínculo e o conteúdo do caso liberado (403). "
            + "Leitura auditada.", tags = "Saúde Mental · PTS")
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ProjetoTerapeuticoDto>> versoes(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.versoes(uuid));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/pts", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Elabora ou revisa o PTS", description = "Sem versaoBase, a primeira versão; com ela, a revisão, que exige a síntese (400). "
            + "Versão base diferente da vigente: 409. Responsável fora da equipe de referência ou tipo inativo: 422.", tags = "Saúde Mental · PTS")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> salvarPts(@PathVariable UUID uuid, @Valid @RequestBody ProjetoTerapeuticoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvarPts(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/intervencao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra uma intervenção", description = "Realizada ou não compareceu, com resumo. No futuro ou antes do início do caso: 400. "
            + "Prevista de outro caso: 422.", tags = "Saúde Mental · PTS")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> intervencao(@PathVariable UUID uuid, @Valid @RequestBody IntervencaoSaudeMentalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarIntervencao(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/rede-apoio", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui na rede de apoio", description = "Pessoa ou serviço, com papel, vínculo e contato.", tags = "Saúde Mental · PTS")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> incluirNaRede(@PathVariable UUID uuid, @Valid @RequestBody PessoaRedeApoioRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.incluirNaRede(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/rede-apoio/{pessoaId}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Tira da rede de apoio", description = "Com motivo; fica no histórico. Já fora da rede: 422.", tags = "Saúde Mental · PTS")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> encerrarNaRede(@PathVariable UUID uuid, @PathVariable UUID pessoaId,
                                                                      @Valid @RequestBody EncerramentoRedeApoioRequestDto dto) {
        return ResponseEntity.ok(service.encerrarNaRede(uuid, pessoaId, dto));
    }
}
