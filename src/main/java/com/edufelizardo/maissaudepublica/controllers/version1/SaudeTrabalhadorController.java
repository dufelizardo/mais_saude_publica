package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcompanhamentoSorologicoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EventoTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NotificacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SaudeTrabalhadorDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ComunicacaoVisaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ControleRiscoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncaminhamentoTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InvestigacaoTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MedidaTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NexoTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RiscoOcupacionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VerificacaoMedidaTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRiscoOcupacional;
import com.edufelizardo.maissaudepublica.services.version1.InvestigacaoTrabalhoService;
import com.edufelizardo.maissaudepublica.services.version1.SaudeTrabalhadorService;
import com.edufelizardo.maissaudepublica.services.version1.VigilanciaService;
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
@Tag(name = "Vigilância epidemiológica", description = "Saúde do Trabalhador: o bloco do trabalho da notificação, o acompanhamento sorológico do "
        + "acidente com material biológico e os acidentes dos servidores sem notificação (ver a ADR-0207).")
public class SaudeTrabalhadorController {

    private static final String TAG = "Vigilância epidemiológica";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private SaudeTrabalhadorService service;

    @Autowired
    private VigilanciaService vigilanciaService;

    @Autowired
    private InvestigacaoTrabalhoService investigacaoService;

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

    // ── V5b: investigação do trabalho (ADR-0208) ──

    @RequerPermissao("VIGILANCIA.INVESTIGAR")
    @PutMapping(value = "notificacao/{uuid}/trabalho/investigacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a visita ao local de trabalho", description = "Inicia ou atualiza a investigação do ambiente (visita, local e achados); sem o bloco do trabalho ou encerrada, 422; visita no futuro ou antes do evento, 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> investigar(@PathVariable UUID uuid, @Valid @RequestBody InvestigacaoTrabalhoRequestDto dto) {
        investigacaoService.investigar(uuid, dto);
        return ResponseEntity.ok(vigilanciaService.notificacao(uuid));
    }

    @RequerPermissao("VIGILANCIA.INVESTIGAR")
    @PostMapping(value = "notificacao/{uuid}/trabalho/investigacao/nexo", produces = JSON, consumes = JSON)
    @Operation(summary = "Conclui o nexo com o trabalho", description = "Confirmado, descartado ou inconclusivo, com a justificativa (400); sem a investigação, 422. O descartado sai dos indicadores do trabalho.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> nexo(@PathVariable UUID uuid, @Valid @RequestBody NexoTrabalhoRequestDto dto) {
        investigacaoService.concluirNexo(uuid, dto);
        return ResponseEntity.ok(vigilanciaService.notificacao(uuid));
    }

    @RequerPermissao("VIGILANCIA.INVESTIGAR")
    @PostMapping(value = "notificacao/{uuid}/trabalho/investigacao/medida", produces = JSON, consumes = JSON)
    @Operation(summary = "Recomenda uma medida", description = "Com tipo, descrição e prazo (no passado, 400); sem a investigação, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> medida(@PathVariable UUID uuid, @Valid @RequestBody MedidaTrabalhoRequestDto dto) {
        investigacaoService.recomendarMedida(uuid, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(vigilanciaService.notificacao(uuid));
    }

    @RequerPermissao("VIGILANCIA.INVESTIGAR")
    @PostMapping(value = "medida-trabalho/{uuid}/verificacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Verifica a medida", description = "Cumprida ou não cumprida (com a observação, 400); já verificada, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> verificar(@PathVariable UUID uuid, @Valid @RequestBody VerificacaoMedidaTrabalhoRequestDto dto) {
        UUID notificacao = investigacaoService.verificarMedida(uuid, dto);
        return ResponseEntity.ok(vigilanciaService.notificacao(notificacao));
    }

    @RequerPermissao("VIGILANCIA.INVESTIGAR")
    @PostMapping(value = "notificacao/{uuid}/trabalho/investigacao/comunicacao-visa", produces = JSON, consumes = JSON)
    @Operation(summary = "Comunica a vigilância sanitária", description = "Abre a denúncia de canal Vigilância contra o empregador, sem os dados do trabalhador; uma por investigação (409); sem empregador, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> comunicarVisa(@PathVariable UUID uuid, @Valid @RequestBody ComunicacaoVisaRequestDto dto) {
        investigacaoService.comunicarVisa(uuid, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(vigilanciaService.notificacao(uuid));
    }

    @RequerPermissao("VIGILANCIA.INVESTIGAR")
    @PostMapping(value = "notificacao/{uuid}/trabalho/investigacao/encaminhamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Encaminha à rede intersetorial", description = "Superintendência do Trabalho, MPT ou sindicato, com a investigação como origem; instituição inativa, 422; em aberto para a mesma, 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<NotificacaoDto> encaminhar(@PathVariable UUID uuid, @Valid @RequestBody EncaminhamentoTrabalhoRequestDto dto) {
        investigacaoService.encaminhar(uuid, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(vigilanciaService.notificacao(uuid));
    }

    @RequerPermissao("VIGILANCIA.INVESTIGAR")
    @PostMapping(value = "saude-trabalhador/risco", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra um risco ocupacional", description = "Do empregador (CPF ou CNPJ válido, 400; sem empregador, 400); pela investigação, com a notificação, o empregador vem do bloco.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SaudeTrabalhadorDtos.Risco> registrarRisco(@Valid @RequestBody RiscoOcupacionalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investigacaoService.registrarRisco(dto));
    }

    @RequerPermissao("VIGILANCIA.INVESTIGAR")
    @PostMapping(value = "risco-ocupacional/{uuid}/controle", produces = JSON, consumes = JSON)
    @Operation(summary = "Marca o risco como controlado", description = "Com o motivo; já controlado, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<SaudeTrabalhadorDtos.Risco> controlarRisco(@PathVariable UUID uuid, @Valid @RequestBody ControleRiscoRequestDto dto) {
        return ResponseEntity.ok(investigacaoService.controlarRisco(uuid, dto));
    }

    @RequerPermissao("VIGILANCIA.INVESTIGAR")
    @GetMapping(value = "saude-trabalhador/riscos", produces = JSON)
    @Operation(summary = "Os riscos ocupacionais", description = "Ativos primeiro; filtra pela situação.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<SaudeTrabalhadorDtos.Risco>> riscos(@RequestParam(required = false) SituacaoRiscoOcupacional situacao) {
        return ResponseEntity.ok(investigacaoService.riscos(situacao));
    }

    @RequerPermissao({"VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR"})
    @GetMapping(value = "saude-trabalhador/semana", produces = JSON)
    @Operation(summary = "A semana da saúde do trabalhador", description = "Para a Sala de Situação: acidentes de trabalho dos últimos 7 dias (sem o nome do trabalhador), óbitos, investigações abertas, medidas vencidas e riscos ativos.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<SaudeTrabalhadorDtos.Semana> semana() {
        return ResponseEntity.ok(service.semana());
    }
}
