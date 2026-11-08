package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CompetenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EducacaoDoTreinamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EducacaoPermanenteDto;
import com.edufelizardo.maissaudepublica.services.version1.EducacaoPermanenteService;
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
@RequestMapping(value = "/api/v1/educacao-permanente/")
@Tag(name = "Educação permanente", description = "Competências e material didático dos treinamentos (ver docs/adr/0173-educacao-permanente.md).")
public class EducacaoPermanenteController {

    private static final String TAG = "Educação permanente";

    @Autowired
    private EducacaoPermanenteService service;

    @RequerPermissao({"RH.CONSULTAR", "RH.GERENCIAR"})
    @GetMapping(value = "competencia", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Competências", description = "O catálogo.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EducacaoPermanenteDto.Competencia>> competencias() {
        return ResponseEntity.ok(service.competencias());
    }

    @RequerPermissao({"RH.GERENCIAR"})
    @PostMapping(value = "competencia", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma competência", description = "Código repetido: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EducacaoPermanenteDto.Competencia> criarCompetencia(@Valid @RequestBody CompetenciaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarCompetencia(dto));
    }

    @RequerPermissao({"RH.GERENCIAR"})
    @PatchMapping(value = "competencia/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita uma competência", description = "Código repetido: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EducacaoPermanenteDto.Competencia> atualizarCompetencia(@PathVariable UUID uuid, @Valid @RequestBody CompetenciaRequestDto dto) {
        return ResponseEntity.ok(service.atualizarCompetencia(uuid, dto));
    }

    @RequerPermissao({"RH.CONSULTAR", "RH.GERENCIAR"})
    @GetMapping(value = "treinamento", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Competências e conteúdos dos treinamentos", description = "Todo o catálogo de treinamentos.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EducacaoPermanenteDto.Treinamento>> treinamentos() {
        return ResponseEntity.ok(service.treinamentos());
    }

    @RequerPermissao({"RH.GERENCIAR"})
    @PutMapping(value = "treinamento/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Liga competências e conteúdos ao treinamento", description = "Substitui as ligações. Competência inativa nova ou conteúdo sem revisão: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EducacaoPermanenteDto.Treinamento> ligar(@PathVariable UUID uuid, @Valid @RequestBody EducacaoDoTreinamentoRequestDto dto) {
        return ResponseEntity.ok(service.ligar(uuid, dto));
    }

    @RequerPermissao({"RH.GERENCIAR"})
    @GetMapping(value = "conteudo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Conteúdos disponíveis", description = "A versão em vigor dos conteúdos que passaram pela revisão técnica.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EducacaoPermanenteDto.Conteudo>> conteudos() {
        return ResponseEntity.ok(service.conteudosDisponiveis());
    }

    @RequerPermissao({"CONTEUDO.EDITAR", "CONTEUDO.REVISAR", "COMUNICACAO.PUBLICAR", "RH.CONSULTAR", "RH.GERENCIAR"})
    @GetMapping(value = "uso-dos-conteudos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Uso dos conteúdos", description = "Em quais treinamentos cada conteúdo é usado.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EducacaoPermanenteDto.UsoDoConteudo>> usoDosConteudos() {
        return ResponseEntity.ok(service.usoDosConteudos());
    }

    @RequerPermissao({"RH.CONSULTAR", "RH.GERENCIAR"})
    @GetMapping(value = "profissional/{matricula}/competencias", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Competências do profissional", description = "Pelas participações concluídas; vencida quando a validade passou.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<EducacaoPermanenteDto.CompetenciaDoProfissional>> doProfissional(@PathVariable String matricula) {
        return ResponseEntity.ok(service.doProfissional(matricula));
    }
}
