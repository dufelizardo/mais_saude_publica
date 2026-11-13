package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConclusaoDenunciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DenunciaSanitariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EstabelecimentoReguladoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InspecaoSanitariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LicencaSanitariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RoteiroInspecaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DocumentosDoPacienteDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VigilanciaSanitariaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.DonoDocumento;
import com.edufelizardo.maissaudepublica.models.enuns.GrauRiscoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDenuncia;
import com.edufelizardo.maissaudepublica.services.version1.DocumentoService;
import com.edufelizardo.maissaudepublica.services.version1.VigilanciaSanitariaService;
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
@RequestMapping(value = "/api/v1/vigilancia-sanitaria/")
@Tag(name = "Vigilância sanitária", description = "Estabelecimentos regulados, licença sanitária, denúncias, roteiros e inspeções com as irregularidades "
        + "(ver a ADR-0199).")
public class VigilanciaSanitariaController {

    private static final String TAG = "Vigilância sanitária";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private VigilanciaSanitariaService service;

    // ── Estabelecimentos ───────────────────────────────────────────────────────────────────────────

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @GetMapping(value = "estabelecimentos", produces = JSON)
    @Operation(summary = "Estabelecimentos regulados", description = "busca (nome ou documento) e risco filtram; com a licença atual e as irregularidades abertas.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<VigilanciaSanitariaDtos.Estabelecimento>> estabelecimentos(@RequestParam(required = false) String busca,
                                                                                         @RequestParam(required = false) GrauRiscoSanitario risco) {
        return ResponseEntity.ok(service.estabelecimentos(busca, risco));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @GetMapping(value = "estabelecimento/{uuid}", produces = JSON)
    @Operation(summary = "Estabelecimento com o histórico", description = "Licenças, inspeções, irregularidades e documentos.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<VigilanciaSanitariaDtos.EstabelecimentoDetalhe> estabelecimento(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.estabelecimento(uuid));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @PostMapping(value = "estabelecimento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cadastra o estabelecimento", description = "CPF ou CNPJ válido (400) e único (409); ao menos uma atividade com o risco.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<VigilanciaSanitariaDtos.Estabelecimento> criarEstabelecimento(@Valid @RequestBody EstabelecimentoReguladoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarEstabelecimento(dto));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @PutMapping(value = "estabelecimento/{uuid}", produces = JSON, consumes = JSON)
    @Operation(summary = "Altera o estabelecimento", description = "O cadastro, as atividades e se está ativo.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<VigilanciaSanitariaDtos.Estabelecimento> atualizarEstabelecimento(@PathVariable UUID uuid,
                                                                                           @Valid @RequestBody EstabelecimentoReguladoRequestDto dto) {
        return ResponseEntity.ok(service.atualizarEstabelecimento(uuid, dto));
    }

    // ── Licenças ───────────────────────────────────────────────────────────────────────────────────

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @GetMapping(value = "licencas", produces = JSON)
    @Operation(summary = "Licenças sanitárias", description = "Com a situação calculada: vigente, vencida, dispensada, suspensa ou cassada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<VigilanciaSanitariaDtos.Licenca>> licencas() {
        return ResponseEntity.ok(service.licencas());
    }

    @RequerPermissao({"VISA.GERENCIAR"})
    @PostMapping(value = "estabelecimento/{uuid}/licenca", produces = JSON, consumes = JSON)
    @Operation(summary = "Concede a licença ou a dispensa", description = "Risco I dispensado; risco III com inspeção satisfatória em 12 meses (422); "
            + "renovação a partir de 60 dias do vencimento (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<VigilanciaSanitariaDtos.Licenca> concederLicenca(@PathVariable UUID uuid, @Valid @RequestBody LicencaSanitariaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.concederLicenca(uuid, dto));
    }

    // ── Roteiros ───────────────────────────────────────────────────────────────────────────────────

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @GetMapping(value = "roteiros", produces = JSON)
    @Operation(summary = "Roteiros de inspeção", description = "Com os itens da versão mais recente.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<VigilanciaSanitariaDtos.Roteiro>> roteiros() {
        return ResponseEntity.ok(service.roteiros());
    }

    @RequerPermissao({"VISA.GERENCIAR"})
    @PostMapping(value = "roteiro", produces = JSON, consumes = JSON)
    @Operation(summary = "Cadastra o roteiro", description = "Código único (409), com a primeira versão dos itens.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<VigilanciaSanitariaDtos.Roteiro> criarRoteiro(@Valid @RequestBody RoteiroInspecaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarRoteiro(dto));
    }

    @RequerPermissao({"VISA.GERENCIAR"})
    @PostMapping(value = "roteiro/{uuid}/versao", produces = JSON, consumes = JSON)
    @Operation(summary = "Nova versão do roteiro", description = "As inspeções antigas seguem com a versão que responderam.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<VigilanciaSanitariaDtos.Roteiro> novaVersaoRoteiro(@PathVariable UUID uuid, @Valid @RequestBody RoteiroInspecaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.novaVersaoRoteiro(uuid, dto));
    }

    // ── Denúncias ──────────────────────────────────────────────────────────────────────────────────

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @GetMapping(value = "denuncias", produces = JSON)
    @Operation(summary = "Denúncias", description = "situacao filtra; o denunciante só aparece para quem gerencia a VISA.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<VigilanciaSanitariaDtos.Denuncia>> denuncias(@RequestParam(required = false) SituacaoDenuncia situacao) {
        return ResponseEntity.ok(service.denuncias(situacao));
    }

    @RequerPermissao({"VISA.GERENCIAR"})
    @PostMapping(value = "denuncia", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a denúncia", description = "Anônima ou identificada; o estabelecimento cadastrado ou descrito (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<VigilanciaSanitariaDtos.Denuncia> registrarDenuncia(@Valid @RequestBody DenunciaSanitariaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarDenuncia(dto));
    }

    @RequerPermissao({"VISA.GERENCIAR"})
    @PostMapping(value = "denuncia/{uuid}/conclusao", produces = JSON, consumes = JSON)
    @Operation(summary = "Conclui a denúncia", description = "Procedente (com inspeção, 422) ou improcedente.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<VigilanciaSanitariaDtos.Denuncia> concluirDenuncia(@PathVariable UUID uuid, @Valid @RequestBody ConclusaoDenunciaRequestDto dto) {
        return ResponseEntity.ok(service.concluirDenuncia(uuid, dto));
    }

    // ── Inspeções ──────────────────────────────────────────────────────────────────────────────────

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @GetMapping(value = "inspecoes", produces = JSON)
    @Operation(summary = "Inspeções", description = "estabelecimentoId filtra.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<VigilanciaSanitariaDtos.Inspecao>> inspecoes(@RequestParam(required = false) UUID estabelecimentoId) {
        return ResponseEntity.ok(service.inspecoes(estabelecimentoId));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @GetMapping(value = "inspecao/{uuid}", produces = JSON)
    @Operation(summary = "Inspeção", description = "O roteiro respondido, os fiscais, as irregularidades e a assinatura.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<VigilanciaSanitariaDtos.Inspecao> inspecao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.inspecao(uuid));
    }

    @RequerPermissao({"VISA.INSPECIONAR"})
    @PostMapping(value = "inspecao", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a inspeção", description = "Roteiro respondido por inteiro, item não conforme com a irregularidade (400); "
            + "com login, quem registra está entre os fiscais (403) e assina.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<VigilanciaSanitariaDtos.Inspecao> registrarInspecao(@Valid @RequestBody InspecaoSanitariaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarInspecao(dto));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @GetMapping(value = "inspecao/{uuid}/termo.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Termo de inspeção em PDF", description = "Com as irregularidades, os prazos e a assinatura eletrônica.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> termo(@PathVariable UUID uuid) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"termo-inspecao-" + uuid + ".pdf\"")
                .body(service.termo(uuid));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @GetMapping(value = "pendencias", produces = JSON)
    @Operation(summary = "Pendências", description = "Licença faltando, vencida ou vencendo; prazo de correção vencido; denúncia parada há 30 dias.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<VigilanciaSanitariaDtos.Pendencias> pendencias() {
        return ResponseEntity.ok(service.pendencias());
    }

    // ── Documentos ─────────────────────────────────────────────────────────────────────────────────

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @GetMapping(value = "estabelecimento/{uuid}/documentos", produces = JSON)
    @Operation(summary = "Documentos do estabelecimento", description = "Os que não foram retirados.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<DocumentosDoPacienteDto.Item>> documentosDoEstabelecimento(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.documentos(DonoDocumento.ESTABELECIMENTO, uuid));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @PostMapping(value = "estabelecimento/{uuid}/documento", produces = JSON, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Anexa ao estabelecimento", description = "PDF, JPG ou PNG, até 10 MB; o tipo é conferido pelo conteúdo (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<DocumentosDoPacienteDto.Item>> anexarAoEstabelecimento(@PathVariable UUID uuid, @RequestParam UUID tipoId, @RequestParam String titulo,
                                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDocumento,
                                                                                   @RequestParam MultipartFile arquivo) {
        return ResponseEntity.ok(service.anexar(DonoDocumento.ESTABELECIMENTO, uuid, tipoId, titulo, dataDocumento, arquivo));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @GetMapping(value = "inspecao/{uuid}/documentos", produces = JSON)
    @Operation(summary = "Documentos da inspeção", description = "Fotos e evidências; os que não foram retirados.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<DocumentosDoPacienteDto.Item>> documentosDaInspecao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.documentos(DonoDocumento.INSPECAO_SANITARIA, uuid));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @PostMapping(value = "inspecao/{uuid}/documento", produces = JSON, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Anexa à inspeção", description = "PDF, JPG ou PNG, até 10 MB; o tipo é conferido pelo conteúdo (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<DocumentosDoPacienteDto.Item>> anexarAInspecao(@PathVariable UUID uuid, @RequestParam UUID tipoId, @RequestParam String titulo,
                                                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDocumento,
                                                                           @RequestParam MultipartFile arquivo) {
        return ResponseEntity.ok(service.anexar(DonoDocumento.INSPECAO_SANITARIA, uuid, tipoId, titulo, dataDocumento, arquivo));
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @PostMapping(value = "documento/{uuid}/retirada", consumes = JSON)
    @Operation(summary = "Retira o documento", description = "Com o motivo. Nada se apaga.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<Void> retirarDocumento(@PathVariable UUID uuid, @Valid @RequestBody MotivoRequestDto dto) {
        service.retirarDocumento(uuid, dto.getMotivo());
        return ResponseEntity.noContent().build();
    }

    @RequerPermissao({"VISA.INSPECIONAR", "VISA.GERENCIAR"})
    @AuditarLeitura
    @GetMapping(value = "documento/versao/{uuid}/arquivo")
    @Operation(summary = "Abre o arquivo", description = "Sempre como anexo; só documentos da VISA (404 nos outros).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> abrirDocumento(@PathVariable UUID uuid) {
        DocumentoService.ArquivoDoDocumento a = service.abrirDocumento(uuid);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(a.versao().getNomeArquivo(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(a.versao().getTipoConteudo()))
                .body(a.conteudo());
    }
}
