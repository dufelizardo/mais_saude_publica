package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConfiguracaoAcolhimentoNoturnoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PermanenciaAcolhimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProrrogacaoAcolhimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SaidaAcolhimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcolhimentoNoturnoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelRapsDto;
import com.edufelizardo.maissaudepublica.services.version1.AcolhimentoNoturnoService;
import com.edufelizardo.maissaudepublica.services.version1.RedePsicossocialService;
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
@RequestMapping(value = "/api/v1/saude-mental/")
@Tag(name = "Saúde Mental · CAPS e RAPS", description = "A Rede de Atenção Psicossocial pelos componentes e o acolhimento noturno do CAPS "
        + "(ver docs/adr/0153-saude-mental-caps-e-raps.md).")
public class RapsController {

    private static final String TAG = "Saúde Mental · CAPS e RAPS";

    @Autowired
    private RedePsicossocialService redeService;

    @Autowired
    private AcolhimentoNoturnoService acolhimentoService;

    @RequerPermissao({"INDICADOR.CONSULTAR", "SAUDE_MENTAL.ACOMPANHAR"})
    @GetMapping(value = "raps", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rede de Atenção Psicossocial", description = "Os pontos por componente (pelos perfis e capacidades das unidades), com os "
            + "casos em acompanhamento e as intervenções dos últimos 90 dias, e as lacunas da rede. Só números.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<PainelRapsDto> raps() {
        return ResponseEntity.ok(redeService.painel());
    }

    @AuditarLeitura
    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @GetMapping(value = "acolhimento-noturno", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Acolhimento noturno da unidade", description = "As vagas, a ocupação, quem está acolhido e as saídas dos últimos 30 dias.",
            tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<AcolhimentoNoturnoDto> acolhimento(@RequestParam UUID unidadeId) {
        return ResponseEntity.ok(acolhimentoService.painel(unidadeId));
    }

    @RequerPermissao({"ORGANIZACAO.GERENCIAR"})
    @PutMapping(value = "acolhimento-noturno/configuracao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Configura o acolhimento noturno", description = "Vagas e permanência máxima. Unidade sem a capacidade, ou vagas abaixo da "
            + "ocupação, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcolhimentoNoturnoDto> configurar(@RequestParam UUID unidadeId, @Valid @RequestBody ConfiguracaoAcolhimentoNoturnoRequestDto dto) {
        return ResponseEntity.ok(acolhimentoService.configurar(unidadeId, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acolhimento-noturno", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Entrada no acolhimento noturno", description = "Unidade sem a capacidade ou sem configuração, paciente sem caso na unidade, "
            + "sem vaga, ou além da permanência máxima sem justificativa: 422; já acolhido: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcolhimentoNoturnoDto.Permanencia> entrar(@Valid @RequestBody PermanenciaAcolhimentoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(acolhimentoService.entrar(dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acolhimento-noturno/{uuid}/prorrogacao", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Prorroga a permanência", description = "Nova saída prevista; além da permanência máxima, com a justificativa (422).",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcolhimentoNoturnoDto.Permanencia> prorrogar(@PathVariable UUID uuid, @Valid @RequestBody ProrrogacaoAcolhimentoRequestDto dto) {
        return ResponseEntity.ok(acolhimentoService.prorrogar(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "acolhimento-noturno/{uuid}/saida", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Saída do acolhimento noturno", description = "Com o motivo; entre a entrada e hoje (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcolhimentoNoturnoDto.Permanencia> sair(@PathVariable UUID uuid, @Valid @RequestBody SaidaAcolhimentoRequestDto dto) {
        return ResponseEntity.ok(acolhimentoService.sair(uuid, dto));
    }

    @AuditarLeitura
    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @GetMapping(value = "paciente/{pacienteId}/acolhimento-noturno", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Acolhimentos noturnos do paciente", description = "As permanências, com as regras do prontuário.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<AcolhimentoNoturnoDto.Permanencia>> doPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(acolhimentoService.doPaciente(pacienteId));
    }
}
