package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.AuditarLeitura;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesBusca;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesListagem;
import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DomicilioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ImovelRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MembroFamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MudancaFamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RendaFamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResponsavelFamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SaidaMembroFamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CadastroTerritorialDto;
import com.edufelizardo.maissaudepublica.services.version1.CadastroTerritorialService;
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
@RequestMapping(value = "/api/v1/territorio/")
@Tag(name = "Cadastro territorial", description = "Imóvel, domicílio, família e membros, com histórico de moradia "
        + "(ver docs/adr/0127-cadastro-territorial.md).")
public class CadastroTerritorialController {

    private static final String TAG = "Cadastro territorial";
    private static final String CADASTRAR = "TERRITORIO.CADASTRAR";

    @Autowired
    private CadastroTerritorialService service;

    @RequerPermissao({CADASTRAR, "TERRITORIO.GERENCIAR"})
    @GetMapping(value = "area/{uuid}/imoveis", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Imóveis da área", description = "Com domicílios, famílias e pessoas; microareaId filtra. Fora do escopo: 403.", tags = TAG)
    @ApiErrorResponsesListagem
    public ResponseEntity<List<CadastroTerritorialDto.Imovel>> imoveis(@PathVariable UUID uuid, @RequestParam(required = false) UUID microareaId) {
        return ResponseEntity.ok(service.imoveis(uuid, microareaId));
    }

    @AuditarLeitura
    @RequerPermissao({CADASTRAR, "TERRITORIO.GERENCIAR"})
    @GetMapping(value = "imovel/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Um imóvel", description = "Os domicílios com as famílias atuais e anteriores e os membros. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> imovel(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.imovel(uuid));
    }

    @AuditarLeitura
    @RequerPermissao({CADASTRAR, "TERRITORIO.GERENCIAR"})
    @GetMapping(value = "paciente/{pacienteId}/familia", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "A família do paciente", description = "A família atual (ou nula), o domicílio e onde ele já morou. Leitura auditada.", tags = TAG)
    @ApiErrorResponsesBusca
    public ResponseEntity<CadastroTerritorialDto.FamiliaDoPaciente> doPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.doPaciente(pacienteId));
    }

    @RequerPermissao({CADASTRAR})
    @PostMapping(value = "imovel", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um imóvel", description = "Ponto fora da microárea: 422. Sem microárea, o ponto a encontra (sem achar, 400). "
            + "Área sem equipe responsável: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> criarImovel(@Valid @RequestBody ImovelRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarImovel(dto));
    }

    @RequerPermissao({CADASTRAR})
    @PatchMapping(value = "imovel/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um imóvel", description = "Outra área ou deixar de ser de moradia com domicílio: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> atualizarImovel(@PathVariable UUID uuid, @Valid @RequestBody ImovelRequestDto dto) {
        return ResponseEntity.ok(service.atualizarImovel(uuid, dto));
    }

    @RequerPermissao({CADASTRAR})
    @PostMapping(value = "domicilio", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um domicílio", description = "Só em imóvel de moradia (422). Identificação repetida no imóvel: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> criarDomicilio(@Valid @RequestBody DomicilioRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarDomicilio(dto));
    }

    @RequerPermissao({CADASTRAR})
    @PatchMapping(value = "domicilio/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edita um domicílio", description = "Inativar com família morando: 422.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> atualizarDomicilio(@PathVariable UUID uuid, @Valid @RequestBody DomicilioRequestDto dto) {
        return ResponseEntity.ok(service.atualizarDomicilio(uuid, dto));
    }

    @RequerPermissao({CADASTRAR})
    @PostMapping(value = "familia", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra uma família", description = "O responsável e os outros membros, com parentesco. Pessoa já em outra família: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> criarFamilia(@Valid @RequestBody FamiliaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarFamilia(dto));
    }

    @RequerPermissao({CADASTRAR})
    @PatchMapping(value = "familia/{uuid}/renda", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Muda a faixa de renda da família", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> renda(@PathVariable UUID uuid, @Valid @RequestBody RendaFamiliaRequestDto dto) {
        return ResponseEntity.ok(service.atualizarRenda(uuid, dto));
    }

    @RequerPermissao({CADASTRAR})
    @PostMapping(value = "familia/{uuid}/membro", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inclui uma pessoa na família", description = "Pessoa já em outra família: 409.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> incluirMembro(@PathVariable UUID uuid, @Valid @RequestBody MembroFamiliaRequestDto dto) {
        return ResponseEntity.ok(service.incluirMembro(uuid, dto));
    }

    @RequerPermissao({CADASTRAR})
    @PostMapping(value = "familia/membro/{uuid}/saida", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra a saída de uma pessoa", description = "O responsável sai por último (422); sem membros, a família se encerra.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> saida(@PathVariable UUID uuid, @Valid @RequestBody SaidaMembroFamiliaRequestDto dto) {
        return ResponseEntity.ok(service.registrarSaida(uuid, dto));
    }

    @RequerPermissao({CADASTRAR})
    @PostMapping(value = "familia/{uuid}/responsavel", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Troca o responsável da família", description = "Outro membro vigente (422), com o parentesco do anterior.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> responsavel(@PathVariable UUID uuid, @Valid @RequestBody ResponsavelFamiliaRequestDto dto) {
        return ResponseEntity.ok(service.trocarResponsavel(uuid, dto));
    }

    @RequerPermissao({CADASTRAR})
    @PostMapping(value = "familia/{uuid}/mudanca", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Muda a família de domicílio", description = "A família inteira; a de origem se encerra e a nova começa na data. "
            + "Devolve o imóvel de destino.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> mudanca(@PathVariable UUID uuid, @Valid @RequestBody MudancaFamiliaRequestDto dto) {
        return ResponseEntity.ok(service.mudar(uuid, dto));
    }

    @RequerPermissao({CADASTRAR})
    @PostMapping(value = "familia/membro/{uuid}/endereco-do-imovel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Leva o endereço do imóvel ao cadastro do paciente", description = "Ação explícita; pede também PACIENTE.CADASTRAR.", tags = TAG)
    @ApiErrorResponsesMutacao
    public ResponseEntity<CadastroTerritorialDto.ImovelDetalhe> enderecoDoImovel(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.atualizarEnderecoDoPaciente(uuid));
    }
}
