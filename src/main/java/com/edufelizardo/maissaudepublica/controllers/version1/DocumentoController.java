package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TipoDocumentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DocumentosDoPacienteDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TipoDocumentoDto;
import com.edufelizardo.maissaudepublica.services.version1.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Documentos", description = "Anexos com versões, fora do banco (ver docs/adr/0172-documentos-anexos.md).")
public class DocumentoController {

    private static final String TAG = "Documentos";

    @Autowired
    private DocumentoService service;

    @RequerPermissao({"DOCUMENTO.ANEXAR", "PRONTUARIO.CONSULTAR", "ADMINISTRATIVO.CONSULTAR", "ADMINISTRATIVO.GERENCIAR"})
    @GetMapping(value = "tipo-documento", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Tipos de documento", description = "O catálogo.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TipoDocumentoDto>> tipos() {
        return ResponseEntity.ok(service.tipos());
    }

    @RequerPermissao({"ADMINISTRATIVO.GERENCIAR"})
    @PostMapping(value = "tipo-documento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um tipo de documento", description = "Código repetido: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TipoDocumentoDto> criarTipo(@Valid @RequestBody TipoDocumentoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarTipo(dto));
    }

    @RequerPermissao({"ADMINISTRATIVO.GERENCIAR"})
    @PatchMapping(value = "tipo-documento/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um tipo de documento", description = "Código repetido: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TipoDocumentoDto> atualizarTipo(@PathVariable UUID uuid, @Valid @RequestBody TipoDocumentoRequestDto dto) {
        return ResponseEntity.ok(service.atualizarTipo(uuid, dto));
    }

    @RequerPermissao({"PRONTUARIO.CONSULTAR", "DOCUMENTO.ANEXAR"})
    @AuditarLeitura
    @GetMapping(value = "paciente/{pacienteId}/documentos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Documentos do paciente", description = "Quem lê o prontuário vê tudo, com o vínculo; quem só anexa vê o que anexou.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<DocumentosDoPacienteDto> doPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.doPaciente(pacienteId));
    }

    @RequerPermissao({"DOCUMENTO.ANEXAR"})
    @PostMapping(value = "paciente/{pacienteId}/documento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Anexa um documento", description = "PDF, JPG ou PNG, até 10 MB; o tipo é conferido pelo conteúdo (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<DocumentosDoPacienteDto> anexar(@PathVariable UUID pacienteId, @RequestParam UUID tipoId, @RequestParam UUID unidadeId,
                                                          @RequestParam String titulo,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDocumento,
                                                          @RequestParam MultipartFile arquivo) {
        return ResponseEntity.ok(service.anexar(pacienteId, tipoId, unidadeId, titulo, dataDocumento, arquivo));
    }

    @RequerPermissao({"DOCUMENTO.ANEXAR"})
    @PostMapping(value = "documento/{uuid}/versao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Envia nova versão", description = "Documento retirado não recebe versão (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<DocumentosDoPacienteDto> novaVersao(@PathVariable UUID uuid, @RequestParam MultipartFile arquivo) {
        return ResponseEntity.ok(service.novaVersao(uuid, arquivo));
    }

    @RequerPermissao({"DOCUMENTO.ANEXAR"})
    @PostMapping(value = "documento/{uuid}/retirada", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retira o documento", description = "Com o motivo. Nada se apaga.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<DocumentosDoPacienteDto> retirar(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.retirar(uuid, dto));
    }

    @RequerPermissao({"PRONTUARIO.CONSULTAR"})
    @AuditarLeitura
    @GetMapping(value = "documento/versao/{uuid}/arquivo")
    @Operation(summary = "Abre o arquivo da versão", description = "Sempre como anexo, com o vínculo do prontuário e o registro de quem leu.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> abrir(@PathVariable UUID uuid) {
        DocumentoService.ArquivoDoDocumento a = service.abrir(uuid);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(a.versao().getNomeArquivo(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(a.versao().getTipoConteudo()))
                .body(a.conteudo());
    }
}
