package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CancelamentoItemExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ComunicacaoCriticoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EntregaResultadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ResultadoAvisoExameDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ColetaExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PedidoExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RejeicaoAmostraRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResultadoExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoResultadoExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ItemTrabalhoExameDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PedidoExameResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PedidoExameResumoDto;
import com.edufelizardo.maissaudepublica.services.version1.PedidoExameService;

@RestController
@RequestMapping(value = "/api/v1/pedido-exame/")
@Tag(name = "Laboratório", description = "Pedido de exames, coleta em amostras, resultado, liberação e retificação; lista de trabalho do laboratório (ver docs/adr/0093-laboratorio-backend.md).")
public class PedidoExameController {

    @Autowired
    private PedidoExameService service;

    @Autowired
    private com.edufelizardo.maissaudepublica.services.version1.LaudoExameService laudoService;

    @RequerPermissao({"EXAME.SOLICITAR", "LABORATORIO.COLETAR", "LABORATORIO.ANALISAR", "LABORATORIO.LIBERAR", "IMAGEM.REALIZAR", "IMAGEM.LAUDAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}/laudo.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Laudo do pedido em PDF", description = "Os resultados liberados e assinados, com o código de verificação e o QR code. "
            + "Sem resultado novo, devolve a mesma emissão; com resultado novo, retificação ou adendo, emite outra versão. Nada liberado: 422. "
            + "Fora do escopo: 403. Leitura auditada (ADR-0183).", tags = "Laboratório")
    @ApiErrorResponsesBusca
    public ResponseEntity<byte[]> laudoPdf(@PathVariable UUID uuid) {
        com.edufelizardo.maissaudepublica.models.EmissaoLaudoExame e = laudoService.emitir(uuid);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"laudo-" + e.getCodigo() + ".pdf\"")
                .header("X-Codigo-Verificacao", e.getCodigo())
                .header(org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "X-Codigo-Verificacao, Content-Disposition")
                .body(e.getPdf());
    }

    @RequerPermissao({"EXAME.SOLICITAR", "LABORATORIO.COLETAR", "LABORATORIO.ANALISAR", "LABORATORIO.LIBERAR", "IMAGEM.REALIZAR", "IMAGEM.LAUDAR"})
    @GetMapping(value = "{uuid}/emissoes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Emissões do laudo do pedido", description = "Versão, código, hash e se é a vigente; da mais nova para a mais velha (ADR-0183).",
            tags = "Laboratório")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<com.edufelizardo.maissaudepublica.models.dtos.version1.response.EmissaoLaudoDtos.Emissao>> emissoes(@PathVariable UUID uuid) {
        return ResponseEntity.ok(laudoService.emissoes(uuid));
    }

    @RequerPermissao({"EXAME.SOLICITAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Pede exames para um paciente",
            description = "Exame fora de uso ou paciente inativo: 422. Exame repetido no pedido, atendimento de outro paciente ou CID inválido: 400.",
            tags = "Laboratório")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> create(@Valid @RequestBody PedidoExameRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sucesso("Pedido de exames registrado com sucesso!", service.solicitar(dto)));
    }

    @RequerPermissao({"LABORATORIO.COLETAR"})
    @PostMapping(value = "{uuid}/coleta", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a coleta", description = "Uma amostra por material; sem itens, coleta todos os que aguardam; sem laboratório, a unidade da coleta analisa. Nada aguardando: 422.", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> coletar(@PathVariable UUID uuid, @Valid @RequestBody ColetaExameRequestDto dto) {
        return ResponseEntity.ok(sucesso("Coleta registrada!", service.coletar(uuid, dto)));
    }


    @RequerPermissao({"LABORATORIO.ANALISAR"})
    @PostMapping(value = "amostra/{uuid}/rejeicao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rejeita uma amostra", description = "Os exames dela voltam para recoleta. Amostra já rejeitada ou com resultado liberado: 422.", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> rejeitarAmostra(@PathVariable UUID uuid, @Valid @RequestBody RejeicaoAmostraRequestDto dto) {
        return ResponseEntity.ok(sucesso("Amostra rejeitada; exames voltam para recoleta.", service.rejeitarAmostra(uuid, dto)));
    }


    @RequerPermissao({"LABORATORIO.ANALISAR"})
    @PostMapping(value = "item/{uuid}/resultado", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra o resultado de um exame", description = "Numérico ou texto, conforme o catálogo (400 se faltar). A referência é copiada e a interpretação calculada. Só COLETADO ou RESULTADO_REGISTRADO (422).", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> registrarResultado(@PathVariable UUID uuid, @Valid @RequestBody ResultadoExameRequestDto dto) {
        return ResponseEntity.ok(sucesso("Resultado registrado!", service.registrarResultado(uuid, dto)));
    }


    @RequerPermissao({"LABORATORIO.LIBERAR"})
    @PostMapping(value = "item/{uuid}/liberacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Libera o resultado", description = "A partir daqui o resultado não muda; correção é retificação. Só RESULTADO_REGISTRADO (422).", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> liberar(@PathVariable UUID uuid, @Valid @RequestBody ProfissionalExameRequestDto dto) {
        return ResponseEntity.ok(sucesso("Resultado liberado!", service.liberar(uuid, dto)));
    }


    @RequerPermissao({"LABORATORIO.LIBERAR"})
    @PostMapping(value = "item/{uuid}/retificacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retifica um resultado liberado", description = "Grava outro resultado, ligado ao corrigido, já liberado. Motivo obrigatório. Só LIBERADO (422).", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> retificar(@PathVariable UUID uuid, @Valid @RequestBody RetificacaoResultadoExameRequestDto dto) {
        return ResponseEntity.ok(sucesso("Resultado retificado!", service.retificar(uuid, dto)));
    }


    @RequerPermissao({"IMAGEM.REALIZAR"})
    @PostMapping(value = "item/{uuid}/realizacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a realização do exame de imagem", description = "No serviço de imagem do pedido (403). Exame laboratorial, já realizado ou data no futuro: 422. Contraste sem descrição: 400 (ADR-0182).", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> realizar(@PathVariable UUID uuid,
            @Valid @RequestBody com.edufelizardo.maissaudepublica.models.dtos.version1.request.RealizacaoExameImagemRequestDto dto) {
        return ResponseEntity.ok(sucesso("Exame realizado!", service.realizar(uuid, dto)));
    }

    @RequerPermissao({"IMAGEM.LAUDAR"})
    @PostMapping(value = "item/{uuid}/laudo", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra e assina o laudo de imagem", description = "O laudo sai liberado. Só exame realizado (422). Classificação NORMAL, ALTERADO ou ACHADO_CRITICO; link das imagens só https (400) (ADR-0182).", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> laudar(@PathVariable UUID uuid,
            @Valid @RequestBody com.edufelizardo.maissaudepublica.models.dtos.version1.request.LaudoImagemRequestDto dto) {
        return ResponseEntity.ok(sucesso("Laudo liberado!", service.laudar(uuid, dto)));
    }

    @RequerPermissao({"IMAGEM.LAUDAR"})
    @PostMapping(value = "item/{uuid}/adendo", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra adendo ao laudo de imagem", description = "Grava outro laudo ligado ao anterior, já liberado. Motivo obrigatório (400). Só laudo liberado (422) (ADR-0182).", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> adendo(@PathVariable UUID uuid,
            @Valid @RequestBody com.edufelizardo.maissaudepublica.models.dtos.version1.request.LaudoImagemRequestDto dto) {
        return ResponseEntity.ok(sucesso("Adendo registrado!", service.adendo(uuid, dto)));
    }

    @RequerPermissao({"EXAME.SOLICITAR", "LABORATORIO.ANALISAR", "IMAGEM.REALIZAR"})
    @PostMapping(value = "item/{uuid}/cancelamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancela um exame do pedido", description = "Quem pediu ou o laboratório, antes da liberação (422). Motivo obrigatório.", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> cancelar(@PathVariable UUID uuid, @Valid @RequestBody CancelamentoItemExameRequestDto dto) {
        return ResponseEntity.ok(sucesso("Exame cancelado!", service.cancelar(uuid, dto)));
    }

    @RequerPermissao({"EXAME.SOLICITAR"})
    @GetMapping(value = "para-ciencia", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Resultados liberados aguardando a ciência de quem pediu",
            description = "Do profissional logado (outra matrícula: 403); sem login, a matrícula é obrigatória (400). Críticos primeiro. Sem valores (ADR-0181).",
            tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResultadoAvisoExameDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ResultadoAvisoExameDto>> paraCiencia(@RequestParam(required = false) String matricula) {
        return ResponseEntity.ok(service.paraCiencia(matricula));
    }

    @RequerPermissao({"EXAME.SOLICITAR"})
    @PostMapping(value = "item/{uuid}/ciencia", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a ciência do resultado", description = "Só quem pediu (422), resultado liberado (422), uma vez por resultado (409). A retificação reabre a ciência (ADR-0181).", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> darCiencia(@PathVariable UUID uuid, @Valid @RequestBody ProfissionalExameRequestDto dto) {
        return ResponseEntity.ok(sucesso("Ciência registrada!", service.darCiencia(uuid, dto)));
    }

    @RequerPermissao({"EXAME.SOLICITAR"})
    @PostMapping(value = "{uuid}/ciencia", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a ciência de todos os resultados liberados do pedido", description = "Só quem pediu (422). Nenhum pendente: 409 (ADR-0181).", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> darCienciaDoPedido(@PathVariable UUID uuid, @Valid @RequestBody ProfissionalExameRequestDto dto) {
        return ResponseEntity.ok(sucesso("Ciência registrada!", service.darCienciaDoPedido(uuid, dto)));
    }

    @RequerPermissao({"LABORATORIO.LIBERAR", "IMAGEM.LAUDAR"})
    @GetMapping(value = "criticos-a-comunicar", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Resultados críticos liberados sem comunicação nem ciência",
            description = "Nos laboratórios do escopo, do mais antigo ao mais novo. Sem valores (ADR-0181).", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResultadoAvisoExameDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ResultadoAvisoExameDto>> criticosAComunicar() {
        return ResponseEntity.ok(service.criticosAComunicar());
    }

    @RequerPermissao({"LABORATORIO.LIBERAR", "IMAGEM.LAUDAR"})
    @PostMapping(value = "item/{uuid}/comunicacao-critico", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a comunicação do valor crítico", description = "Quem recebeu, meio e quando. Resultado não crítico ou data no futuro: 422. Já comunicado: 409 (ADR-0181).", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> comunicarCritico(@PathVariable UUID uuid, @Valid @RequestBody ComunicacaoCriticoRequestDto dto) {
        return ResponseEntity.ok(sucesso("Comunicação do crítico registrada!", service.comunicarCritico(uuid, dto)));
    }

    @RequerPermissao({"LABORATORIO.COLETAR", "EXAME.SOLICITAR"})
    @PostMapping(value = "{uuid}/entrega", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a entrega do resultado ao paciente", description = "Na unidade informada. Exame em andamento ou nada liberado: 422. Terceiro sem nome ou documento: 400. Segunda via é outra entrega (ADR-0181).", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> entregar(@PathVariable UUID uuid, @Valid @RequestBody EntregaResultadoRequestDto dto) {
        return ResponseEntity.ok(sucesso("Entrega do resultado registrada!", service.entregar(uuid, dto)));
    }

    @RequerPermissao({"LABORATORIO.COLETAR", "LABORATORIO.ANALISAR", "LABORATORIO.LIBERAR", "IMAGEM.REALIZAR", "IMAGEM.LAUDAR"})
    @GetMapping(value = "trabalho", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista de trabalho por exame",
            description = "etapa=PARA_COLETAR, EM_ANALISE ou PARA_LIBERAR (laboratório) e PARA_REALIZAR ou PARA_LAUDAR (imagem, ADR-0182), nas unidades do escopo; urgente primeiro. Sem valores.",
            tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ItemTrabalhoExameDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ItemTrabalhoExameDto>> trabalho(@RequestParam String etapa) {
        return ResponseEntity.ok(service.trabalho(etapa));
    }

    @RequerPermissao({"EXAME.SOLICITAR", "LABORATORIO.COLETAR", "LABORATORIO.ANALISAR", "LABORATORIO.LIBERAR", "IMAGEM.REALIZAR", "IMAGEM.LAUDAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os pedidos, do mais recente ao mais antigo",
            description = "Sem valores. Filtros: pacienteId e situacao (AGUARDANDO_COLETA, EM_ANDAMENTO, CONCLUIDO, CANCELADO).",
            tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PedidoExameResumoDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<PedidoExameResumoDto>> findAll(@RequestParam(required = false) UUID pacienteId,
                                                              @RequestParam(required = false) String situacao) {
        return ResponseEntity.ok(service.listar(pacienteId, situacao));
    }

    @RequerPermissao({"EXAME.SOLICITAR", "LABORATORIO.COLETAR", "LABORATORIO.ANALISAR", "LABORATORIO.LIBERAR", "IMAGEM.REALIZAR", "IMAGEM.LAUDAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Detalhe do pedido, com indicação clínica, resultados, amostras e eventos",
            description = "Leitura auditada (dado de saúde). Fora do escopo das unidades do pedido: 403.", tags = "Laboratório")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoExameResponseDto.class))
    })
    @ApiErrorResponsesBusca
    public ResponseEntity<PedidoExameResponseDto> findById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    private static SuccessResponseDto sucesso(String mensagem, PedidoExameResponseDto p) {
        return new SuccessResponseDto(mensagem, "Id: " + p.getUuid() + ", Exames: " + p.getTotalExames() + ", Liberados: " + p.getLiberados()
                + ", Situação: " + p.getSituacao());
    }
}
