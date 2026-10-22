package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoSaudeBucalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacoesBucaisColetivasRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcaoSaudeBucalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ConvocacaoSaudeBucalDto;
import com.edufelizardo.maissaudepublica.services.version1.SaudeBucalColetivaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Saúde bucal coletiva", description = "O detalhe odontológico das ações coletivas dos programas Saúde Bucal e Saúde na Escola, "
        + "a avaliação coletiva por participante e a lista a convocar (ver docs/adr/0140-saude-bucal-coletiva.md).")
public class SaudeBucalColetivaController {

    private static final String TAG = "Saúde bucal coletiva";

    @Autowired
    private SaudeBucalColetivaService service;

    @RequerPermissao({"PROGRAMA.ACOMPANHAR", "ODONTOLOGIA.REGISTRAR"})
    @GetMapping(value = "acao-programatica/{uuid}/saude-bucal", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Detalhe de saúde bucal da ação", description = "Atividades, local, público e kits, e as avaliações dos participantes.",
            tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<AcaoSaudeBucalDto> detalhe(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.detalhe(uuid));
    }

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PutMapping(value = "acao-programatica/{uuid}/saude-bucal", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Preenche o detalhe de saúde bucal", description = "Só na ação coletiva dos programas Saúde Bucal e Saúde na Escola "
            + "(422), prevista ou realizada (422 na não realizada e na cancelada).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcaoSaudeBucalDto> salvar(@PathVariable UUID uuid, @Valid @RequestBody AcaoSaudeBucalRequestDto dto) {
        return ResponseEntity.ok(service.salvarDetalhe(uuid, dto));
    }

    @RequerPermissao({"ODONTOLOGIA.REGISTRAR"})
    @PostMapping(value = "acao-programatica/{uuid}/avaliacao-bucal", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Avaliação coletiva por participante", description = "Do cirurgião-dentista, na ação realizada (422), com a lista "
            + "nominal (422) e só para os participantes (422). Corrigir substitui o resultado do participante na mesma ação.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcaoSaudeBucalDto> avaliar(@PathVariable UUID uuid, @Valid @RequestBody AvaliacoesBucaisColetivasRequestDto dto) {
        return ResponseEntity.ok(service.avaliar(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"PROGRAMA.ACOMPANHAR", "ODONTOLOGIA.REGISTRAR"})
    @GetMapping(value = "saude-bucal/a-convocar", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "A convocar", description = "Quem a avaliação coletiva apontou com necessidade de tratamento e ainda não tem plano "
            + "aberto, na unidade. A urgência primeiro.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<ConvocacaoSaudeBucalDto>> aConvocar(@RequestParam UUID unidadeId) {
        return ResponseEntity.ok(service.aConvocar(unidadeId));
    }
}
