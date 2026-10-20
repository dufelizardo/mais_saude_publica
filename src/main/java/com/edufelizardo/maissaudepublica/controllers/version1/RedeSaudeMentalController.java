package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AceiteTransferenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CompartilhamentoCuidadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncaminhamentoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TransferenciaCuidadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransferenciaCuidadoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransferenciasCuidadoDto;
import com.edufelizardo.maissaudepublica.services.version1.RedeSaudeMentalService;
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
@RequestMapping(value = "/api/v1/saude-mental/")
@Tag(name = "Saúde Mental · Rede", description = "Encaminhamento pela Regulação, cuidado compartilhado e transferência do cuidado "
        + "(ver docs/adr/0122-saude-mental-rede.md).")
public class RedeSaudeMentalController {

    @Autowired
    private RedeSaudeMentalService service;

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/encaminhamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encaminha pela Regulação", description = "Cria a solicitação (procedimento, CID, justificativa e prioridade; com "
            + "REGULACAO.SOLICITAR na unidade) ou vincula uma existente do paciente (solicitacaoId; de outro paciente, 422; já ligada, 409).",
            tags = "Saúde Mental · Rede")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> encaminhar(@PathVariable UUID uuid, @Valid @RequestBody EncaminhamentoSaudeMentalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.encaminhar(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/compartilhamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Compartilha o cuidado", description = "Com outra equipe, que passa a ter vínculo e o conteúdo do caso. A própria "
            + "referência: 422; já compartilhado com ela: 409.", tags = "Saúde Mental · Rede")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> compartilhar(@PathVariable UUID uuid, @Valid @RequestBody CompartilhamentoCuidadoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.compartilhar(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/compartilhamento/{compartilhamentoId}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra o compartilhamento", description = "Com motivo; a equipe parceira perde o acesso.", tags = "Saúde Mental · Rede")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> encerrarCompartilhamento(@PathVariable UUID uuid, @PathVariable UUID compartilhamentoId,
                                                                                @Valid @RequestBody MotivoSaudeMentalRequestDto dto) {
        return ResponseEntity.ok(service.encerrarCompartilhamento(uuid, compartilhamentoId, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/transferencia", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Propõe a transferência do cuidado", description = "Para outra unidade, com o resumo de contrarreferência. Uma "
            + "pendente por caso (409).", tags = "Saúde Mental · Rede")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> propor(@PathVariable UUID uuid, @Valid @RequestBody TransferenciaCuidadoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.propor(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "transferencia/{uuid}/aceite", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Aceita a transferência", description = "Pede SAUDE_MENTAL.ACOMPANHAR no destino. Encerra o caso na origem e abre o "
            + "caso no destino, ligado ao anterior, com a nova referência; o destino precisa oferecer o programa Saúde Mental (422).",
            tags = "Saúde Mental · Rede")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> aceitar(@PathVariable UUID uuid, @Valid @RequestBody AceiteTransferenciaRequestDto dto) {
        return ResponseEntity.ok(service.aceitar(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "transferencia/{uuid}/recusa", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Recusa a transferência", description = "Pelo destino, com motivo; o caso continua na origem.", tags = "Saúde Mental · Rede")
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransferenciaCuidadoDto> recusar(@PathVariable UUID uuid, @Valid @RequestBody MotivoSaudeMentalRequestDto dto) {
        return ResponseEntity.ok(service.recusar(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "transferencia/{uuid}/cancelamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancela a transferência", description = "Pela origem, enquanto pendente.", tags = "Saúde Mental · Rede")
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransferenciaCuidadoDto> cancelar(@PathVariable UUID uuid, @Valid @RequestBody MotivoSaudeMentalRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @GetMapping(value = "transferencias", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Transferências pendentes", description = "As que chegam às unidades do seu escopo e as que saem delas, com o resumo de "
            + "contrarreferência. Filtro: unidadeId. Leitura auditada.", tags = "Saúde Mental · Rede")
    @ApiErrorResponsesListagem
    public ResponseEntity<TransferenciasCuidadoDto> transferencias(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.pendentes(unidadeId));
    }
}
