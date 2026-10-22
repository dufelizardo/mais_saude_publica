package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ParticipacaoProgramaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProgramaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProgramaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProgramaResumoDto;
import com.edufelizardo.maissaudepublica.services.version1.ProgramaService;
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
@RequestMapping(value = "/api/v1/programa/")
@Tag(name = "Programas de Saúde")
public class ProgramaController {

    @Autowired
    private ProgramaService service;

    @LiberadoParaAutenticados
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os programas", description = "Por categoria e nome, com o público-alvo e quantas unidades oferecem cada um. Filtros: "
            + "categoriaId, unidadeId e equipeId (que também restringem a contagem de inscritos). Aberto a quem está logado: o catálogo não tem dado de paciente.", tags = "Programas de Saúde")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ProgramaResumoDto>> listar(@RequestParam(required = false) UUID categoriaId,
                                                          @RequestParam(required = false) UUID unidadeId,
                                                          @RequestParam(required = false) UUID equipeId) {
        return ResponseEntity.ok(service.listar(categoriaId, unidadeId, equipeId));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca um programa", description = "Com as unidades e equipes participantes, vigentes e encerradas.", tags = "Programas de Saúde")
    @ApiErrorResponsesBusca
    public ResponseEntity<ProgramaDto> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscar(uuid));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um programa", description = "Código repetido: 409. Encerrado sem fim, fim antes do início, idade máxima menor "
            + "que a mínima ou condição fora do formato CID/CIAP: 400. Categoria inativa ou coordenação inativa: 422.", tags = "Programas de Saúde")
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProgramaDto> criar(@Valid @RequestBody ProgramaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um programa", description = "Substitui os campos editáveis e o público-alvo. As mesmas regras do cadastro.",
            tags = "Programas de Saúde")
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProgramaDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody ProgramaRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PostMapping(value = "{uuid}/participacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui uma unidade (e equipe) que oferece o programa", description = "Só unidade de atendimento e equipe dela (422); a "
            + "mesma unidade e equipe vigente: 409; programa encerrado: 422; início no futuro: 400.", tags = "Programas de Saúde")
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProgramaDto> incluirParticipacao(@PathVariable UUID uuid, @Valid @RequestBody ParticipacaoProgramaRequestDto dto) {
        return ResponseEntity.ok(service.incluirParticipacao(uuid, dto));
    }

    @RequerPermissao({"PROGRAMA.GERENCIAR"})
    @PostMapping(value = "{uuid}/participacao/{participacaoId}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra uma participação", description = "Com motivo; o fim fica entre o início e hoje (400). Já encerrada: 422.",
            tags = "Programas de Saúde")
    @ApiErrorResponsesMutacao
    public ResponseEntity<ProgramaDto> encerrarParticipacao(@PathVariable UUID uuid, @PathVariable UUID participacaoId,
                                                            @Valid @RequestBody EncerramentoRequestDto dto) {
        return ResponseEntity.ok(service.encerrarParticipacao(uuid, participacaoId, dto));
    }
}
