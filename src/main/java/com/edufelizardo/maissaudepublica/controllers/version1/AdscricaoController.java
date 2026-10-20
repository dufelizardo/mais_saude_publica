package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AdscricaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoAtribuicaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AdscricaoDto;
import com.edufelizardo.maissaudepublica.services.version1.AdscricaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/territorio/")
@Tag(name = "Adscrição", description = "Equipe de referência do paciente (pelo domicílio ou manual) e cobertura da equipe "
        + "(ver docs/adr/0128-adscricao-e-cobertura.md).")
public class AdscricaoController {

    private static final String TAG = "Adscrição";

    @Autowired
    private AdscricaoService service;

    @AuditarLeitura
    @RequerPermissao({"PACIENTE.CONSULTAR", "TERRITORIO.CADASTRAR", "TERRITORIO.GERENCIAR"})
    @GetMapping(value = "paciente/{pacienteId}/adscricao", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Equipe de referência do paciente", description = "A adscrição vigente (território ou manual), o domicílio, a "
            + "microárea com o ACS e o histórico. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<AdscricaoDto.DoPaciente> doPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.doPaciente(pacienteId));
    }

    @RequerPermissao({"TERRITORIO.CADASTRAR"})
    @PostMapping(value = "paciente/{pacienteId}/adscricao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Adscreve o paciente à mão", description = "Com motivo (sem domicílio no território, por escolha); a vigente se "
            + "encerra. Equipe inativa: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AdscricaoDto.DoPaciente> adscrever(@PathVariable UUID pacienteId, @Valid @RequestBody AdscricaoRequestDto dto) {
        return ResponseEntity.ok(service.adscrever(pacienteId, dto));
    }

    @RequerPermissao({"TERRITORIO.CADASTRAR"})
    @PostMapping(value = "paciente/{pacienteId}/adscricao/encerrar", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encerra a adscrição manual", description = "O paciente volta à do território, se mora em área com equipe. A do "
            + "território não se encerra por aqui (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AdscricaoDto.DoPaciente> encerrar(@PathVariable UUID pacienteId, @Valid @RequestBody EncerramentoAtribuicaoRequestDto dto) {
        return ResponseEntity.ok(service.encerrar(pacienteId, dto));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "equipe/{uuid}/cobertura", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cobertura da equipe", description = "Pessoas adscritas, famílias, imóveis e, por microárea, as pessoas "
            + "cadastradas contra a população de referência. Só números.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<AdscricaoDto.Cobertura> cobertura(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.cobertura(uuid));
    }
}
