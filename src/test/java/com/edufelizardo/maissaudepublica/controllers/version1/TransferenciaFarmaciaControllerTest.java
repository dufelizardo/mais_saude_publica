package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.MovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoFarmaciaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TransferenciaFarmaciaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da transferência de estoque entre unidades (ADR-0059). Mesmo padrão dos testes do livro
 * (ADR-0057): SEM {@code @Transactional} na classe, limpeza manual no {@code @AfterEach} — livro,
 * depois transferências, depois lotes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransferenciaFarmaciaControllerTest {

    private static final String TRANSFERENCIA_URL = "/api/v1/transferencia-farmacia/";
    private static final String MOVIMENTACAO_URL = "/api/v1/movimentacao-farmacia/";
    private static final String LOTE_URL = "/api/v1/lote/";
    private static final String MEDICAMENTO_URL = "/api/v1/medicamento/";
    private static final String UNIDADE_SAUDE_URL = "/api/v1/unidade-saude/";
    private static final String FEDERAL_URL = "/api/v1/federal/";
    private static final String ESTADUAL_URL = "/api/v1/estadual/";
    private static final String MUNICIPAL_URL = "/api/v1/municipal/";
    private static final String PROFISSIONAL_URL = "/api/v1/profissional/";
    private static final String PREFIXO_NOME_TESTE = "Transferencia Teste - ";
    private static final String PREFIXO_CPF_TESTE = "77744433";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovimentacaoFarmaciaRepository movimentacaoFarmaciaRepository;

    @Autowired
    private TransferenciaFarmaciaRepository transferenciaFarmaciaRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @AfterEach
    void limparDadosDeTeste() {
        movimentacaoFarmaciaRepository.deleteAll();
        transferenciaFarmaciaRepository.deleteAll();
        loteRepository.deleteAll();

        List<Medicamento> medicamentos = medicamentoRepository.findAll().stream()
                .filter(m -> m.getNome() != null && m.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        medicamentoRepository.deleteAll(medicamentos);

        List<Profissional> profissionais = profissionalRepository.findAll().stream()
                .filter(p -> p.getCpf() != null && p.getCpf().startsWith(PREFIXO_CPF_TESTE))
                .toList();
        profissionalRepository.deleteAll(profissionais);

        List<UnidadeDeSaude> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        unidadeDeSaudeRepository.deleteAll(unidades);
    }

    private UUID criarUnidadeSaudeUbs(String sufixo) throws Exception {
        String nomeFederal = PREFIXO_NOME_TESTE + "Federal " + sufixo;
        String nomeEstadual = PREFIXO_NOME_TESTE + "Estadual " + sufixo;
        String nomeMunicipal = PREFIXO_NOME_TESTE + "Municipal " + sufixo;
        String nomeUnidade = PREFIXO_NOME_TESTE + "UBS " + sufixo;
        String endereco = """
                "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  }""";

        mockMvc.perform(post(FEDERAL_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "nome": "%s", "tipo": "FEDERAL", %s, "email": "contato@saude.gov.br" }
                        """.formatted(nomeFederal, endereco)))
                .andExpect(status().isCreated());
        mockMvc.perform(post(ESTADUAL_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "nome": "%s", "tipo": "ESTADUAL", "administracaoSuperior": "%s", "estado": "SP", %s,
                          "email": "contato@saude.sp.gov.br" }
                        """.formatted(nomeEstadual, nomeFederal, endereco)))
                .andExpect(status().isCreated());
        mockMvc.perform(post(MUNICIPAL_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "nome": "%s", "tipo": "MUNICIPAL", "administracaoSuperior": "%s", "municipio": "São Paulo", %s,
                          "email": "contato@prefeitura.sp.gov.br" }
                        """.formatted(nomeMunicipal, nomeEstadual, endereco)))
                .andExpect(status().isCreated());
        mockMvc.perform(post(UNIDADE_SAUDE_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "nome": "%s", "tipo": "UBS", "administracaoSuperior": "%s", %s, "email": "contato@ubs.sp.gov.br" }
                        """.formatted(nomeUnidade, nomeMunicipal, endereco)))
                .andExpect(status().isCreated());

        return unidadeDeSaudeRepository.findByNome(nomeUnidade).orElseThrow().getUuid();
    }

    private UUID criarMedicamentoEBuscarUuid(String sufixo) throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Medicamento " + sufixo;
        mockMvc.perform(post(MEDICAMENTO_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "nome": "%s", "principioAtivo": "Losartana Potássica", "apresentacao": "Comprimido 50mg",
                          "codigo": "TRF-%s", "ativo": true }
                        """.formatted(nome, sufixo)))
                .andExpect(status().isCreated());

        return medicamentoRepository.findAll().stream()
                .filter(m -> nome.equals(m.getNome()))
                .findFirst()
                .orElseThrow()
                .getUuid();
    }

    private String criarProfissionalEBuscarMatricula(String sufixo) throws Exception {
        String cpf = PREFIXO_CPF_TESTE + sufixo;
        mockMvc.perform(post(PROFISSIONAL_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "cpf": "%s", "nome": "Farmacêutico %s",
                          "endereco": { "cep": "01310-100", "logradouro": "Avenida Paulista", "numeroLogradouro": "1000",
                                        "bairro": "Bela Vista", "cidade": "São Paulo", "estado": "SP" },
                          "telefones": ["011-2063-7185"], "email": "farmacia@saude.sp.gov.br" }
                        """.formatted(cpf, sufixo)))
                .andExpect(status().isCreated());

        return profissionalRepository.findByCpfAndAtivoTrue(cpf).orElseThrow().getMatricula();
    }

    private record Cenario(UUID medicamentoId, UUID loteOrigemId, UUID unidadeDestinoId, String matricula) {
    }

    /** Lote com saldo na UBS {@code sufixo}, mais uma segunda UBS ({@code sufixo + "D"}) como destino. */
    private Cenario criarCenario(String sufixo, int quantidade, String validade) throws Exception {
        UUID medicamentoId = criarMedicamentoEBuscarUuid(sufixo);
        UUID unidadeOrigemId = criarUnidadeSaudeUbs(sufixo);
        UUID unidadeDestinoId = criarUnidadeSaudeUbs(sufixo + "D");
        String matricula = criarProfissionalEBuscarMatricula(sufixo);
        UUID loteId = criarLote(medicamentoId, unidadeOrigemId, "T" + sufixo, validade, quantidade, matricula);
        return new Cenario(medicamentoId, loteId, unidadeDestinoId, matricula);
    }

    private UUID criarLote(UUID medicamentoId, UUID unidadeId, String numeroLote, String validade, int quantidade,
                           String matricula) throws Exception {
        mockMvc.perform(post(LOTE_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "medicamentoId": "%s", "unidadeId": "%s", "numeroLote": "%s", "validade": "%s",
                          "quantidade": %d, "profissionalMatricula": "%s" }
                        """.formatted(medicamentoId, unidadeId, numeroLote, validade, quantidade, matricula)))
                .andExpect(status().isCreated());
        return lotesDoMedicamentoNaUnidade(medicamentoId, unidadeId).get(0).getUuid();
    }

    private String corpoTransferencia(UUID loteOrigemId, UUID unidadeDestinoId, Integer quantidade, String matricula) {
        return """
                { "loteOrigemId": "%s", "unidadeDestinoId": "%s", "quantidade": %s, "profissionalMatricula": "%s",
                  "observacao": "Remanejamento para cobrir falta" }
                """.formatted(loteOrigemId, unidadeDestinoId, quantidade, matricula);
    }

    private int saldo(UUID loteId) {
        return loteRepository.findById(loteId).orElseThrow().getQuantidade();
    }

    private List<MovimentacaoFarmacia> extrato(UUID loteId) {
        return movimentacaoFarmaciaRepository.findByLote_UuidOrderByRegistradoEmAsc(loteId);
    }

    private List<Lote> lotesDoMedicamentoNaUnidade(UUID medicamentoId, UUID unidadeId) {
        return loteRepository.findByMedicamentoUuid(medicamentoId).stream()
                .filter(l -> l.getUnidade().getUuid().equals(unidadeId))
                .toList();
    }

    @Test
    void deveTransferirCriandoLoteDaMesmaRemessaNoDestino() throws Exception {
        Cenario c = criarCenario("01", 100, "2027-01-01");

        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoTransferencia(c.loteOrigemId(), c.unidadeDestinoId(), 30, c.matricula())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Transferência registrada com sucesso!"));

        assertThat(saldo(c.loteOrigemId())).isEqualTo(70);
        List<Lote> destinos = lotesDoMedicamentoNaUnidade(c.medicamentoId(), c.unidadeDestinoId());
        assertThat(destinos).hasSize(1);
        Lote destino = destinos.get(0);
        assertThat(destino.getQuantidade()).isEqualTo(30);
        assertThat(destino.getNumeroLote()).isEqualTo("T01");
        assertThat(destino.getValidade()).isEqualTo(LocalDate.of(2027, 1, 1));

        MovimentacaoFarmacia saida = extrato(c.loteOrigemId()).get(1);
        assertThat(saida.getTipo()).isEqualTo(TipoMovimentacaoFarmacia.TRANSFERENCIA_SAIDA);
        assertThat(saida.getQuantidade()).isEqualTo(-30);
        assertThat(saida.getSaldoApos()).isEqualTo(70);
        List<MovimentacaoFarmacia> extratoDestino = extrato(destino.getUuid());
        assertThat(extratoDestino).hasSize(1);
        MovimentacaoFarmacia entrada = extratoDestino.get(0);
        assertThat(entrada.getTipo()).isEqualTo(TipoMovimentacaoFarmacia.TRANSFERENCIA_ENTRADA);
        assertThat(entrada.getQuantidade()).isEqualTo(30);
        assertThat(entrada.getSaldoApos()).isEqualTo(30);
        assertThat(transferenciaFarmaciaRepository.count()).isEqualTo(1);
    }

    @Test
    void deveSomarNoLoteDaMesmaRemessaQueJaExisteNoDestino() throws Exception {
        Cenario c = criarCenario("02", 100, "2027-01-01");
        UUID destinoId = criarLote(c.medicamentoId(), c.unidadeDestinoId(), "T02", "2027-01-01", 5, c.matricula());

        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoTransferencia(c.loteOrigemId(), c.unidadeDestinoId(), 20, c.matricula())))
                .andExpect(status().isCreated());

        assertThat(lotesDoMedicamentoNaUnidade(c.medicamentoId(), c.unidadeDestinoId())).hasSize(1);
        assertThat(saldo(destinoId)).isEqualTo(25);
        assertThat(saldo(c.loteOrigemId())).isEqualTo(80);
        assertThat(extrato(destinoId)).extracting(MovimentacaoFarmacia::getTipo)
                .containsExactly(TipoMovimentacaoFarmacia.ENTRADA, TipoMovimentacaoFarmacia.TRANSFERENCIA_ENTRADA);
    }

    @Test
    void deveRecusarTransferenciaMaiorQueOSaldoSemCriarNada() throws Exception {
        Cenario c = criarCenario("03", 10, "2027-01-01");

        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoTransferencia(c.loteOrigemId(), c.unidadeDestinoId(), 50, c.matricula())))
                .andExpect(status().isUnprocessableEntity());

        assertThat(saldo(c.loteOrigemId())).isEqualTo(10);
        assertThat(extrato(c.loteOrigemId())).hasSize(1);
        assertThat(lotesDoMedicamentoNaUnidade(c.medicamentoId(), c.unidadeDestinoId())).isEmpty();
        assertThat(transferenciaFarmaciaRepository.count()).isZero();
    }

    @Test
    void deveRecusarTransferenciaDeLoteVencido() throws Exception {
        Cenario c = criarCenario("04", 10, "2020-01-01");

        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoTransferencia(c.loteOrigemId(), c.unidadeDestinoId(), 5, c.matricula())))
                .andExpect(status().isUnprocessableEntity());

        assertThat(saldo(c.loteOrigemId())).isEqualTo(10);
        assertThat(transferenciaFarmaciaRepository.count()).isZero();
    }

    @Test
    void deveRecusarTransferenciaParaAMesmaUnidade() throws Exception {
        Cenario c = criarCenario("05", 10, "2027-01-01");
        UUID unidadeOrigemId = loteRepository.findById(c.loteOrigemId()).orElseThrow().getUnidade().getUuid();

        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoTransferencia(c.loteOrigemId(), unidadeOrigemId, 5, c.matricula())))
                .andExpect(status().isBadRequest());

        assertThat(saldo(c.loteOrigemId())).isEqualTo(10);
        assertThat(transferenciaFarmaciaRepository.count()).isZero();
    }

    @Test
    void deveRetornarBadRequestSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Erro de Validação"));
    }

    @Test
    void deveRetornarNotFoundQuandoLoteNaoExiste() throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs("06");
        String matricula = criarProfissionalEBuscarMatricula("06");

        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoTransferencia(UUID.randomUUID(), unidadeId, 5, matricula)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundQuandoUnidadeDestinoNaoExiste() throws Exception {
        Cenario c = criarCenario("07", 10, "2027-01-01");

        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoTransferencia(c.loteOrigemId(), UUID.randomUUID(), 5, c.matricula())))
                .andExpect(status().isNotFound());
        assertThat(saldo(c.loteOrigemId())).isEqualTo(10);
    }

    @Test
    void deveListarBuscarPorIdELigarAoLivro() throws Exception {
        Cenario c = criarCenario("08", 40, "2027-01-01");
        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoTransferencia(c.loteOrigemId(), c.unidadeDestinoId(), 15, c.matricula())))
                .andExpect(status().isCreated());
        UUID uuid = transferenciaFarmaciaRepository.findAll().get(0).getUuid();

        mockMvc.perform(get(TRANSFERENCIA_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uuid").value(uuid.toString()));

        mockMvc.perform(get(TRANSFERENCIA_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(15))
                .andExpect(jsonPath("$.numeroLote").value("T08"))
                .andExpect(jsonPath("$.loteOrigemId").value(c.loteOrigemId().toString()))
                .andExpect(jsonPath("$.unidadeDestinoId").value(c.unidadeDestinoId().toString()))
                .andExpect(jsonPath("$.profissionalMatricula").value(c.matricula()))
                .andExpect(jsonPath("$.observacao").value("Remanejamento para cobrir falta"));

        mockMvc.perform(get(MOVIMENTACAO_URL + "lote/" + c.loteOrigemId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].tipo").value("TRANSFERENCIA_SAIDA"))
                .andExpect(jsonPath("$[1].transferenciaId").value(uuid.toString()));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(TRANSFERENCIA_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
