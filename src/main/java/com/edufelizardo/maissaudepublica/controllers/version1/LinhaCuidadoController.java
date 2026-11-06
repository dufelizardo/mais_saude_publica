package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoPercursoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LinhaCuidadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PercursoLinhaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegistroEtapaPercursoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LinhaCuidadoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelLinhasCuidadoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PercursoLinhaDto;
import com.edufelizardo.maissaudepublica.services.version1.LinhaCuidadoService;
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
@Tag(name = "Linhas de Cuidado", description = "O percurso assistencial do paciente pela rede, com as etapas cumpridas pelos registros dos domínios "
        + "(ver docs/adr/0152-programas-linhas-de-cuidado.md).")
public class LinhaCuidadoController {

    private static final String TAG = "Linhas de Cuidado";

    @Autowired
    private LinhaCuidadoService service;

    // ── Catálogo ──

    @LiberadoParaAutenticados
    @GetMapping(value = "linhas-de-cuidado", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Linhas de cuidado", description = "O catálogo, com as etapas ativas e quantos percursos estão em curso.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<LinhaCuidadoDto>> linhas() {
        return ResponseEntity.ok(service.linhas());
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "linha-de-cuidado/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Linha de cuidado", description = "A linha com as etapas e como cada uma se cumpre.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<LinhaCuidadoDto> linha(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.linha(uuid));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PostMapping(value = "linha-de-cuidado", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra uma linha de cuidado", description = "Com as etapas em ordem. Código repetido, 409; etapa sem a referência do "
            + "tipo (exame, procedimento, vacina) ou primeira etapa contada da anterior, 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LinhaCuidadoDto> criar(@Valid @RequestBody LinhaCuidadoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PutMapping(value = "linha-de-cuidado/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta uma linha de cuidado", description = "A etapa com uuid é ajustada, a nova é criada e a que não vier é desativada.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<LinhaCuidadoDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody LinhaCuidadoRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    // ── Percurso ──

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PostMapping(value = "percurso-linha-cuidado", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Abre o percurso do paciente na linha", description = "Linha inativa, 422; início no futuro ou equipe de outra unidade, 400; "
            + "acompanhamento de outro paciente ou programa, 422; já em curso, 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PercursoLinhaDto> abrir(@Valid @RequestBody PercursoLinhaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(dto));
    }

    @AuditarLeitura
    @RequerPermissao({"PRONTUARIO.CONSULTAR", "PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "percurso-linha-cuidado/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Percurso", description = "As etapas com a situação calculada (cumprida, no prazo, atrasada, aguardando a anterior, "
            + "dispensada) e o que cumpriu cada uma. Regras do prontuário; linha de programa sensível só para quem acompanha na unidade.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<PercursoLinhaDto> percurso(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.percurso(uuid));
    }

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PostMapping(value = "percurso-linha-cuidado/{uuid}/etapa", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra uma etapa", description = "A manual cumprida, ou qualquer uma dispensada com o motivo (400). Só a manual se cumpre "
            + "à mão (422); percurso encerrado, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PercursoLinhaDto> registrar(@PathVariable UUID uuid, @Valid @RequestBody RegistroEtapaPercursoRequestDto dto) {
        return ResponseEntity.ok(service.registrar(uuid, dto));
    }

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PostMapping(value = "percurso-linha-cuidado/{uuid}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra o percurso", description = "Concluído, com as obrigatórias cumpridas ou dispensadas (422); interrompido, com o "
            + "motivo (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<PercursoLinhaDto> encerrar(@PathVariable UUID uuid, @Valid @RequestBody EncerramentoPercursoRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"PRONTUARIO.CONSULTAR", "PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "paciente/{pacienteId}/linhas-de-cuidado", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Percursos do paciente", description = "Os percursos nas linhas de cuidado, em curso primeiro, com as etapas.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<PercursoLinhaDto>> doPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.doPaciente(pacienteId));
    }

    @AuditarLeitura
    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "linhas-de-cuidado/painel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Painel das linhas de cuidado", description = "Os percursos em curso do escopo, atrasados primeiro, e as sugestões de abrir "
            + "a linha para quem está no programa dela.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<PainelLinhasCuidadoDto> painel(@RequestParam(required = false) UUID linhaId, @RequestParam(required = false) UUID unidadeId,
                                                         @RequestParam(required = false) UUID equipeId,
                                                         @RequestParam(required = false, defaultValue = "false") boolean atrasados) {
        return ResponseEntity.ok(service.painel(linhaId, unidadeId, equipeId, atrasados));
    }
}
