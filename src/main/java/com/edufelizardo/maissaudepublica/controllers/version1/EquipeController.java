package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EquipeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MembroEquipeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SaidaMembroEquipeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EquipeResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EquipesResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.EquipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/equipe/")
@Tag(name = "Equipes", description = "Equipes de saúde da unidade, membros como histórico e composição mínima (ver docs/adr/0103-equipes-backend.md).")
public class EquipeController {

    @Autowired
    private EquipeService service;

    @RequerPermissao({"EQUIPE.GERENCIAR", "RH.CONSULTAR", "RH.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as equipes", description = "Do escopo (ou de unidadeId), com membros vigentes, composição calculada e os "
            + "indicadores: equipes, ativas, por tipo, profissionais vinculados e incompletas.", tags = "Equipes")
    @ApiErrorResponsesListagem
    public ResponseEntity<EquipesResponseDto> listar(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.listar(unidadeId));
    }

    @RequerPermissao({"EQUIPE.GERENCIAR", "RH.CONSULTAR", "RH.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Busca uma equipe", description = "Membros vigentes (com cargo, jornada e afastamento do RH), antigos, coordenação, reunião "
            + "e apoiadas. Fora do escopo: 403.", tags = "Equipes")
    @ApiErrorResponsesBusca
    public ResponseEntity<EquipeResponseDto> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorId(uuid));
    }

    @RequerPermissao({"EQUIPE.GERENCIAR"})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra uma equipe", description = "Nome ou INE repetido: 409. Só a eMulti apoia equipes, e só eSF e eAP (422).", tags = "Equipes")
    @ApiErrorResponsesMutacao
    public ResponseEntity<EquipeResponseDto> criar(@Valid @RequestBody EquipeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"EQUIPE.GERENCIAR"})
    @PatchMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita a equipe", description = "O tipo não muda (422); quem coordena precisa ser membro vigente (422).", tags = "Equipes")
    @ApiErrorResponsesMutacao
    public ResponseEntity<EquipeResponseDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody EquipeRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"EQUIPE.GERENCIAR"})
    @PostMapping(value = "{uuid}/membro", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui um membro", description = "Precisa de lotação vigente na unidade (422). Já membro, ou já em outra eSF/eAP: 409. "
            + "Equipe inativa: 422.", tags = "Equipes")
    @ApiErrorResponsesMutacao
    public ResponseEntity<EquipeResponseDto> adicionarMembro(@PathVariable UUID uuid, @Valid @RequestBody MembroEquipeRequestDto dto) {
        return ResponseEntity.ok(service.adicionarMembro(uuid, dto));
    }

    @RequerPermissao({"EQUIPE.GERENCIAR"})
    @PostMapping(value = "membro/{uuid}/saida", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a saída de um membro", description = "Com motivo; a data fica entre a entrada e hoje (400). Já encerrada: 422.", tags = "Equipes")
    @ApiErrorResponsesMutacao
    public ResponseEntity<EquipeResponseDto> saida(@PathVariable UUID uuid, @Valid @RequestBody SaidaMembroEquipeRequestDto dto) {
        return ResponseEntity.ok(service.registrarSaida(uuid, dto));
    }
}
