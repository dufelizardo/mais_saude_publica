package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SolicitacaoTransporteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ViagemTransporteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransporteEletivoDtos;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoTransporte;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoTransporte;
import com.edufelizardo.maissaudepublica.services.version1.TransporteEletivoService;
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
@RequestMapping(value = "/api/v1/transporte/")
@Tag(name = "Transporte · eletivo e TFD", description = "Solicitação de transporte eletivo, autorização do TFD, viagens com embarques e "
        + "quilometragem (ver docs/adr/0190-transporte-eletivo-e-tfd.md).")
public class TransporteEletivoController {

    private static final String TAG = "Transporte · eletivo e TFD";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private TransporteEletivoService service;

    @RequerPermissao({"TRANSPORTE.SOLICITAR", "TRANSPORTE.CONSULTAR", "TRANSPORTE.GERENCIAR", "TFD.AUTORIZAR"})
    @GetMapping(value = "solicitacao", produces = JSON)
    @Operation(summary = "Solicitações de transporte", description = "A fila e o histórico: da unidade que pede, ou da rede para a frota e a comissão "
            + "de TFD. Filtros: unidadeId, pacienteId, situacao, motivo, de, ate (padrão: de 7 dias atrás a 60 dias à frente; até 120).", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TransporteEletivoDtos.Solicitacao>> listar(@RequestParam(required = false) UUID unidadeId,
                                                                          @RequestParam(required = false) UUID pacienteId,
                                                                          @RequestParam(required = false) SituacaoSolicitacaoTransporte situacao,
                                                                          @RequestParam(required = false) MotivoTransporte motivo,
                                                                          @RequestParam(required = false) LocalDate de,
                                                                          @RequestParam(required = false) LocalDate ate) {
        return ResponseEntity.ok(service.listar(unidadeId, pacienteId, situacao, motivo, de, ate));
    }

    @RequerPermissao({"TRANSPORTE.SOLICITAR", "TRANSPORTE.CONSULTAR", "TRANSPORTE.GERENCIAR", "TFD.AUTORIZAR"})
    @GetMapping(value = "solicitacao/{uuid}", produces = JSON)
    @Operation(summary = "A solicitação", description = "Com a viagem em que está.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<TransporteEletivoDtos.Solicitacao> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscar(uuid));
    }

    @RequerPermissao({"TRANSPORTE.SOLICITAR"})
    @PostMapping(value = "solicitacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Pede transporte", description = "Com agendamento, regulação ou internação (alta), a data e o destino vêm deles (um só, 400; de "
            + "outro paciente ou em situação que não serve, 422). Acompanhante para menor de 18 anos e para maca (400). TFD: laudo com CID, justificativa "
            + "e matrícula (400), para outro município (422); fica aguardando a comissão. O mesmo paciente na mesma hora: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteEletivoDtos.Solicitacao> solicitar(@Valid @RequestBody SolicitacaoTransporteRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.solicitar(dto));
    }

    @RequerPermissao({"TRANSPORTE.SOLICITAR"})
    @PostMapping(value = "solicitacao/recorrente", produces = JSON, consumes = JSON)
    @Operation(summary = "Pede transporte recorrente", description = "Hemodiálise e terapia seriada (400 nos outros): uma solicitação por data nos dias "
            + "da semana, da primeira sessão até a data final (até 90 dias, 400).", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<TransporteEletivoDtos.Solicitacao>> solicitarRecorrente(@Valid @RequestBody SolicitacaoTransporteRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.solicitarRecorrente(dto));
    }

    @RequerPermissao({"TRANSPORTE.SOLICITAR"})
    @PostMapping(value = "solicitacao/{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a solicitação", description = "Com motivo; numa viagem ou já encerrada, 422. Com recorrencia=true, cancela também as "
            + "próximas da recorrência que não estão numa viagem.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<List<TransporteEletivoDtos.Solicitacao>> cancelar(@PathVariable UUID uuid,
                                                                            @Valid @RequestBody ViagemTransporteRequestDto.Motivo dto) {
        return ResponseEntity.ok(service.cancelar(uuid, dto));
    }

    @RequerPermissao({"TFD.AUTORIZAR"})
    @PostMapping(value = "solicitacao/{uuid}/autorizacao", produces = JSON)
    @Operation(summary = "Autoriza o TFD", description = "A comissão de TFD; fora de aguardando autorização, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteEletivoDtos.Solicitacao> autorizar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.autorizar(uuid));
    }

    @RequerPermissao({"TFD.AUTORIZAR"})
    @PostMapping(value = "solicitacao/{uuid}/negacao", produces = JSON, consumes = JSON)
    @Operation(summary = "Nega o TFD", description = "Com motivo; fora de aguardando autorização, 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteEletivoDtos.Solicitacao> negar(@PathVariable UUID uuid, @Valid @RequestBody ViagemTransporteRequestDto.Motivo dto) {
        return ResponseEntity.ok(service.negar(uuid, dto));
    }

    @RequerPermissao({"TRANSPORTE.CONSULTAR", "TRANSPORTE.GERENCIAR"})
    @GetMapping(value = "viagem", produces = JSON)
    @Operation(summary = "Agenda de viagens", description = "As que cruzam o dia (padrão: hoje) e os próximos (dias: 1, até 14). Filtros: veiculoId, "
            + "unidadeId.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TransporteEletivoDtos.Viagem>> viagens(@RequestParam(required = false) LocalDate data,
                                                                      @RequestParam(required = false) Integer dias,
                                                                      @RequestParam(required = false) UUID veiculoId,
                                                                      @RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.viagens(data, dias, veiculoId, unidadeId));
    }

    @RequerPermissao({"TRANSPORTE.CONSULTAR", "TRANSPORTE.GERENCIAR"})
    @GetMapping(value = "viagem/{uuid}", produces = JSON)
    @Operation(summary = "A viagem", description = "Com os embarques.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<TransporteEletivoDtos.Viagem> viagem(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.viagem(uuid));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PostMapping(value = "viagem", produces = JSON, consumes = JSON)
    @Operation(summary = "Programa uma viagem", description = "Veículo eletivo (do SAMU, inativo ou em manutenção: 422), condutor, janela até 72 horas "
            + "(400) e as solicitações na fila com o compromisso dentro da janela (TFD sem autorização ou fora da janela: 422; já noutra viagem: 409). "
            + "Lugares, maca e cadeira de rodas: 422. Veículo ou condutor noutra viagem no horário: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteEletivoDtos.Viagem> programar(@Valid @RequestBody ViagemTransporteRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.programar(dto));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PutMapping(value = "viagem/{uuid}/embarques", produces = JSON, consumes = JSON)
    @Operation(summary = "Troca os embarques", description = "Só programada (422); quem sai volta para a fila.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteEletivoDtos.Viagem> embarques(@PathVariable UUID uuid, @Valid @RequestBody ViagemTransporteRequestDto.Embarques dto) {
        return ResponseEntity.ok(service.embarques(uuid, dto));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PostMapping(value = "viagem/{uuid}/saida", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a saída", description = "Com a quilometragem (não volta, 422); o veículo fica em serviço.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteEletivoDtos.Viagem> sair(@PathVariable UUID uuid, @Valid @RequestBody ViagemTransporteRequestDto.Quilometragem dto) {
        return ResponseEntity.ok(service.sair(uuid, dto));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PostMapping(value = "viagem/{uuid}/embarque", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra quem embarcou", description = "Com a viagem em andamento (422); quem faltou fica com a falta na solicitação.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteEletivoDtos.Viagem> embarque(@PathVariable UUID uuid, @Valid @RequestBody ViagemTransporteRequestDto.Presencas dto) {
        return ResponseEntity.ok(service.presencas(uuid, dto));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PostMapping(value = "viagem/{uuid}/chegada", produces = JSON, consumes = JSON)
    @Operation(summary = "Registra a chegada", description = "Todos com o embarque registrado (422); a quilometragem não fica abaixo da saída (422) e "
            + "atualiza o veículo, que volta a disponível.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteEletivoDtos.Viagem> chegar(@PathVariable UUID uuid, @Valid @RequestBody ViagemTransporteRequestDto.Quilometragem dto) {
        return ResponseEntity.ok(service.chegar(uuid, dto));
    }

    @RequerPermissao({"TRANSPORTE.GERENCIAR"})
    @PostMapping(value = "viagem/{uuid}/cancelamento", produces = JSON, consumes = JSON)
    @Operation(summary = "Cancela a viagem", description = "Antes da saída (422), com motivo; as solicitações voltam para a fila.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<TransporteEletivoDtos.Viagem> cancelarViagem(@PathVariable UUID uuid, @Valid @RequestBody ViagemTransporteRequestDto.Motivo dto) {
        return ResponseEntity.ok(service.cancelarViagem(uuid, dto));
    }

    @RequerPermissao({"TRANSPORTE.CONSULTAR", "TRANSPORTE.GERENCIAR"})
    @GetMapping(value = "quilometragem", produces = JSON)
    @Operation(summary = "Quilometragem por veículo", description = "Das viagens concluídas com a chegada no período (padrão: os últimos 30 dias; até um "
            + "ano). Filtro: unidadeId.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<TransporteEletivoDtos.Quilometragem>> quilometragem(@RequestParam(required = false) LocalDate de,
                                                                                   @RequestParam(required = false) LocalDate ate,
                                                                                   @RequestParam(required = false) UUID unidadeId) {
        return ResponseEntity.ok(service.quilometragem(de, ate, unidadeId));
    }
}
