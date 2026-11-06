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
import com.edufelizardo.maissaudepublica.models.enuns.SetorIntersetorial;
import com.edufelizardo.maissaudepublica.services.version1.InstituicaoExternaService;
import com.edufelizardo.maissaudepublica.services.version1.IntersetorialSaudeMentalService;
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
@RequestMapping(value = "/api/v1/")
@Tag(name = "Intersetorialidade", description = "O catálogo de instituições externas e o encaminhamento intersetorial do caso em saúde mental "
        + "(ver docs/adr/0154-intersetorialidade.md e docs/adr/0155-saude-mental-intersetorialidade-e-indicadores.md).")
public class IntersetorialidadeController {

    private static final String TAG = "Intersetorialidade";

    @Autowired
    private InstituicaoExternaService instituicaoService;

    @Autowired
    private IntersetorialSaudeMentalService saudeMentalService;

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
