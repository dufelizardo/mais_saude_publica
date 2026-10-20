package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcolhimentoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcompanhamentoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoAcompanhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReferenciaSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcolhimentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcolhimentosSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CasosSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcompanhamento;
import com.edufelizardo.maissaudepublica.services.version1.SaudeMentalService;
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
@Tag(name = "Saúde Mental", description = "Acolhimento, caso em saúde mental ligado ao programa, equipe e técnico de referência e avaliação, "
        + "com sigilo pela equipe de referência (ver docs/adr/0120-saude-mental-base.md).")
public class SaudeMentalController {

    @Autowired
    private SaudeMentalService service;

    @RequerPermissao({"SAUDE_MENTAL.ACOLHER"})
    @PostMapping(value = "acolhimento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra um acolhimento em saúde mental", description = "Acompanhamento na unidade e cuidado compartilhado abrem o "
            + "caso (com equipe e técnico de referência, 400 sem eles) ou entram no caso vigente; encaminhamento pede o destino (400). "
            + "Abrir o caso pede SAUDE_MENTAL.ACOMPANHAR na unidade.", tags = "Saúde Mental")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcolhimentoSaudeMentalDto> acolher(@Valid @RequestBody AcolhimentoSaudeMentalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.acolher(dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Abre o caso em saúde mental", description = "Reaproveita o acompanhamento do programa Saúde Mental na unidade ou "
            + "inscreve (a unidade precisa oferecer o programa, 422). O técnico precisa ser membro vigente da equipe (422). Um caso "
            + "vigente por paciente (409).", tags = "Saúde Mental")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> abrir(@Valid @RequestBody AcompanhamentoSaudeMentalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(dto));
    }

    @AuditarLeitura
    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @GetMapping(value = "acompanhamentos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Casos em saúde mental", description = "Casos vigentes nas unidades do seu escopo, sem conteúdo clínico; faltosos e "
            + "sem avaliação primeiro. Filtros: unidadeId, pacienteId, escopo (TECNICO ou EQUIPE), situacao e pendencia (SEM_AVALIACAO, "
            + "SEM_PTS, PTS_A_REVISAR). Leitura auditada.",
            tags = "Saúde Mental")
    @ApiErrorResponsesListagem
    public ResponseEntity<CasosSaudeMentalDto> casos(@RequestParam(required = false) UUID unidadeId,
                                                     @RequestParam(required = false) UUID pacienteId,
                                                     @RequestParam(required = false) String escopo,
                                                     @RequestParam(required = false) SituacaoAcompanhamento situacao,
                                                     @RequestParam(required = false) String pendencia) {
        return ResponseEntity.ok(service.casos(unidadeId, pacienteId, escopo, situacao, pendencia));
    }

    @AuditarLeitura
    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR", "SAUDE_MENTAL.ACOLHER", "PRONTUARIO.CONSULTAR"})
    @GetMapping(value = "acompanhamento/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "O caso em saúde mental", description = "Pede vínculo assistencial (403 VINCULO_ASSISTENCIAL_AUSENTE); a equipe de "
            + "referência conta como vínculo. Acolhimentos e avaliações só com conteudoLiberado: equipe de referência, quem registrou no "
            + "caso, acesso justificado. Leitura auditada.", tags = "Saúde Mental")
    @ApiErrorResponsesBusca
    public ResponseEntity<AcompanhamentoSaudeMentalDto> caso(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.caso(uuid));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/referencia", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Troca a equipe ou o técnico de referência", description = "Fecha a referência vigente e abre a nova, com o motivo. "
            + "A mesma referência: 422. Só a equipe de referência ou o acesso justificado (403).", tags = "Saúde Mental")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> referencia(@PathVariable UUID uuid, @Valid @RequestBody ReferenciaSaudeMentalRequestDto dto) {
        return ResponseEntity.ok(service.trocarReferencia(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/avaliacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra uma avaliação", description = "Contexto e necessidades obrigatórios; CID-10 opcional (400 se inválido). Caso "
            + "encerrado: 422.", tags = "Saúde Mental")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> avaliar(@PathVariable UUID uuid, @Valid @RequestBody AvaliacaoSaudeMentalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.avaliar(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra o caso", description = "Fecha a referência e registra a saída do programa Saúde Mental com o mesmo motivo. "
            + "Alta conclui; os outros motivos encerram.", tags = "Saúde Mental")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> encerrar(@PathVariable UUID uuid, @Valid @RequestBody EncerramentoAcompanhamentoRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR", "SAUDE_MENTAL.ACOLHER", "PRONTUARIO.CONSULTAR"})
    @GetMapping(value = "paciente/{pacienteId}/acolhimentos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Acolhimentos do paciente", description = "Com vínculo assistencial, como no prontuário; o conteúdo só onde estiver "
            + "liberado. Leitura auditada.", tags = "Saúde Mental")
    @ApiErrorResponsesBusca
    public ResponseEntity<AcolhimentosSaudeMentalDto> acolhimentos(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.acolhimentosDoPaciente(pacienteId));
    }
}
