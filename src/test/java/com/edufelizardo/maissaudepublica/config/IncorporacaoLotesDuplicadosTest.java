package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.MovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoFarmaciaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.services.version1.MovimentacaoFarmaciaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Incorporação de lotes duplicados da mesma remessa (ADR-0060). O duplicado é gravado direto pelo
 * repositório, com o índice único removido antes — é o cenário de uma base criada antes da regra.
 * SEM {@code @Transactional} na classe, como os demais testes da Farmácia.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IncorporacaoLotesDuplicadosTest {

    private static final String LOTE_URL = "/api/v1/lote/";
    private static final String MEDICAMENTO_URL = "/api/v1/medicamento/";
    private static final String UNIDADE_SAUDE_URL = "/api/v1/unidade-saude/";
    private static final String FEDERAL_URL = "/api/v1/federal/";
    private static final String ESTADUAL_URL = "/api/v1/estadual/";
    private static final String MUNICIPAL_URL = "/api/v1/municipal/";
    private static final String PREFIXO_NOME_TESTE = "Incorporacao Teste - ";
    private static final LocalDate VALIDADE = LocalDate.of(2027, 1, 1);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncorporacaoLotesDuplicados incorporacao;

    @Autowired
    private MovimentacaoFarmaciaService movimentacaoFarmaciaService;

    @Autowired
    private MovimentacaoFarmaciaRepository movimentacaoFarmaciaRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void limparDadosDeTeste() {
        movimentacaoFarmaciaRepository.deleteAll();
        // O lote incorporado referencia o que ficou: desfaz a ligação antes de apagar.
        jdbcTemplate.update("update tb_lote set lote_incorporador_id = null where lote_incorporador_id is not null");
        loteRepository.deleteAll();

        List<Medicamento> medicamentos = medicamentoRepository.findAll().stream()
                .filter(m -> m.getNome() != null && m.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        medicamentoRepository.deleteAll(medicamentos);

        List<UnidadeDeSaude> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        unidadeDeSaudeRepository.deleteAll(unidades);

        // Se um teste falhar no meio, o índice volta mesmo assim.
        incorporacao.run(null);
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
                        { "nome": "%s", "principioAtivo": "Metformina", "apresentacao": "Comprimido 850mg",
                          "codigo": "INC-%s", "ativo": true }
                        """.formatted(nome, sufixo)))
                .andExpect(status().isCreated());

        return medicamentoRepository.findAll().stream()
                .filter(m -> nome.equals(m.getNome()))
                .findFirst()
                .orElseThrow()
                .getUuid();
    }

    private record Cenario(UUID medicamentoId, UUID unidadeId, UUID loteOriginalId) {
    }

    /** Lote criado pela API (saldo 40) e remoção do índice, para permitir gravar um duplicado. */
    private Cenario criarLoteOriginalSemIndice(String sufixo) throws Exception {
        UUID medicamentoId = criarMedicamentoEBuscarUuid(sufixo);
        UUID unidadeId = criarUnidadeSaudeUbs(sufixo);
        mockMvc.perform(post(LOTE_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "medicamentoId": "%s", "unidadeId": "%s", "numeroLote": "I%s", "validade": "%s", "quantidade": 40 }
                        """.formatted(medicamentoId, unidadeId, sufixo, VALIDADE)))
                .andExpect(status().isCreated());
        UUID loteId = loteRepository.findByMedicamentoUuid(medicamentoId).get(0).getUuid();

        jdbcTemplate.execute("DROP INDEX IF EXISTS " + IncorporacaoLotesDuplicados.INDICE_REMESSA_UNICA);
        return new Cenario(medicamentoId, unidadeId, loteId);
    }

    /** Grava um segundo lote da mesma remessa direto no banco, com entrada de 10 — como uma base antiga. */
    private UUID gravarDuplicado(Cenario c, String numeroLote) {
        Medicamento medicamento = medicamentoRepository.findById(c.medicamentoId()).orElseThrow();
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(c.unidadeId()).orElseThrow();
        Lote duplicado = loteRepository.save(new Lote(medicamento, unidade, numeroLote, VALIDADE, 0));
        movimentacaoFarmaciaService.lancar(duplicado, TipoMovimentacaoFarmacia.ENTRADA, 10, null, null, null, null);
        return duplicado.getUuid();
    }

    @Test
    void deveIncorporarDuplicadoAoLoteMaisAntigoPreservandoOsExtratos() throws Exception {
        Cenario c = criarLoteOriginalSemIndice("01");
        UUID duplicadoId = gravarDuplicado(c, "I01");

        incorporacao.run(null);

        assertThat(loteRepository.findById(c.loteOriginalId()).orElseThrow().getQuantidade()).isEqualTo(50);
        assertThat(loteRepository.findById(duplicadoId).orElseThrow().getQuantidade()).isZero();

        List<MovimentacaoFarmacia> extratoOriginal = movimentacaoFarmaciaRepository
                .findByLote_UuidOrderByRegistradoEmAsc(c.loteOriginalId());
        assertThat(extratoOriginal).extracting(MovimentacaoFarmacia::getTipo)
                .containsExactly(TipoMovimentacaoFarmacia.ENTRADA, TipoMovimentacaoFarmacia.INCORPORACAO_ENTRADA);
        List<MovimentacaoFarmacia> extratoDuplicado = movimentacaoFarmaciaRepository
                .findByLote_UuidOrderByRegistradoEmAsc(duplicadoId);
        assertThat(extratoDuplicado).extracting(MovimentacaoFarmacia::getTipo)
                .containsExactly(TipoMovimentacaoFarmacia.ENTRADA, TipoMovimentacaoFarmacia.INCORPORACAO_SAIDA);
        assertThat(extratoDuplicado.get(1).getSaldoApos()).isZero();

        mockMvc.perform(get(LOTE_URL + duplicadoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loteIncorporadorId").value(c.loteOriginalId().toString()));
        mockMvc.perform(get(LOTE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].uuid", not(hasItem(duplicadoId.toString()))))
                .andExpect(jsonPath("$[*].uuid", hasItem(c.loteOriginalId().toString())));

        Lote incorporado = loteRepository.findById(duplicadoId).orElseThrow();
        assertThatThrownBy(() -> movimentacaoFarmaciaService.lancar(incorporado, TipoMovimentacaoFarmacia.PERDA, -1,
                null, null, null, null))
                .isInstanceOf(ResourceUnprocessableEntityException.class);
    }

    @Test
    void deveRecriarOIndiceQueImpedeDuasRemessasAtivasIguais() throws Exception {
        Cenario c = criarLoteOriginalSemIndice("02");

        incorporacao.run(null);

        Integer indices = jdbcTemplate.queryForObject("select count(*) from pg_indexes where indexname = ?",
                Integer.class, IncorporacaoLotesDuplicados.INDICE_REMESSA_UNICA);
        assertThat(indices).isEqualTo(1);

        Medicamento medicamento = medicamentoRepository.findById(c.medicamentoId()).orElseThrow();
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(c.unidadeId()).orElseThrow();
        assertThatThrownBy(() -> loteRepository.saveAndFlush(new Lote(medicamento, unidade, "I02", VALIDADE, 0)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoFazNadaQuandoNaoHaDuplicados() throws Exception {
        Cenario c = criarLoteOriginalSemIndice("03");
        long lancamentosAntes = movimentacaoFarmaciaRepository.count();

        incorporacao.run(null);

        assertThat(movimentacaoFarmaciaRepository.count()).isEqualTo(lancamentosAntes);
        assertThat(loteRepository.findById(c.loteOriginalId()).orElseThrow().getQuantidade()).isEqualTo(40);
    }
}
