package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.MovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerda;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoFarmaciaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do livro de movimentação da Farmácia (ADR-0057). Mesmo padrão dos testes de Lote e
 * Dispensacao: SEM {@code @Transactional} na classe, limpeza manual no {@code @AfterEach} — o livro
 * sai antes do Lote, que ele referencia.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovimentacaoFarmaciaControllerTest {

    private static final String MOVIMENTACAO_URL = "/api/v1/movimentacao-farmacia/";
    private static final String LOTE_URL = "/api/v1/lote/";
    private static final String MEDICAMENTO_URL = "/api/v1/medicamento/";
    private static final String UNIDADE_SAUDE_URL = "/api/v1/unidade-saude/";
    private static final String FEDERAL_URL = "/api/v1/federal/";
    private static final String ESTADUAL_URL = "/api/v1/estadual/";
    private static final String MUNICIPAL_URL = "/api/v1/municipal/";
    private static final String PROFISSIONAL_URL = "/api/v1/profissional/";
    private static final String PREFIXO_NOME_TESTE = "Movimentacao Teste - ";
    private static final String PREFIXO_CPF_TESTE = "77722211";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovimentacaoFarmaciaRepository movimentacaoFarmaciaRepository;

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
                        { "nome": "%s", "principioAtivo": "Dipirona Sódica", "apresentacao": "Comprimido 500mg",
                          "codigo": "COD-%s", "ativo": true }
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

    private record Cenario(UUID loteId, String matricula) {
    }

    private Cenario criarLoteComSaldo(String sufixo, int quantidade) throws Exception {
        UUID medicamentoId = criarMedicamentoEBuscarUuid(sufixo);
        UUID unidadeId = criarUnidadeSaudeUbs(sufixo);
        String matricula = criarProfissionalEBuscarMatricula(sufixo);

        mockMvc.perform(post(LOTE_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "medicamentoId": "%s", "unidadeId": "%s", "numeroLote": "M%s", "validade": "2027-01-01",
                          "quantidade": %d, "profissionalMatricula": "%s" }
                        """.formatted(medicamentoId, unidadeId, sufixo, quantidade, matricula)))
                .andExpect(status().isCreated());

        UUID loteId = loteRepository.findAll().stream()
                .filter(l -> l.getMedicamento().getUuid().equals(medicamentoId))
                .findFirst()
                .orElseThrow()
                .getUuid();
        return new Cenario(loteId, matricula);
    }

    private String corpoPerda(UUID loteId, String matricula, Integer quantidade, String motivo, String justificativa) {
        return """
                { "loteId": "%s", "tipo": "PERDA", "quantidade": %s, "motivoPerda": %s, "justificativa": %s,
                  "profissionalMatricula": "%s" }
                """.formatted(loteId, quantidade, json(motivo), json(justificativa), matricula);
    }

    private String corpoAjuste(UUID loteId, String matricula, Integer saldoContado, String justificativa) {
        return """
                { "loteId": "%s", "tipo": "AJUSTE_INVENTARIO", "saldoContado": %s, "justificativa": %s,
                  "profissionalMatricula": "%s" }
                """.formatted(loteId, saldoContado, json(justificativa), matricula);
    }

    private static String json(String valor) {
        return valor == null ? "null" : "\"" + valor + "\"";
    }

    private int saldo(UUID loteId) {
        return loteRepository.findById(loteId).orElseThrow().getQuantidade();
    }

    private List<MovimentacaoFarmacia> extrato(UUID loteId) {
        return movimentacaoFarmaciaRepository.findByLote_UuidOrderByRegistradoEmAsc(loteId);
    }

    @Test
    void deveRegistrarPerdaEDescontarDoLote() throws Exception {
        Cenario c = criarLoteComSaldo("01", 100);

        mockMvc.perform(post(MOVIMENTACAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPerda(c.loteId(), c.matricula(), 5, "VENCIMENTO", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Movimentação registrada com sucesso!"));

        assertThat(saldo(c.loteId())).isEqualTo(95);
        MovimentacaoFarmacia perda = extrato(c.loteId()).get(1);
        assertThat(perda.getTipo()).isEqualTo(TipoMovimentacaoFarmacia.PERDA);
        assertThat(perda.getQuantidade()).isEqualTo(-5);
        assertThat(perda.getSaldoApos()).isEqualTo(95);
        assertThat(perda.getMotivoPerda()).isEqualTo(MotivoPerda.VENCIMENTO);
    }

    @Test
    void deveRecusarPerdaMaiorQueOSaldo() throws Exception {
        Cenario c = criarLoteComSaldo("02", 10);

        mockMvc.perform(post(MOVIMENTACAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPerda(c.loteId(), c.matricula(), 50, "AVARIA", null)))
                .andExpect(status().isUnprocessableEntity());

        assertThat(saldo(c.loteId())).isEqualTo(10);
        assertThat(extrato(c.loteId())).hasSize(1);
    }

    @Test
    void deveExigirMotivoNaPerda() throws Exception {
        Cenario c = criarLoteComSaldo("03", 10);

        mockMvc.perform(post(MOVIMENTACAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPerda(c.loteId(), c.matricula(), 1, null, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveExigirJustificativaNaPerdaComMotivoOutro() throws Exception {
        Cenario c = criarLoteComSaldo("04", 10);

        mockMvc.perform(post(MOVIMENTACAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPerda(c.loteId(), c.matricula(), 1, "OUTRO", null)))
                .andExpect(status().isBadRequest());
        assertThat(saldo(c.loteId())).isEqualTo(10);
    }

    @Test
    void deveAjustarInventarioPeloSaldoContado() throws Exception {
        Cenario c = criarLoteComSaldo("05", 100);

        mockMvc.perform(post(MOVIMENTACAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAjuste(c.loteId(), c.matricula(), 97, "Contagem semestral")))
                .andExpect(status().isCreated());
        mockMvc.perform(post(MOVIMENTACAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAjuste(c.loteId(), c.matricula(), 110, "Caixa encontrada no depósito")))
                .andExpect(status().isCreated());

        assertThat(saldo(c.loteId())).isEqualTo(110);
        assertThat(extrato(c.loteId())).extracting(MovimentacaoFarmacia::getQuantidade).containsExactly(100, -3, 13);
    }

    @Test
    void deveExigirJustificativaNoAjuste() throws Exception {
        Cenario c = criarLoteComSaldo("06", 100);

        mockMvc.perform(post(MOVIMENTACAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAjuste(c.loteId(), c.matricula(), 90, "  ")))
                .andExpect(status().isBadRequest());
        assertThat(saldo(c.loteId())).isEqualTo(100);
    }

    @Test
    void deveRecusarLancamentoManualDeEntrada() throws Exception {
        Cenario c = criarLoteComSaldo("07", 10);

        mockMvc.perform(post(MOVIMENTACAO_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "loteId": "%s", "tipo": "ENTRADA", "quantidade": 50, "profissionalMatricula": "%s" }
                        """.formatted(c.loteId(), c.matricula())))
                .andExpect(status().isBadRequest());
        assertThat(saldo(c.loteId())).isEqualTo(10);
    }

    @Test
    void deveRetornarNotFoundQuandoLoteNaoExiste() throws Exception {
        String matricula = criarProfissionalEBuscarMatricula("08");

        mockMvc.perform(post(MOVIMENTACAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPerda(UUID.randomUUID(), matricula, 1, "AVARIA", null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarExtratoDoLoteEmOrdemComResponsavel() throws Exception {
        Cenario c = criarLoteComSaldo("09", 40);
        mockMvc.perform(post(MOVIMENTACAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPerda(c.loteId(), c.matricula(), 4, "EXTRAVIO", "Caixa não localizada")))
                .andExpect(status().isCreated());

        mockMvc.perform(get(MOVIMENTACAO_URL + "lote/" + c.loteId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("ENTRADA"))
                .andExpect(jsonPath("$[0].saldoApos").value(40))
                .andExpect(jsonPath("$[0].profissionalMatricula").value(c.matricula()))
                .andExpect(jsonPath("$[1].tipo").value("PERDA"))
                .andExpect(jsonPath("$[1].quantidade").value(-4))
                .andExpect(jsonPath("$[1].saldoApos").value(36))
                .andExpect(jsonPath("$[1].justificativa").value("Caixa não localizada"))
                .andExpect(jsonPath("$[1].registradoEm").isNotEmpty());
    }

    @Test
    void deveRetornarNotFoundNoExtratoDeLoteInexistente() throws Exception {
        mockMvc.perform(get(MOVIMENTACAO_URL + "lote/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        Cenario c = criarLoteComSaldo("10", 25);
        UUID uuid = extrato(c.loteId()).get(0).getUuid();

        mockMvc.perform(get(MOVIMENTACAO_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.loteId").value(c.loteId().toString()));
    }
}
