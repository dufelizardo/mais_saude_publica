package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AplicarModeloRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CopiarSemanaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DesignarTurnoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TurnoEscalaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AplicacaoModeloResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CopiaSemanaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EscalaSemanaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TurnoEscalaDto;
import com.edufelizardo.maissaudepublica.services.version1.EscalaService;
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
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/escala/")
@Tag(name = "Escalas", description = "Turnos da unidade com data, vagas, designação e alertas de jornada (ver docs/adr/0105-escalas-backend.md).")
public class EscalaController {

    @Autowired
    private EscalaService service;

    @RequerPermissao({"ESCALA.GERENCIAR", "EQUIPE.GERENCIAR", "RH.CONSULTAR", "RH.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Escala da semana", description = "Da unidade, de segunda a domingo da semana de `semana` (sem data, a atual), opcionalmente "
            + "só de uma equipe: uma linha por profissional com turnos, horas, alertas e ausências do RH; vagas abertas; férias e "
            + "licenças dos próximos 30 dias; e os indicadores.", tags = "Escalas")
    @ApiErrorResponsesListagem
    public ResponseEntity<EscalaSemanaResponseDto> semana(@RequestParam UUID unidadeId,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate semana,
                                                          @RequestParam(required = false) UUID equipeId) {
        return ResponseEntity.ok(service.semana(unidadeId, semana, equipeId));
    }

    @RequerPermissao({"ESCALA.GERENCIAR"})
    @PostMapping(value = "turno", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um turno", description = "Sem profissional, é vaga aberta (precisa da função, 400). Fora do horário da unidade, plantão "
            + "em unidade que não é 24 horas, sem lotação, afastado no RH ou em dia passado: 422. Sobreposição com outro turno da "
            + "pessoa: 409. Jornada acima da contratada e descanso menor que 11h voltam como alertas.", tags = "Escalas")
    @ApiErrorResponsesMutacao
    public ResponseEntity<TurnoEscalaDto> criar(@Valid @RequestBody TurnoEscalaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @RequerPermissao({"ESCALA.GERENCIAR"})
    @PatchMapping(value = "turno/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um turno", description = "Tipo, data, horário, equipe, função e descrição, com as regras do cadastro. A unidade não muda e a "
            + "troca de profissional é pela designação (422). Turno já terminado: 422.", tags = "Escalas")
    @ApiErrorResponsesMutacao
    public ResponseEntity<TurnoEscalaDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody TurnoEscalaRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(uuid, dto));
    }

    @RequerPermissao({"ESCALA.GERENCIAR"})
    @PostMapping(value = "turno/{uuid}/designar", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Designa ou troca o profissional", description = "Preenche a vaga ou troca quem está no turno, com as regras do cadastro. "
            + "O mesmo profissional: 409.", tags = "Escalas")
    @ApiErrorResponsesMutacao
    public ResponseEntity<TurnoEscalaDto> designar(@PathVariable UUID uuid, @Valid @RequestBody DesignarTurnoRequestDto dto) {
        return ResponseEntity.ok(service.designar(uuid, dto));
    }

    @RequerPermissao({"ESCALA.GERENCIAR"})
    @DeleteMapping(value = "turno/{uuid}")
    @Operation(summary = "Remove um turno", description = "Só antes de começar (422).", tags = "Escalas")
    @ApiErrorResponsesMutacao
    public ResponseEntity<Void> remover(@PathVariable UUID uuid) {
        service.remover(uuid);
        return ResponseEntity.noContent().build();
    }

    @RequerPermissao({"ESCALA.GERENCIAR"})
    @PostMapping(value = "aplicar-modelo", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Aplica um modelo de jornada", description = "Gera a semana do profissional pelo modelo (40h com 8h + 1h de intervalo, 44h "
            + "6x1, 30h, 20h ou 12x36), com o início e o intervalo escolhidos. Os turnos que caem em dia passado ou quebram uma regra "
            + "ficam de fora e voltam com o motivo.", tags = "Escalas")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AplicacaoModeloResponseDto> aplicarModelo(@Valid @RequestBody AplicarModeloRequestDto dto) {
        return ResponseEntity.ok(service.aplicarModelo(dto));
    }

    @RequerPermissao({"ESCALA.GERENCIAR"})
    @PostMapping(value = "copiar-semana", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Copia a semana", description = "Os turnos da semana de origem vão para a de destino, no mesmo dia e horário. Os que caem em dia "
            + "passado ou quebram uma regra ficam de fora e voltam com o motivo.", tags = "Escalas")
    @ApiErrorResponsesMutacao
    public ResponseEntity<CopiaSemanaResponseDto> copiarSemana(@Valid @RequestBody CopiarSemanaRequestDto dto) {
        return ResponseEntity.ok(service.copiarSemana(dto));
    }
}
