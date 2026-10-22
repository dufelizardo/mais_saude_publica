package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoOdontologicaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EspecialidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AvaliacaoOdontologicaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EspecialidadeDto;
import com.edufelizardo.maissaudepublica.services.version1.AvaliacaoOdontologicaService;
import com.edufelizardo.maissaudepublica.services.version1.EspecialidadeService;
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
@RequestMapping(value = "/api/v1/")
@Tag(name = "Especialidades e Saúde Bucal", description = "Catálogo de especialidades do atendimento e da agenda, e a avaliação odontológica "
        + "(ver docs/adr/0134-saude-bucal-base.md).")
public class SaudeBucalController {

    private static final String TAG = "Especialidades e Saúde Bucal";

    @Autowired
    private EspecialidadeService especialidadeService;

    @Autowired
    private AvaliacaoOdontologicaService avaliacaoService;

    @LiberadoParaAutenticados
    @GetMapping(value = "especialidades", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Catálogo de especialidades", description = "Ativas e inativas, por nome.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<EspecialidadeDto>> especialidades() {
        return ResponseEntity.ok(especialidadeService.listar());
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "especialidade", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra uma especialidade", description = "Código repetido: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EspecialidadeDto> criarEspecialidade(@Valid @RequestBody EspecialidadeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(especialidadeService.criar(dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PutMapping(value = "especialidade/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza uma especialidade", description = "O código não muda (422); desativar a tira de uso sem apagar.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<EspecialidadeDto> atualizarEspecialidade(@PathVariable UUID uuid, @Valid @RequestBody EspecialidadeRequestDto dto) {
        return ResponseEntity.ok(especialidadeService.atualizar(uuid, dto));
    }

    @RequerPermissao({"ODONTOLOGIA.REGISTRAR"})
    @PostMapping(value = "avaliacao-odontologica", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a avaliação odontológica", description = "No atendimento de especialidade odontológica (422). Ato do "
            + "cirurgião-dentista (ODONTOLOGIA.REGISTRAR).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AvaliacaoOdontologicaDto> avaliar(@Valid @RequestBody AvaliacaoOdontologicaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoService.criar(dto));
    }

    @RequerPermissao({"ODONTOLOGIA.REGISTRAR"})
    @PostMapping(value = "avaliacao-odontologica/{uuid}/retificacao", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retifica a avaliação odontológica", description = "Nova versão ligada à anterior (ADR-0062): mesmo atendimento (400), "
            + "motivo obrigatório (400), só a vigente (422); de outro profissional, só com supervisão (403).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AvaliacaoOdontologicaDto> retificar(@PathVariable UUID uuid, @Valid @RequestBody AvaliacaoOdontologicaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoService.retificar(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"PRONTUARIO.CONSULTAR", "ODONTOLOGIA.REGISTRAR"})
    @GetMapping(value = "avaliacao-odontologica/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Avaliação odontológica", description = "Com a retificação (retificado, retificadoPorUuid). Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<AvaliacaoOdontologicaDto> avaliacao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(avaliacaoService.buscarPorId(uuid));
    }
}
