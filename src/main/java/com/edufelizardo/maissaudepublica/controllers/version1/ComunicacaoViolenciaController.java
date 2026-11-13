package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ComunicacaoViolenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComunicacaoViolenciaDtos;
import com.edufelizardo.maissaudepublica.services.version1.ComunicacaoViolenciaService;
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
@RequestMapping(value = "/api/v1/vigilancia/")
@Tag(name = "Vigilância epidemiológica", description = "Comunicação obrigatória da violência notificada à rede intersetorial (ver a ADR-0201).")
public class ComunicacaoViolenciaController {

    private static final String TAG = "Vigilância epidemiológica";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private ComunicacaoViolenciaService service;

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @AuditarLeitura
    @GetMapping(value = "notificacao/{uuid}/comunicacao", produces = JSON)
    @Operation(summary = "Comunicação obrigatória da notificação", description = "Se a violência notificada exige comunicação, a quem, até quando e se foi "
            + "feita; o acesso é o do detalhe da notificação.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<ComunicacaoViolenciaDtos.Comunicacao> comunicacao(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.comunicacao(uuid));
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "notificacao/{uuid}/comunicacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a comunicação", description = "Encaminhamento intersetorial a uma instituição de tipo aceito (422); sem obrigação, 422.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComunicacaoViolenciaDtos.Comunicacao> comunicar(@PathVariable UUID uuid, @Valid @RequestBody ComunicacaoViolenciaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.comunicar(uuid, dto));
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "notificacao/{uuid}/comunicacao/{encaminhamento}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a comunicação registrada por engano", description = "Só em aberto (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<ComunicacaoViolenciaDtos.Comunicacao> cancelar(@PathVariable UUID uuid, @PathVariable UUID encaminhamento,
                                                                        @Valid @RequestBody MotivoRequestDto dto) {
        return ResponseEntity.ok(service.cancelar(uuid, encaminhamento, dto.getMotivo()));
    }

    @RequerPermissao({"VIGILANCIA.INVESTIGAR"})
    @AuditarLeitura
    @GetMapping(value = "comunicacoes-pendentes", produces = JSON)
    @Operation(summary = "Comunicações obrigatórias pendentes", description = "As violências notificadas sem a comunicação, vencidas primeiro; só para quem "
            + "investiga, nas unidades do acesso.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<ComunicacaoViolenciaDtos.Pendente>> pendentes() {
        return ResponseEntity.ok(service.pendentes());
    }
}
