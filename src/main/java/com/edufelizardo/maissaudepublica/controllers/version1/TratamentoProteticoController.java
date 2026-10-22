package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EtapaProteticaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TratamentoProteticoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TratamentoProteticoDto;
import com.edufelizardo.maissaudepublica.services.version1.TratamentoProteticoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Tratamento protético", description = "O ciclo da prótese dentária em etapas, com o laboratório de prótese "
        + "(ver docs/adr/0139-saude-bucal-tratamento-protetico.md).")
public class TratamentoProteticoController {

    private static final String TAG = "Tratamento protético";

    @Autowired
    private TratamentoProteticoService service;

    @RequerPermissao({"ODONTOLOGIA.PLANEJAR"})
    @PostMapping(value = "tratamento-protetico", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Abre o tratamento protético", description = "No atendimento de odontologia (422). Total e parcial removível pela "
            + "arcada (400), unitária pelo dente FDI (400). Laboratório: unidade com a capacidade LRPD (422) ou contratado, não os dois (400).",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TratamentoProteticoDto> abrir(@Valid @RequestBody TratamentoProteticoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(dto));
    }

    @RequerPermissao({"ODONTOLOGIA.PLANEJAR"})
    @PostMapping(value = "tratamento-protetico/{uuid}/etapa", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra uma etapa", description = "Na ordem do ciclo (422): prova e laboratório depois da moldagem; o laboratório "
            + "pede o laboratório definido; a entrega pede o laboratório; depois da entrega, só acompanhamento. Data no futuro: 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TratamentoProteticoDto> etapa(@PathVariable UUID uuid, @Valid @RequestBody EtapaProteticaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarEtapa(uuid, dto));
    }

    @RequerPermissao({"ODONTOLOGIA.PLANEJAR"})
    @PostMapping(value = "tratamento-protetico/{uuid}/conclusao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Conclui o tratamento protético", description = "Depois da entrega (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TratamentoProteticoDto> concluir(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.concluir(uuid));
    }

    @RequerPermissao({"ODONTOLOGIA.PLANEJAR"})
    @PostMapping(value = "tratamento-protetico/{uuid}/cancelamento", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancela o tratamento protético", description = "Corpo: {\"motivo\": \"...\"} (400 sem motivo).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TratamentoProteticoDto> cancelar(@PathVariable UUID uuid, @RequestBody Map<String, String> corpo) {
        return ResponseEntity.ok(service.cancelar(uuid, corpo.get("motivo")));
    }

    @AuditarLeitura
    @RequerPermissao({"PRONTUARIO.CONSULTAR", "ODONTOLOGIA.REGISTRAR", "ODONTOLOGIA.PLANEJAR"})
    @GetMapping(value = "paciente/{pacienteId}/tratamentos-proteticos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Tratamentos protéticos do paciente", description = "O mais recente primeiro, com as etapas. Regras do prontuário: "
            + "vínculo e leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<TratamentoProteticoDto>> doPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.doPaciente(pacienteId));
    }
}
