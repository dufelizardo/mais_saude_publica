package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AnulacaoVacinacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EsquemaVacinalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ImunobiologicoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegistroVacinacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AtrasadoVacinacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CadernetaVacinacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CoberturaVacinalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EsquemaVacinalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ImunobiologicoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RegistroVacinacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VacinacaoDoDiaDto;
import com.edufelizardo.maissaudepublica.services.version1.ImunizacaoCatalogoService;
import com.edufelizardo.maissaudepublica.services.version1.CoberturaVacinalService;
import com.edufelizardo.maissaudepublica.services.version1.VacinacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Imunização", description = "Imunobiológicos, calendário vacinal, registro de vacinação, caderneta e sala de vacina "
        + "(ver docs/adr/0147-imunizacao.md).")
public class VacinacaoController {

    private static final String TAG = "Imunização";

    @Autowired
    private ImunizacaoCatalogoService catalogoService;

    @Autowired
    private VacinacaoService service;

    @Autowired
    private CoberturaVacinalService coberturaService;

    // ── Catálogo e calendário ──

    @LiberadoParaAutenticados
    @GetMapping(value = "imunobiologicos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Imunobiológicos", description = "O catálogo, em ordem de nome.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ImunobiologicoDto>> imunobiologicos() {
        return ResponseEntity.ok(catalogoService.imunobiologicos());
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PostMapping(value = "imunobiologico", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um imunobiológico", description = "Código único (409).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ImunobiologicoDto> criarImunobiologico(@Valid @RequestBody ImunobiologicoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.criarImunobiologico(dto));
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PutMapping(value = "imunobiologico/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta um imunobiológico", description = "Inclusive desativar.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ImunobiologicoDto> atualizarImunobiologico(@PathVariable UUID uuid, @Valid @RequestBody ImunobiologicoRequestDto dto) {
        return ResponseEntity.ok(catalogoService.atualizarImunobiologico(uuid, dto));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "calendario-vacinal", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Calendário vacinal", description = "As doses do calendário, na ordem, com a vigência.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EsquemaVacinalDto>> calendario() {
        return ResponseEntity.ok(catalogoService.calendario());
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PostMapping(value = "esquema-vacinal", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra uma dose do calendário", description = "Idades em ordem e vigência válida (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EsquemaVacinalDto> criarEsquema(@Valid @RequestBody EsquemaVacinalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.criarEsquema(dto));
    }

    @RequerPermissao({"IMUNIZACAO.GERENCIAR"})
    @PutMapping(value = "esquema-vacinal/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta uma dose do calendário", description = "Para mudar o calendário, encerre a vigência e cadastre a nova.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EsquemaVacinalDto> atualizarEsquema(@PathVariable UUID uuid, @Valid @RequestBody EsquemaVacinalRequestDto dto) {
        return ResponseEntity.ok(catalogoService.atualizarEsquema(uuid, dto));
    }

    // ── Vacinação ──

    @RequerPermissao({"VACINACAO.REGISTRAR"})
    @PostMapping(value = "vacinacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra uma dose", description = "Aplicada (com quem aplicou e o lote, 400) ou transcrita da caderneta (com a "
            + "data, 400). Dose repetida, 409; fora da idade ou do intervalo do calendário, só com a justificativa (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RegistroVacinacaoDto> registrar(@Valid @RequestBody RegistroVacinacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @RequerPermissao({"VACINACAO.REGISTRAR"})
    @PostMapping(value = "vacinacao/{uuid}/retificacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retifica uma dose", description = "Nova versão ligada à anterior, do mesmo paciente, com o motivo (ADR-0062). Só a "
            + "vigente, pelo autor ou pela supervisão.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RegistroVacinacaoDto> retificar(@PathVariable UUID uuid, @Valid @RequestBody RegistroVacinacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.retificar(uuid, dto));
    }

    @RequerPermissao({"VACINACAO.REGISTRAR"})
    @PostMapping(value = "vacinacao/{uuid}/anulacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Anula uma dose registrada por engano", description = "Sai da caderneta, com o motivo; fica na trilha.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RegistroVacinacaoDto> anular(@PathVariable UUID uuid, @Valid @RequestBody AnulacaoVacinacaoRequestDto dto) {
        return ResponseEntity.ok(service.anular(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"PRONTUARIO.CONSULTAR", "VACINACAO.REGISTRAR"})
    @GetMapping(value = "paciente/{pacienteId}/vacinacao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Caderneta de vacinação", description = "As doses, o calendário com a situação (aplicada, atrasada, próxima, futura, "
            + "fora da idade) e as recomendações. Com as regras do prontuário (vínculo assistencial ou acesso justificado).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CadernetaVacinacaoDto> caderneta(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.caderneta(pacienteId));
    }

    @AuditarLeitura
    @RequerPermissao({"VACINACAO.REGISTRAR"})
    @GetMapping(value = "vacinacao/dia", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Sala de vacina do dia", description = "As doses aplicadas na unidade no dia (padrão: hoje) e a contagem por "
            + "imunobiológico.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<VacinacaoDoDiaDto> doDia(@RequestParam UUID unidadeId,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.doDia(unidadeId, data));
    }

    @AuditarLeitura
    @RequerPermissao({"VACINACAO.REGISTRAR"})
    @GetMapping(value = "vacinacao/atrasados", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atrasados da unidade", description = "Os adscritos às equipes da unidade com dose atrasada, para a busca ativa, "
            + "com a equipe e a microárea (filtros), o agente comunitário e onde mora. Equipe de outra unidade, 400.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<AtrasadoVacinacaoDto>> atrasados(@RequestParam UUID unidadeId, @RequestParam(required = false) UUID equipeId,
                                                                @RequestParam(required = false) UUID microareaId) {
        return ResponseEntity.ok(service.atrasados(unidadeId, equipeId, microareaId));
    }

    @RequerPermissao({"VACINACAO.REGISTRAR", "IMUNIZACAO.GERENCIAR", "INDICADOR.CONSULTAR"})
    @GetMapping(value = "vacinacao/cobertura", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cobertura vacinal", description = "Pelo cadastro: por vacina de referência, a coorte, as vacinadas, a cobertura "
            + "contra a meta, a homogeneidade (entre vacinas e entre territórios) e a taxa de abandono. Sem filtro, as unidades do acesso por "
            + "unidade; com a unidade, por equipe; com a equipe, por microárea; com a equipe e a microárea, a microárea. Só números.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CoberturaVacinalDto> cobertura(@RequestParam(required = false) UUID unidadeId, @RequestParam(required = false) UUID equipeId,
                                                         @RequestParam(required = false) UUID microareaId,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(coberturaService.cobertura(unidadeId, equipeId, microareaId, data));
    }
}
