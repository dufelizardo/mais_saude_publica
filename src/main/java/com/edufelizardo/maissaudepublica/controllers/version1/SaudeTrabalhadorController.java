package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcompanhamentoSorologicoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EventoTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NotificacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SaudeTrabalhadorDtos;
import com.edufelizardo.maissaudepublica.services.version1.SaudeTrabalhadorService;
import com.edufelizardo.maissaudepublica.services.version1.VigilanciaService;
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
@RequestMapping(value = "/api/v1/vigilancia/")
@Tag(name = "Vigilância epidemiológica", description = "Saúde do Trabalhador: o bloco do trabalho da notificação, o acompanhamento sorológico do "
        + "acidente com material biológico e os acidentes dos servidores sem notificação (ver a ADR-0207).")
public class SaudeTrabalhadorController {

    private static final String TAG = "Vigilância epidemiológica";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private SaudeTrabalhadorService service;

    @Autowired
    private VigilanciaService vigilanciaService;

    @RequerPermissao("VIGILANCIA.INVESTIGAR")
    @PutMapping(value = "notificacao/{uuid}/trabalho", produces = JSON, consumes = JSON)
    @Operation(summary = "Completa ou corrige o bloco do trabalho", description = "Ocupação (CBO de seis dígitos, 400), situação, empregador (CPF ou CNPJ "
            + "válido, 400; ligado ao estabelecimento da vigilância sanitária pelo documento), local e, só no acidente (400), tipo, jornada, CAT, óbito "
            + "e o acidente do RH (404; em outra notificação, 409; de outro CPF, 422). Encerrada, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody EventoTrabalhoRequestDto dto) {
        service.atualizar(uuid, dto);
        return ResponseEntity.ok(vigilanciaService.notificacao(uuid));
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @PostMapping(value = "notificacao/{uuid}/trabalho/acompanhamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra uma etapa do acompanhamento sorológico", description = "Só no acidente com material biológico aberto (422); a data "
            + "não pode ser no futuro nem antes do acidente (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> acompanhamento(@PathVariable UUID uuid, @Valid @RequestBody AcompanhamentoSorologicoRequestDto dto) {
        service.registrarAcompanhamento(uuid, dto);
        return ResponseEntity.ok(vigilanciaService.notificacao(uuid));
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @AuditarLeitura
    @GetMapping(value = "saude-trabalhador/painel", produces = JSON)
    @Operation(summary = "Painel da saúde do trabalhador", description = "As notificações relacionadas ao trabalho do escopo, os totais e as etapas "
            + "do acompanhamento sorológico vencidas ou nos próximos 7 dias.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<SaudeTrabalhadorDtos.Painel> painel(@RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.painel(unidadeId));
    }

    @RequerPermissao("VIGILANCIA.INVESTIGAR")
    @AuditarLeitura
    @GetMapping(value = "saude-trabalhador/acidentes-sem-notificacao", produces = JSON)
    @Operation(summary = "Acidentes de servidores sem notificação", description = "Os acidentes registrados no RH que ainda não têm notificação, com o "
            + "paciente de mesmo CPF; só para a vigilância com acesso à rede inteira (403).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<SaudeTrabalhadorDtos.AcidenteSemNotificacao>> acidentesSemNotificacao() {
        return ResponseEntity.ok(service.acidentesSemNotificacao());
    }
}
