package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncaminhamentoIntersetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InstituicaoExternaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetornoEncaminhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.InstituicaoExternaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelIntersetorialDtos.CobrancaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelIntersetorialDtos.PainelDto;
import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SetorIntersetorial;
import com.edufelizardo.maissaudepublica.services.version1.InstituicaoExternaService;
import com.edufelizardo.maissaudepublica.services.version1.IntersetorialSaudeMentalService;
import com.edufelizardo.maissaudepublica.services.version1.PainelIntersetorialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/")
@Tag(name = "Intersetorialidade", description = "O catálogo de instituições externas o encaminhamento intersetorial do caso em saúde mental "
        + "e o painel da rede (ver docs/adr/0154-intersetorialidade.md e docs/adr/0155-saude-mental-intersetorialidade-e-indicadores.md e docs/adr/0211-intersetorialidade-painel-da-rede.md).")
public class IntersetorialidadeController {

    private static final String TAG = "Intersetorialidade";

    @Autowired
    private InstituicaoExternaService instituicaoService;

    @Autowired
    private IntersetorialSaudeMentalService saudeMentalService;

    @Autowired
    private PainelIntersetorialService painelService;

    @LiberadoParaAutenticados
    @GetMapping(value = "instituicoes-externas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Catálogo de instituições externas", description = "Em ordem de nome, pelo setor e, com ativas=true, só as ativas. "
            + "Aberto a quem está logado.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<List<InstituicaoExternaDto>> listar(@RequestParam(required = false) SetorIntersetorial setor,
                                                              @RequestParam(required = false) Boolean ativas) {
        return ResponseEntity.ok(instituicaoService.listar(setor, ativas));
    }

    @LiberadoParaAutenticados
    @GetMapping(value = "instituicao-externa/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Instituição externa", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<InstituicaoExternaDto> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(instituicaoService.buscar(uuid));
    }

    @RequerPermissao({"INTERSETORIAL.GERENCIAR"})
    @PostMapping(value = "instituicao-externa", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra a instituição externa", description = "Nome repetido, 409; tipo de outro setor, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<InstituicaoExternaDto> criar(@Valid @RequestBody InstituicaoExternaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(instituicaoService.criar(dto));
    }

    @RequerPermissao({"INTERSETORIAL.GERENCIAR"})
    @PutMapping(value = "instituicao-externa/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ajusta a instituição externa", description = "Também inativa (ativa=false); sem ativa, mantém.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<InstituicaoExternaDto> atualizar(@PathVariable UUID uuid, @Valid @RequestBody InstituicaoExternaRequestDto dto) {
        return ResponseEntity.ok(instituicaoService.atualizar(uuid, dto));
    }

    @RequerPermissao({"INTERSETORIAL.GERENCIAR"})
    @GetMapping(value = "intersetorial/painel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Painel da rede intersetorial", description = "Por instituição: enviados, concluídos, recusados, cancelados, taxa de "
            + "resolução e tempo médio até o retorno dos enviados no período (padrão: 90 dias até hoje; máximo 366, senão 400); em aberto e sem "
            + "retorno de hoje, de qualquer data. Sem paciente e sem motivo (ADR-0211).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<PainelDto> painel(@RequestParam(required = false) LocalDate inicio, @RequestParam(required = false) LocalDate fim,
                                            @RequestParam(required = false) SetorIntersetorial setor,
                                            @RequestParam(required = false) DominioOrigemEncaminhamento origem,
                                            @RequestParam(required = false) UUID unidade) {
        return ResponseEntity.ok(painelService.painel(inicio, fim, setor, origem, unidade));
    }

    @RequerPermissao({"INTERSETORIAL.GERENCIAR"})
    @GetMapping(value = "intersetorial/instituicao/{uuid}/sem-retorno", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encaminhamentos sem retorno da instituição", description = "Para cobrar: número, origem, unidade, datas e dias de "
            + "atraso, do mais atrasado ao menos. Sem paciente e sem motivo (ADR-0211).", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CobrancaDto> semRetorno(@PathVariable UUID uuid) {
        return ResponseEntity.ok(painelService.semRetorno(uuid));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "saude-mental/acompanhamento/{uuid}/encaminhamento-intersetorial", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Encaminha o caso a uma instituição externa", description = "Instituição inativa, 422; envio no futuro ou retorno "
            + "esperado antes do envio, 400; já em aberto para a mesma instituição, 409. Devolve o caso.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> encaminhar(@PathVariable UUID uuid, @Valid @RequestBody EncaminhamentoIntersetorialRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saudeMentalService.encaminhar(uuid, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "saude-mental/acompanhamento/{uuid}/encaminhamento-intersetorial/{encaminhamentoId}/recebimento",
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "A instituição recebeu", description = "Só do enviado (422); a data entre o envio e hoje (400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> receber(@PathVariable UUID uuid, @PathVariable UUID encaminhamentoId,
                                                                @Valid @RequestBody RetornoEncaminhamentoRequestDto dto) {
        return ResponseEntity.ok(saudeMentalService.receber(uuid, encaminhamentoId, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "saude-mental/acompanhamento/{uuid}/encaminhamento-intersetorial/{encaminhamentoId}/retorno",
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "O retorno da instituição", description = "Conclui (CONCLUIDO) ou recusa (RECUSADO), com o texto (400). Já fechado, 422.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> retorno(@PathVariable UUID uuid, @PathVariable UUID encaminhamentoId,
                                                                @Valid @RequestBody RetornoEncaminhamentoRequestDto dto) {
        return ResponseEntity.ok(saudeMentalService.retorno(uuid, encaminhamentoId, dto));
    }

    @RequerPermissao({"SAUDE_MENTAL.ACOMPANHAR"})
    @PostMapping(value = "saude-mental/acompanhamento/{uuid}/encaminhamento-intersetorial/{encaminhamentoId}/cancelamento",
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancela o encaminhamento", description = "Com o motivo; só em aberto (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<AcompanhamentoSaudeMentalDto> cancelar(@PathVariable UUID uuid, @PathVariable UUID encaminhamentoId,
                                                                 @Valid @RequestBody MotivoSaudeMentalRequestDto dto) {
        return ResponseEntity.ok(saudeMentalService.cancelar(uuid, encaminhamentoId, dto));
    }
}
