package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.MovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.StatusTransferenciaFarmacia;
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
 * Testes da transferência de estoque entre unidades em duas etapas (ADR-0059, ADR-0061). Mesmo padrão dos testes do livro
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

    private record Cenario(UUID medicamentoId, UUID loteOrigemId, UUID unidadeDestinoId, String matriculaEnvio,
                           String matriculaRecebimento) {
    }

    /**
     * Lote com saldo na UBS {@code sufixo}, uma segunda UBS ({@code sufixo + "D"}) como destino, e dois
     * profissionais — quem envia e quem confere no destino.
     */
    private Cenario criarCenario(String sufixo, int quantidade, String validade) throws Exception {
        UUID medicamentoId = criarMedicamentoEBuscarUuid(sufixo);
        UUID unidadeOrigemId = criarUnidadeSaudeUbs(sufixo);
        UUID unidadeDestinoId = criarUnidadeSaudeUbs(sufixo + "D");
        String matriculaEnvio = criarProfissionalEBuscarMatricula(sufixo);
        String matriculaRecebimento = criarProfissionalEBuscarMatricula(String.valueOf(Integer.parseInt(sufixo) + 50));
        UUID loteId = criarLote(medicamentoId, unidadeOrigemId, "T" + sufixo, validade, quantidade, matriculaEnvio);
        return new Cenario(medicamentoId, loteId, unidadeDestinoId, matriculaEnvio, matriculaRecebimento);
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

    private String corpoEnvio(UUID loteOrigemId, UUID unidadeDestinoId, Integer quantidade, String matricula) {
        return """
                { "loteOrigemId": "%s", "unidadeDestinoId": "%s", "quantidade": %s, "profissionalMatricula": "%s",
                  "observacao": "Remanejamento para cobrir falta" }
                """.formatted(loteOrigemId, unidadeDestinoId, quantidade, matricula);
    }

    private String corpoRecebimento(Integer quantidadeRecebida, String matricula, String motivo, String justificativa) {
        return """
                { "quantidadeRecebida": %s, "profissionalMatricula": "%s", "motivoDivergencia": %s,
                  "justificativaDivergencia": %s }
                """.formatted(quantidadeRecebida, matricula, json(motivo), json(justificativa));
    }

    private static String json(String valor) {
        return valor == null ? "null" : "\"" + valor + "\"";
    }

    /** Envia {@code quantidade} e devolve o id da transferência. */
    private UUID enviar(Cenario c, int quantidade) throws Exception {
        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEnvio(c.loteOrigemId(), c.unidadeDestinoId(), quantidade, c.matriculaEnvio())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Transferência enviada com sucesso!"));
        return transferenciaFarmaciaRepository.findAll().get(0).getUuid();
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

    private StatusTransferenciaFarmacia statusDaTransferencia(UUID transferenciaId) {
        return transferenciaFarmaciaRepository.findById(transferenciaId).orElseThrow().getStatus();
    }

    // ── Envio ──────────────────────────────────────────────────────────────────────────────────────

    @Test
    void deveEnviarDeixandoEmTransitoSemEntradaNoDestino() throws Exception {
        Cenario c = criarCenario("01", 100, "2027-01-01");

        UUID id = enviar(c, 30);

        assertThat(statusDaTransferencia(id)).isEqualTo(StatusTransferenciaFarmacia.EM_TRANSITO);
        assertThat(saldo(c.loteOrigemId())).isEqualTo(70);
        assertThat(extrato(c.loteOrigemId())).extracting(MovimentacaoFarmacia::getTipo)
                .containsExactly(TipoMovimentacaoFarmacia.ENTRADA, TipoMovimentacaoFarmacia.TRANSFERENCIA_SAIDA);
        assertThat(lotesDoMedicamentoNaUnidade(c.medicamentoId(), c.unidadeDestinoId())).isEmpty();
    }

    @Test
    void deveRecusarEnvioMaiorQueOSaldoSemGravarNada() throws Exception {
        Cenario c = criarCenario("02", 10, "2027-01-01");

        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEnvio(c.loteOrigemId(), c.unidadeDestinoId(), 50, c.matriculaEnvio())))
                .andExpect(status().isUnprocessableEntity());

        assertThat(saldo(c.loteOrigemId())).isEqualTo(10);
        assertThat(extrato(c.loteOrigemId())).hasSize(1);
        assertThat(transferenciaFarmaciaRepository.count()).isZero();
    }

    @Test
    void deveRecusarEnvioDeLoteVencido() throws Exception {
        Cenario c = criarCenario("03", 10, "2020-01-01");

        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEnvio(c.loteOrigemId(), c.unidadeDestinoId(), 5, c.matriculaEnvio())))
                .andExpect(status().isUnprocessableEntity());

        assertThat(saldo(c.loteOrigemId())).isEqualTo(10);
        assertThat(transferenciaFarmaciaRepository.count()).isZero();
    }

    @Test
    void deveRecusarEnvioParaAMesmaUnidade() throws Exception {
        Cenario c = criarCenario("04", 10, "2027-01-01");
        UUID unidadeOrigemId = loteRepository.findById(c.loteOrigemId()).orElseThrow().getUnidade().getUuid();

        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEnvio(c.loteOrigemId(), unidadeOrigemId, 5, c.matriculaEnvio())))
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
    void deveRetornarNotFoundQuandoLoteOuUnidadeNaoExistem() throws Exception {
        Cenario c = criarCenario("05", 10, "2027-01-01");

        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEnvio(UUID.randomUUID(), c.unidadeDestinoId(), 5, c.matriculaEnvio())))
                .andExpect(status().isNotFound());
        mockMvc.perform(post(TRANSFERENCIA_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEnvio(c.loteOrigemId(), UUID.randomUUID(), 5, c.matriculaEnvio())))
                .andExpect(status().isNotFound());
        assertThat(saldo(c.loteOrigemId())).isEqualTo(10);
    }

    // ── Recebimento ────────────────────────────────────────────────────────────────────────────────

    @Test
    void deveReceberCriandoLoteDaMesmaRemessaNoDestino() throws Exception {
        Cenario c = criarCenario("06", 100, "2027-01-01");
        UUID id = enviar(c, 30);

        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/recebimento").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRecebimento(30, c.matriculaRecebimento(), null, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Recebimento registrado com sucesso!"));

        assertThat(statusDaTransferencia(id)).isEqualTo(StatusTransferenciaFarmacia.RECEBIDA);
        List<Lote> destinos = lotesDoMedicamentoNaUnidade(c.medicamentoId(), c.unidadeDestinoId());
        assertThat(destinos).hasSize(1);
        Lote destino = destinos.get(0);
        assertThat(destino.getQuantidade()).isEqualTo(30);
        assertThat(destino.getNumeroLote()).isEqualTo("T06");
        assertThat(destino.getValidade()).isEqualTo(LocalDate.of(2027, 1, 1));

        List<MovimentacaoFarmacia> extratoDestino = extrato(destino.getUuid());
        assertThat(extratoDestino).hasSize(1);
        assertThat(extratoDestino.get(0).getTipo()).isEqualTo(TipoMovimentacaoFarmacia.TRANSFERENCIA_ENTRADA);
        assertThat(extratoDestino.get(0).getQuantidade()).isEqualTo(30);

        mockMvc.perform(get(TRANSFERENCIA_URL + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.quantidadeRecebida").value(30))
                .andExpect(jsonPath("$.quantidadeDivergente").value(0))
                .andExpect(jsonPath("$.loteDestinoId").value(destino.getUuid().toString()))
                .andExpect(jsonPath("$.profissionalMatricula").value(c.matriculaEnvio()))
                .andExpect(jsonPath("$.profissionalRecebimentoMatricula").value(c.matriculaRecebimento()))
                .andExpect(jsonPath("$.recebidoEm").isNotEmpty());
    }

    @Test
    void deveSomarNoLoteDaMesmaRemessaQueJaExisteNoDestino() throws Exception {
        Cenario c = criarCenario("07", 100, "2027-01-01");
        UUID destinoId = criarLote(c.medicamentoId(), c.unidadeDestinoId(), "T07", "2027-01-01", 5, c.matriculaEnvio());
        UUID id = enviar(c, 20);

        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/recebimento").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRecebimento(20, c.matriculaRecebimento(), null, null)))
                .andExpect(status().isOk());

        assertThat(lotesDoMedicamentoNaUnidade(c.medicamentoId(), c.unidadeDestinoId())).hasSize(1);
        assertThat(saldo(destinoId)).isEqualTo(25);
        assertThat(saldo(c.loteOrigemId())).isEqualTo(80);
    }

    @Test
    void deveReceberComDivergenciaRegistrandoMotivoEDiferenca() throws Exception {
        Cenario c = criarCenario("08", 100, "2027-01-01");
        UUID id = enviar(c, 30);

        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/recebimento").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRecebimento(27, c.matriculaRecebimento(), "AVARIA", "3 frascos quebrados na caixa")))
                .andExpect(status().isOk());

        assertThat(lotesDoMedicamentoNaUnidade(c.medicamentoId(), c.unidadeDestinoId()).get(0).getQuantidade())
                .isEqualTo(27);
        mockMvc.perform(get(TRANSFERENCIA_URL + id))
                .andExpect(jsonPath("$.status").value("RECEBIDA_COM_DIVERGENCIA"))
                .andExpect(jsonPath("$.quantidade").value(30))
                .andExpect(jsonPath("$.quantidadeRecebida").value(27))
                .andExpect(jsonPath("$.quantidadeDivergente").value(3))
                .andExpect(jsonPath("$.motivoDivergencia").value("AVARIA"))
                .andExpect(jsonPath("$.justificativaDivergencia").value("3 frascos quebrados na caixa"));
    }

    @Test
    void deveRegistrarDivergenciaTotalQuandoNadaChega() throws Exception {
        Cenario c = criarCenario("09", 100, "2027-01-01");
        UUID id = enviar(c, 10);

        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/recebimento").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRecebimento(0, c.matriculaRecebimento(), "EXTRAVIO", "Caixa não chegou")))
                .andExpect(status().isOk());

        assertThat(statusDaTransferencia(id)).isEqualTo(StatusTransferenciaFarmacia.RECEBIDA_COM_DIVERGENCIA);
        assertThat(lotesDoMedicamentoNaUnidade(c.medicamentoId(), c.unidadeDestinoId())).isEmpty();
        assertThat(saldo(c.loteOrigemId())).isEqualTo(90);
    }

    @Test
    void deveExigirMotivoEJustificativaNaDivergencia() throws Exception {
        Cenario c = criarCenario("10", 100, "2027-01-01");
        UUID id = enviar(c, 30);

        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/recebimento").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRecebimento(27, c.matriculaRecebimento(), null, null)))
                .andExpect(status().isBadRequest());

        assertThat(statusDaTransferencia(id)).isEqualTo(StatusTransferenciaFarmacia.EM_TRANSITO);
        assertThat(lotesDoMedicamentoNaUnidade(c.medicamentoId(), c.unidadeDestinoId())).isEmpty();
    }

    @Test
    void deveRecusarRecebimentoPorQuemEnviou() throws Exception {
        Cenario c = criarCenario("11", 100, "2027-01-01");
        UUID id = enviar(c, 30);

        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/recebimento").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRecebimento(30, c.matriculaEnvio(), null, null)))
                .andExpect(status().isUnprocessableEntity());

        assertThat(statusDaTransferencia(id)).isEqualTo(StatusTransferenciaFarmacia.EM_TRANSITO);
    }

    @Test
    void deveRecusarReceberMaisQueOEnviado() throws Exception {
        Cenario c = criarCenario("12", 100, "2027-01-01");
        UUID id = enviar(c, 30);

        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/recebimento").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRecebimento(31, c.matriculaRecebimento(), null, null)))
                .andExpect(status().isUnprocessableEntity());

        assertThat(statusDaTransferencia(id)).isEqualTo(StatusTransferenciaFarmacia.EM_TRANSITO);
    }

    @Test
    void deveRecusarReceberDuasVezes() throws Exception {
        Cenario c = criarCenario("13", 100, "2027-01-01");
        UUID id = enviar(c, 30);
        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/recebimento").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRecebimento(30, c.matriculaRecebimento(), null, null)))
                .andExpect(status().isOk());

        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/recebimento").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRecebimento(30, c.matriculaRecebimento(), null, null)))
                .andExpect(status().isUnprocessableEntity());

        assertThat(lotesDoMedicamentoNaUnidade(c.medicamentoId(), c.unidadeDestinoId()).get(0).getQuantidade())
                .isEqualTo(30);
    }

    // ── Cancelamento ───────────────────────────────────────────────────────────────────────────────

    @Test
    void deveCancelarEstornandoOSaldoAOrigem() throws Exception {
        Cenario c = criarCenario("14", 100, "2027-01-01");
        UUID id = enviar(c, 30);

        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/cancelamento").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "profissionalMatricula": "%s", "motivo": "Unidade de destino recebeu doação" }
                                """.formatted(c.matriculaEnvio())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transferência cancelada com sucesso!"));

        assertThat(statusDaTransferencia(id)).isEqualTo(StatusTransferenciaFarmacia.CANCELADA);
        assertThat(saldo(c.loteOrigemId())).isEqualTo(100);
        assertThat(extrato(c.loteOrigemId())).extracting(MovimentacaoFarmacia::getTipo)
                .containsExactly(TipoMovimentacaoFarmacia.ENTRADA, TipoMovimentacaoFarmacia.TRANSFERENCIA_SAIDA,
                        TipoMovimentacaoFarmacia.TRANSFERENCIA_ESTORNO);

        // Cancelada não pode mais ser recebida.
        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/recebimento").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRecebimento(30, c.matriculaRecebimento(), null, null)))
                .andExpect(status().isUnprocessableEntity());
        assertThat(lotesDoMedicamentoNaUnidade(c.medicamentoId(), c.unidadeDestinoId())).isEmpty();
    }

    @Test
    void deveExigirMotivoNoCancelamento() throws Exception {
        Cenario c = criarCenario("15", 100, "2027-01-01");
        UUID id = enviar(c, 30);

        mockMvc.perform(post(TRANSFERENCIA_URL + id + "/cancelamento").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "profissionalMatricula": "%s", "motivo": " " }
                                """.formatted(c.matriculaEnvio())))
                .andExpect(status().isBadRequest());

        assertThat(statusDaTransferencia(id)).isEqualTo(StatusTransferenciaFarmacia.EM_TRANSITO);
        assertThat(saldo(c.loteOrigemId())).isEqualTo(70);
    }

    // ── Consulta ───────────────────────────────────────────────────────────────────────────────────

    @Test
    void deveListarFiltrandoEmTransitoParaAUnidadeELigarAoLivro() throws Exception {
        Cenario c = criarCenario("16", 40, "2027-01-01");
        UUID id = enviar(c, 15);

        mockMvc.perform(get(TRANSFERENCIA_URL)
                        .param("status", "EM_TRANSITO")
                        .param("unidadeDestinoId", c.unidadeDestinoId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].uuid").value(id.toString()))
                .andExpect(jsonPath("$[0].status").value("EM_TRANSITO"))
                .andExpect(jsonPath("$[0].loteDestinoId").doesNotExist());
        mockMvc.perform(get(TRANSFERENCIA_URL).param("status", "RECEBIDA")
                        .param("unidadeDestinoId", c.unidadeDestinoId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get(MOVIMENTACAO_URL + "lote/" + c.loteOrigemId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].tipo").value("TRANSFERENCIA_SAIDA"))
                .andExpect(jsonPath("$[1].transferenciaId").value(id.toString()));
    }

    @Test
    void deveRetornarNotFoundParaTransferenciaInexistente() throws Exception {
        mockMvc.perform(get(TRANSFERENCIA_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
        mockMvc.perform(post(TRANSFERENCIA_URL + UUID.randomUUID() + "/recebimento").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRecebimento(1, "000000", null, null)))
                .andExpect(status().isNotFound());
    }
}
