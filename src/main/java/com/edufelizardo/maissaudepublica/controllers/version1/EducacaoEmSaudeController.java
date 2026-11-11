package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoEducativaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcaoEducativaDto;
import com.edufelizardo.maissaudepublica.services.version1.EducacaoEmSaudeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Educação em saúde", description = "O detalhe educativo da ação coletiva de Programas: tema, conteúdos, materiais, microáreas, campanha e "
        + "avaliação (ver docs/adr/0195-educacao-em-saude.md).")
public class EducacaoEmSaudeController {

    private static final String TAG = "Educação em saúde";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private EducacaoEmSaudeService service;

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "acao-programatica/{uuid}/educacao", produces = JSON)
    @Operation(summary = "Educação em saúde da ação", description = "Vazio (sem tema) enquanto não preenchido.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<AcaoEducativaDto> detalhe(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.detalhe(uuid));
    }

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PutMapping(value = "acao-programatica/{uuid}/educacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Preenche a educação em saúde", description = "Ação coletiva de grupo, orientação, mobilização ou outra (422); conteúdo só "
            + "revisado (422); material retirado (422) ou repetido (400); campanha planejada ou em andamento (422). Substitui as ligações.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcaoEducativaDto> salvar(@PathVariable UUID uuid, @Valid @RequestBody AcaoEducativaRequestDto dto) {
        return ResponseEntity.ok(service.salvar(uuid, dto));
    }

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PostMapping(value = "acao-programatica/{uuid}/educacao/avaliacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Avalia a ação educativa", description = "Na ação realizada (422) com a educação preenchida (422): a participação e o texto.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcaoEducativaDto> avaliar(@PathVariable UUID uuid, @Valid @RequestBody AcaoEducativaRequestDto.Avaliacao dto) {
        return ResponseEntity.ok(service.avaliar(uuid, dto));
    }
}
