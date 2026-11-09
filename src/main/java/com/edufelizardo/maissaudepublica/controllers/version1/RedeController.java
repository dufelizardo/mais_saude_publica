package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CopiaCompetenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CotaPpiRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.OfertaAssistencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RedeDtos;
import com.edufelizardo.maissaudepublica.services.version1.RedeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/rede/")
@Tag(name = "Gestão da Rede · PPI", description = "Oferta assistencial e cotas da PPI, que a Regulação consome na autorização "
        + "(ver docs/adr/0179-gestao-da-rede-de-atencao.md).")
public class RedeController {

    private static final String TAG = "Gestão da Rede · PPI";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private RedeService service;

    @RequerPermissao({"REDE.CONSULTAR", "REDE.GERENCIAR"})
    @GetMapping(value = "ppi", produces = JSON)
    @Operation(summary = "Painel da PPI", description = "As ofertas da competência (padrão: o mês atual) com as cotas, o usado, o saldo e as extracotas. "
            + "Filtro: executanteId.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<RedeDtos.Painel> painel(@RequestParam(required = false) YearMonth competencia, @RequestParam(required = false) UUID executanteId) {
        return ResponseEntity.ok(service.painel(competencia, executanteId));
    }

    @RequerPermissao({"REDE.CONSULTAR", "REDE.GERENCIAR", "REGULACAO.REGULAR"})
    @GetMapping(value = "ppi/saldo", produces = JSON)
    @Operation(summary = "Saldo para a autorização", description = "O que a autorização vai encontrar para a solicitante na executante: se há oferta "
            + "controlada, a cota que vale e o saldo.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<RedeDtos.Saldo> saldo(@RequestParam UUID procedimentoId, @RequestParam UUID executanteId, @RequestParam UUID solicitanteId,
                                                @RequestParam YearMonth competencia) {
        return ResponseEntity.ok(service.saldo(procedimentoId, executanteId, solicitanteId, competencia));
    }

    @RequerPermissao({"REDE.GERENCIAR"})
    @PostMapping(value = "oferta", produces = JSON, consumes = JSON)
    @Operation(summary = "Cadastra a oferta", description = "Uma por executante, procedimento e competência (409); a executante precisa da capacidade que o "
            + "procedimento exige (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RedeDtos.Oferta> criarOferta(@Valid @RequestBody OfertaAssistencialRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarOferta(dto));
    }

    @RequerPermissao({"REDE.GERENCIAR"})
    @PutMapping(value = "oferta/{uuid}", produces = JSON, consumes = JSON)
    @Operation(summary = "Ajusta a oferta", description = "Só a quantidade e a observação; abaixo do pactuado ou do usado: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RedeDtos.Oferta> atualizarOferta(@PathVariable UUID uuid, @Valid @RequestBody OfertaAssistencialRequestDto dto) {
        return ResponseEntity.ok(service.atualizarOferta(uuid, dto));
    }

    @RequerPermissao({"REDE.GERENCIAR"})
    @PostMapping(value = "oferta/{uuid}/cota", produces = JSON, consumes = JSON)
    @Operation(summary = "Pactua uma cota", description = "Para uma unidade ou uma esfera; a soma não passa da oferta (422); a mesma beneficiária: 409.",
            tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RedeDtos.Oferta> criarCota(@PathVariable UUID uuid, @Valid @RequestBody CotaPpiRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarCota(uuid, dto));
    }

    @RequerPermissao({"REDE.GERENCIAR"})
    @PutMapping(value = "cota/{uuid}", produces = JSON, consumes = JSON)
    @Operation(summary = "Ajusta a cota", description = "Não fica abaixo do usado nem passa a oferta (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RedeDtos.Oferta> atualizarCota(@PathVariable UUID uuid, @Valid @RequestBody CotaPpiRequestDto dto) {
        return ResponseEntity.ok(service.atualizarCota(uuid, dto));
    }

    @RequerPermissao({"REDE.GERENCIAR"})
    @DeleteMapping(value = "cota/{uuid}", produces = JSON)
    @Operation(summary = "Remove a cota", description = "Só a que nunca foi usada (422).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RedeDtos.Oferta> removerCota(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.removerCota(uuid));
    }

    @RequerPermissao({"REDE.GERENCIAR"})
    @PostMapping(value = "ppi/copia", produces = JSON, consumes = JSON)
    @Operation(summary = "Copia a PPI de um mês para outro", description = "As ofertas e cotas que você gerencia; a oferta que já existe no destino fica "
            + "como está. Destino antes da origem: 400.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<RedeDtos.Copia> copiar(@Valid @RequestBody CopiaCompetenciaRequestDto dto) {
        return ResponseEntity.ok(service.copiar(dto));
    }
}
