package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ServicoUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ServicosDaUnidadeDto;
import com.edufelizardo.maissaudepublica.services.version1.ServicoUnidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/unidade-saude/")
@Tag(name = "Modelo Operacional")
public class ServicoUnidadeController {

    @Autowired
    private ServicoUnidadeService service;

    @RequerPermissao({"ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(value = "id/{uuid}/servicos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Serviços da unidade", description = "Vigentes, encerrados e os setores livres; os leitos vêm dos setores.", tags = "Modelo Operacional")
    @ApiErrorResponsesBusca
    public ResponseEntity<ServicosDaUnidadeDto> listar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.daUnidade(uuid));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/servico", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui um serviço na unidade", description = "Sem a capacidade exigida pelo tipo: 422. Nome repetido entre os vigentes ou setor "
            + "em outro serviço: 409.", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<ServicosDaUnidadeDto> incluir(@PathVariable UUID uuid, @Valid @RequestBody ServicoUnidadeRequestDto dto) {
        return ResponseEntity.ok(service.incluir(uuid, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PatchMapping(value = "id/{uuid}/servico/{servicoId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta o serviço", description = "Nome, responsável, horário e setores; encerrado não muda (422).", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<ServicosDaUnidadeDto> atualizar(@PathVariable UUID uuid, @PathVariable UUID servicoId, @Valid @RequestBody ServicoUnidadeRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, servicoId, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/servico/{servicoId}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra o serviço", description = "Com motivo; o fim fica entre o início e hoje (400). Já encerrado: 422.", tags = "Modelo Operacional")
    @ApiErrorResponsesMutacao
    public ResponseEntity<ServicosDaUnidadeDto> encerrar(@PathVariable UUID uuid, @PathVariable UUID servicoId, @Valid @RequestBody EncerramentoRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(uuid, servicoId, dto));
    }
}
