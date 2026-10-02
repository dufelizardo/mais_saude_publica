package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
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
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AltaInternacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InternacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TrocaLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.InternacaoResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.services.version1.InternacaoService;

@RestController
@RequestMapping(value = "/api/v1/internacao/")
@Tag(name = "Internação", description = "Admissão, troca de leito e alta (ver docs/adr/0098-leitos-e-internacao-backend.md).")
public class InternacaoController {

    @Autowired
    private InternacaoService service;

    @RequerPermissao({"INTERNACAO.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Interna um paciente", description = "Em leito livre e em uso, compatível com o sexo do paciente (senão 422). "
            + "Paciente já internado: 409. Atendimento de outro paciente ou CID inválido: 400.", tags = "Internação")
    @ApiErrorResponsesMutacao
    public ResponseEntity<InternacaoResponseDto> internar(@Valid @RequestBody InternacaoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.internar(dto));
    }

    @RequerPermissao({"INTERNACAO.GERENCIAR"})
    @PostMapping(value = "{uuid}/troca-de-leito", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Troca o paciente de leito", description = "Na mesma unidade, para leito livre e compatível (senão 422). O leito "
            + "anterior vai para higienização.", tags = "Internação")
    @ApiErrorResponsesMutacao
    public ResponseEntity<InternacaoResponseDto> trocarLeito(@PathVariable UUID uuid, @Valid @RequestBody TrocaLeitoRequestDto dto) {
        return ResponseEntity.ok(service.trocarLeito(uuid, dto));
    }

    @RequerPermissao({"INTERNACAO.ALTA"})
    @PostMapping(value = "{uuid}/alta", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Dá alta", description = "Ato médico: tipo, sumário (10 a 4000 caracteres) e data, que não pode ser antes da admissão "
            + "nem no futuro (400). Internação encerrada: 422. O leito vai para higienização.", tags = "Internação")
    @ApiErrorResponsesMutacao
    public ResponseEntity<InternacaoResponseDto> darAlta(@PathVariable UUID uuid, @Valid @RequestBody AltaInternacaoRequestDto dto) {
        return ResponseEntity.ok(service.darAlta(uuid, dto));
    }

    @RequerPermissao({"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "LEITO.GERENCIAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista internações", description = "Das unidades do escopo, da mais recente à mais antiga, com filtros de unidade, "
            + "situação e paciente. Sem motivo, sumário e movimentos.", tags = "Internação")
    @ApiErrorResponsesListagem
    public ResponseEntity<List<InternacaoResponseDto>> listar(@RequestParam(required = false) UUID unidadeId,
                                                              @RequestParam(required = false) StatusInternacao status,
                                                              @RequestParam(required = false) UUID pacienteId) {
        return ResponseEntity.ok(service.listar(unidadeId, status, pacienteId));
    }

    @RequerPermissao({"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "LEITO.GERENCIAR"})
    @AuditarLeitura
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma internação", description = "Com motivo, sumário de alta e movimentos; leitura auditada. Fora do escopo: 403.", tags = "Internação")
    @ApiErrorResponsesBusca
    public ResponseEntity<InternacaoResponseDto> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }
}
