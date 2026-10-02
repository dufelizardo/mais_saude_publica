package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ExampleConstants;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.BlocoAgendaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.BloqueioAgendaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoBlocoAgendaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AgendaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.BlocoAgendaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.BloqueioAgendaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ItemAgendaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SuccessResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping(value = "/api/v1/agenda/")
@Tag(name = "Agenda", description = "Agenda do profissional por unidade: blocos recorrentes, bloqueios, férias e afastamentos do RH, vagas livres e a agenda dia a dia (ver docs/adr/0091-agenda-do-profissional.md).")
public class AgendaController {

    @Autowired
    private AgendaService service;

    @RequerPermissao({"AGENDAMENTO.GERENCIAR", "ATENDIMENTO.GERENCIAR", "REGULACAO.REGULAR"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "A agenda de um profissional numa unidade, dia a dia",
            description = "Vagas, marcações, encaixes, bloqueios e afastamentos de cada dia de [de, ate], com o resumo do período "
                    + "(vagas ofertadas e ocupadas, ocupação, marcações, encaixes, faltas). Até 62 dias por vez.",
            tags = "Agenda")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = AgendaResponseDto.class))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<AgendaResponseDto> agenda(@RequestParam String profissionalMatricula, @RequestParam UUID unidadeId,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(service.agenda(profissionalMatricula, unidadeId, de, ate));
    }

    @RequerPermissao({"AGENDAMENTO.GERENCIAR", "ATENDIMENTO.GERENCIAR", "REGULACAO.REGULAR"})
    @GetMapping(value = "vagas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Vagas livres de um profissional numa unidade",
            description = "A partir de agora, sem bloqueio nem afastamento e sem marcação. Para a nova marcação e para a regulação.",
            tags = "Agenda")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ItemAgendaDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ItemAgendaDto>> vagas(@RequestParam String profissionalMatricula, @RequestParam UUID unidadeId,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(service.vagas(profissionalMatricula, unidadeId, de, ate));
    }

    // ── Blocos ──

    @RequerPermissao({"AGENDAMENTO.GERENCIAR"})
    @PostMapping(value = "bloco", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria um bloco recorrente na agenda",
            description = "Dia da semana, início, fim, duração da vaga e tipo, por profissional e unidade. Fim antes do início ou bloco menor "
                    + "que uma vaga: 400. Bloco do mesmo profissional, em qualquer unidade, cruzando horário e vigência: 409.",
            tags = "Agenda")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> criarBloco(@Valid @RequestBody BlocoAgendaRequestDto dto) {
        BlocoAgendaResponseDto b = service.criarBloco(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Bloco de agenda criado com sucesso!",
                "Id: " + b.getUuid() + ", " + b.getDiaSemana() + " " + b.getHoraInicio() + "-" + b.getHoraFim() + ", Vagas por dia: " + b.getVagasPorDia()));
    }

    @RequerPermissao({"AGENDAMENTO.GERENCIAR"})
    @PostMapping(value = "bloco/{uuid}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra um bloco da agenda",
            description = "O bloco vale até vigenteAte, inclusive; as marcações já feitas ficam. Mudar um bloco é encerrá-lo e criar outro.",
            tags = "Agenda")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> encerrarBloco(@PathVariable UUID uuid, @Valid @RequestBody EncerramentoBlocoAgendaRequestDto dto) {
        BlocoAgendaResponseDto b = service.encerrarBloco(uuid, dto.getVigenteAte());
        return ResponseEntity.ok(new SuccessResponseDto("Bloco de agenda encerrado!", "Id: " + b.getUuid() + ", Vale até: " + b.getVigenteAte()));
    }

    @RequerPermissao({"AGENDAMENTO.GERENCIAR", "ATENDIMENTO.GERENCIAR", "REGULACAO.REGULAR"})
    @GetMapping(value = "bloco", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista os blocos de um profissional, de uma unidade, ou dos dois", tags = "Agenda")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BlocoAgendaResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<BlocoAgendaResponseDto>> listarBlocos(@RequestParam(required = false) String profissionalMatricula,
                                                                     @RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.listarBlocos(profissionalMatricula, unidadeId));
    }

    // ── Bloqueios ──

    @RequerPermissao({"AGENDAMENTO.GERENCIAR"})
    @PostMapping(value = "bloqueio", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Bloqueia a agenda num período",
            description = "De um profissional (numa unidade ou em todas) ou da unidade inteira. Sem profissional nem unidade, ou fim antes "
                    + "do início: 400. Férias e afastamentos não entram aqui: vêm do RH.",
            tags = "Agenda")
    @ApiResponse(responseCode = "201", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> criarBloqueio(@Valid @RequestBody BloqueioAgendaRequestDto dto) {
        BloqueioAgendaResponseDto b = service.criarBloqueio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponseDto("Bloqueio de agenda registrado!",
                "Id: " + b.getUuid() + ", " + b.getMotivo() + ", De: " + b.getInicio() + ", Até: " + b.getFim()));
    }

    @RequerPermissao({"AGENDAMENTO.GERENCIAR", "ATENDIMENTO.GERENCIAR", "REGULACAO.REGULAR"})
    @GetMapping(value = "bloqueio", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Bloqueios que tocam um período, para um profissional numa unidade",
            description = "Os do profissional (na unidade ou em todas) e os da unidade inteira. Até 62 dias por vez.", tags = "Agenda")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BloqueioAgendaResponseDto.class)))
    })
    @ApiErrorResponsesListagem
    public ResponseEntity<List<BloqueioAgendaResponseDto>> listarBloqueios(@RequestParam String profissionalMatricula, @RequestParam UUID unidadeId,
                                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(service.listarBloqueios(profissionalMatricula, unidadeId, de, ate));
    }

    @RequerPermissao({"AGENDAMENTO.GERENCIAR"})
    @DeleteMapping(value = "bloqueio/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Remove um bloqueio da agenda", description = "As vagas do período voltam a ser oferecidas.", tags = "Agenda")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SuccessResponseDto.class),
                    examples = @ExampleObject(name = "Success", summary = "SuccessResponse",
                            value = ExampleConstants.SUCCESS_RESPONSE_EXAMPLE))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<SuccessResponseDto> removerBloqueio(@PathVariable UUID uuid) {
        service.removerBloqueio(uuid);
        return ResponseEntity.ok(new SuccessResponseDto("Bloqueio de agenda removido!", "Id: " + uuid));
    }
}
