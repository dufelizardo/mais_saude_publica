package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.HorariosUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeCadastroRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FichaUnidadeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RedeUnidadesResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.RedeUnidadesService;
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
@RequestMapping(value = "/api/v1/unidade-saude/")
@Tag(name = "Equipamentos de Saúde", description = "Rede de unidades por id: lista com resumo, ficha, cadastro, horário estruturado e situação operacional (ver docs/adr/0101-equipamentos-de-saude-backend.md).")
public class RedeUnidadesController {

    @Autowired
    private RedeUnidadesService service;

    @RequerPermissao({"ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(value = "rede", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rede de unidades", description = "Unidades do escopo com tipo, CNES, situação, endereço, profissionais lotados, setores, "
            + "leitos e ocupação, e os indicadores (só unidades de atendimento, sem os níveis de gestão).", tags = "Equipamentos de Saúde")
    @ApiErrorResponsesListagem
    public ResponseEntity<RedeUnidadesResponseDto> rede() {
        return ResponseEntity.ok(service.rede());
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(value = "id/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ficha da unidade", description = "Dados, horário estruturado, unidades abaixo, setores, profissionais lotados e histórico de "
            + "situação. Fora do escopo: 403.", tags = "Equipamentos de Saúde")
    @ApiErrorResponsesBusca
    public ResponseEntity<FichaUnidadeDto> ficha(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.ficha(uuid));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra uma unidade", description = "Unidade superior do nível esperado (senão 422); nome ou CNES repetido: 409. "
            + "Hospital e UPA nascem funcionando 24 horas.", tags = "Equipamentos de Saúde")
    @ApiErrorResponsesMutacao
    public ResponseEntity<FichaUnidadeDto> criar(@Valid @RequestBody UnidadeCadastroRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PatchMapping(value = "id/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita a unidade", description = "Nome, CNES, endereço, contato, responsável e supervisão regional. O tipo não muda (422).", tags = "Equipamentos de Saúde")
    @ApiErrorResponsesMutacao
    public ResponseEntity<FichaUnidadeDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody UnidadeCadastroRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PutMapping(value = "id/{uuid}/horarios", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Define o horário estruturado", description = "Substitui todos os turnos: fecham depois de abrir, sem sobrepor, até 3 por dia "
            + "(senão 400). Com 24 horas, o horário não fecha a agenda.", tags = "Equipamentos de Saúde")
    @ApiErrorResponsesMutacao
    public ResponseEntity<FichaUnidadeDto> horarios(@PathVariable UUID uuid, @Valid @RequestBody HorariosUnidadeRequestDto dto) {
        return ResponseEntity.ok(service.horarios(uuid, dto));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PostMapping(value = "id/{uuid}/situacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Muda a situação operacional", description = "Fora de operação exige motivo (400); previsão no passado: 400; mesma situação: 422. "
            + "Em obra ou inoperante, a agenda não oferece vaga nem aceita marcação.", tags = "Equipamentos de Saúde")
    @ApiErrorResponsesMutacao
    public ResponseEntity<FichaUnidadeDto> situacao(@PathVariable UUID uuid, @Valid @RequestBody SituacaoUnidadeRequestDto dto) {
        return ResponseEntity.ok(service.situacao(uuid, dto));
    }
}
