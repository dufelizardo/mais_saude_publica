package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcompanhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoAcompanhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoAcompanhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AdscricaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentosDoPacienteDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.InscritosProgramaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ResumoAcompanhamentosDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcompanhamento;
import com.edufelizardo.maissaudepublica.services.version1.AcompanhamentoService;
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
@RequestMapping(value = "/api/v1/")
@Tag(name = "Acompanhamento Programático", description = "Pacientes acompanhados nos programas de saúde: inscrição, faltoso, saída e leitura com as regras do prontuário (ver docs/adr/0119-programas-acompanhamento.md).")
public class AcompanhamentoController {

    @Autowired
    private AcompanhamentoService service;

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PostMapping(value = "programa/{uuid}/acompanhamento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inscreve um paciente no programa", description = "A unidade responsável precisa oferecer o programa (422); um "
            + "acompanhamento vigente por paciente e programa (409); programa não ativo ou paciente inativo: 422. Fora do público-alvo só "
            + "avisa (avisoPublico).", tags = "Acompanhamento Programático")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoDto> inscrever(@PathVariable UUID uuid, @Valid @RequestBody AcompanhamentoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.inscrever(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "programa/{uuid}/acompanhamentos", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inscritos do programa", description = "Acompanhamentos vigentes nas unidades do seu escopo, com faltosos e atrasados "
            + "primeiro. Filtros: unidadeId e situacao. Leitura auditada.", tags = "Acompanhamento Programático")
    @ApiErrorResponsesListagem
    public ResponseEntity<InscritosProgramaDto> inscritos(@PathVariable UUID uuid, @RequestParam(required = false) UUID unidadeId,
                                                          @RequestParam(required = false) SituacaoAcompanhamento situacao) {
        return ResponseEntity.ok(service.inscritos(uuid, unidadeId, situacao));
    }

    @AuditarLeitura
    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "programa/{uuid}/elegiveis", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Elegíveis do programa", description = "Os adscritos da equipe (ou da microárea) no público do programa e ainda não "
            + "inscritos (ADR-0128). Avalia idade e sexo; gestação e condição vêm como não avaliadas. Leitura auditada.", tags = "Acompanhamento Programático")
    @ApiErrorResponsesListagem
    public ResponseEntity<AdscricaoDto.Elegiveis> elegiveis(
            @PathVariable UUID uuid, @RequestParam UUID equipeId, @RequestParam(required = false) UUID microareaId) {
        return ResponseEntity.ok(service.elegiveis(uuid, equipeId, microareaId));
    }

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/situacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Marca faltoso ou volta a ativo", description = "Com motivo. A mesma situação: 422; encerrado: 422.", tags = "Acompanhamento Programático")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoDto> situacao(@PathVariable UUID uuid, @Valid @RequestBody SituacaoAcompanhamentoRequestDto dto) {
        return ResponseEntity.ok(service.mudarSituacao(uuid, dto));
    }

    @RequerPermissao({"PROGRAMA.ACOMPANHAR"})
    @PostMapping(value = "acompanhamento/{uuid}/encerramento", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a saída do programa", description = "Alta conclui; os outros motivos encerram. A data fica entre o início e "
            + "hoje (400). Já encerrado: 422.", tags = "Acompanhamento Programático")
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoDto> encerrar(@PathVariable UUID uuid, @Valid @RequestBody EncerramentoAcompanhamentoRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"PRONTUARIO.CONSULTAR", "PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "paciente/{pacienteId}/programas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Programas do paciente", description = "Com as regras do prontuário: vínculo assistencial ou acesso justificado "
            + "(ADR-0076; sem vínculo, 403 VINCULO_ASSISTENCIAL_AUSENTE) e auditoria de leitura. Programa sensível de unidade que você "
            + "não acompanha só conta em restritos.", tags = "Acompanhamento Programático")
    @ApiErrorResponsesBusca
    public ResponseEntity<AcompanhamentosDoPacienteDto> doPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.doPaciente(pacienteId));
    }

    @RequerPermissao({"PACIENTE.CONSULTAR", "PRONTUARIO.CONSULTAR", "PROGRAMA.ACOMPANHAR"})
    @GetMapping(value = "acompanhamento/resumo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Resumo dos acompanhamentos", description = "Quantos pacientes estão em algum programa, na rede. Só números.",
            tags = "Acompanhamento Programático")
    @ApiErrorResponsesBusca
    public ResponseEntity<ResumoAcompanhamentosDto> resumo() {
        return ResponseEntity.ok(service.resumo());
    }
}
