package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CanalComunicacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ComunicadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConteudoEducativoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PublicarConteudoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CanalComunicacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComunicadoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ConteudoEducativoDto;
import com.edufelizardo.maissaudepublica.services.version1.ComunicacaoService;
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
@RequestMapping(value = "/api/v1/comunicacao/")
@Tag(name = "Comunicação", description = "Comunicado, conteúdo com revisão técnica, canais e publicação (ver docs/adr/0168-comunicacao-basica.md).")
public class ComunicacaoController {

    private static final String TAG = "Comunicação";

    @Autowired
    private ComunicacaoService service;

    @RequerPermissao({"COMUNICACAO.PUBLICAR", "CONTEUDO.EDITAR", "CONTEUDO.REVISAR"})
    @GetMapping(value = "canal", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Canais", description = "O catálogo de canais.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CanalComunicacaoDto>> canais() {
        return ResponseEntity.ok(service.canais());
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PostMapping(value = "canal", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um canal", description = "Nome repetido: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CanalComunicacaoDto> criarCanal(@Valid @RequestBody CanalComunicacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarCanal(dto));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PatchMapping(value = "canal/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita o canal", description = "O tipo não muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CanalComunicacaoDto> atualizarCanal(@PathVariable UUID uuid, @Valid @RequestBody CanalComunicacaoRequestDto dto) {
        return ResponseEntity.ok(service.atualizarCanal(uuid, dto));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR", "CONTEUDO.EDITAR", "CONTEUDO.REVISAR"})
    @GetMapping(value = "comunicado", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Comunicados", description = "Os da rede e os das unidades do acesso, com o histórico de publicação.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ComunicadoDto>> comunicados() {
        return ResponseEntity.ok(service.comunicados());
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PostMapping(value = "comunicado", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria o comunicado em rascunho", description = "População só pelo portal; profissionais fora dele (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComunicadoDto> criarComunicado(@Valid @RequestBody ComunicadoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarComunicado(dto));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PatchMapping(value = "comunicado/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita o rascunho", description = "Publicado não se edita (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComunicadoDto> atualizarComunicado(@PathVariable UUID uuid, @Valid @RequestBody ComunicadoRequestDto dto) {
        return ResponseEntity.ok(service.atualizarComunicado(uuid, dto));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PostMapping(value = "comunicado/{uuid}/publicacao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Publica o comunicado", description = "Nos canais dele; o emergencial pede EMERGENCIA.COORDENAR (403). A falta de SMTP fica no resultado.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComunicadoDto> publicarComunicado(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.publicarComunicado(uuid));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PostMapping(value = "comunicado/{uuid}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra o comunicado", description = "Com o motivo; só o publicado (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComunicadoDto> encerrarComunicado(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.encerrarComunicado(uuid, dto));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR", "CONTEUDO.EDITAR", "CONTEUDO.REVISAR"})
    @GetMapping(value = "conteudo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Conteúdos educativos", description = "Todas as versões, com a situação e o histórico.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ConteudoEducativoDto>> conteudos() {
        return ResponseEntity.ok(service.conteudos());
    }

    @RequerPermissao({"CONTEUDO.EDITAR"})
    @PostMapping(value = "conteudo", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria o conteúdo em rascunho", description = "Com validade.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ConteudoEducativoDto> criarConteudo(@Valid @RequestBody ConteudoEducativoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarConteudo(dto));
    }

    @RequerPermissao({"CONTEUDO.EDITAR"})
    @PatchMapping(value = "conteudo/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita o rascunho", description = "Só o rascunho muda (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ConteudoEducativoDto> atualizarConteudo(@PathVariable UUID uuid, @Valid @RequestBody ConteudoEducativoRequestDto dto) {
        return ResponseEntity.ok(service.atualizarConteudo(uuid, dto));
    }

    @RequerPermissao({"CONTEUDO.EDITAR"})
    @PostMapping(value = "conteudo/{uuid}/envio-revisao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Envia à revisão técnica", description = "Só o rascunho (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ConteudoEducativoDto> enviarParaRevisao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.enviarParaRevisao(uuid));
    }

    @RequerPermissao({"CONTEUDO.REVISAR"})
    @PostMapping(value = "conteudo/{uuid}/aprovacao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Aprova o conteúdo", description = "Quem escreveu não aprova o próprio conteúdo (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ConteudoEducativoDto> aprovar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.aprovar(uuid));
    }

    @RequerPermissao({"CONTEUDO.REVISAR"})
    @PostMapping(value = "conteudo/{uuid}/devolucao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Devolve ao autor", description = "Com o motivo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ConteudoEducativoDto> devolver(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.devolver(uuid, dto));
    }

    @RequerPermissao({"COMUNICACAO.PUBLICAR"})
    @PostMapping(value = "conteudo/{uuid}/publicacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Publica o conteúdo", description = "Aprovado e na validade (422); a versão anterior fica substituída.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ConteudoEducativoDto> publicarConteudo(@PathVariable UUID uuid, @Valid @RequestBody PublicarConteudoRequestDto dto) {
        return ResponseEntity.ok(service.publicarConteudo(uuid, dto));
    }

    @RequerPermissao({"CONTEUDO.EDITAR"})
    @PostMapping(value = "conteudo/{uuid}/nova-versao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria a nova versão", description = "Do publicado; uma em andamento por conteúdo (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ConteudoEducativoDto> novaVersao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.novaVersao(uuid));
    }
}
