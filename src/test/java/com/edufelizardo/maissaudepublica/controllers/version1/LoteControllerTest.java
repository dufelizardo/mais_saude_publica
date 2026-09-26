package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do LoteController (Farmácia — ver MAPA-DE-DOMINIOS.md #9, ADR-0050). Mesmo padrão de
 * engenharia de teste dos demais controllers com FK dupla por uuid direto (sem resolução por
 * identificador externo): SEM {@code @Transactional} na classe, limpeza manual via
 * {@code @AfterEach} incondicional pra Lote (único teste que os cria — mesmo raciocínio do
 * TriagemControllerTest, evita navegar associações LAZY fora de transação).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoteControllerTest {

    private static final String LOTE_URL = "/api/v1/lote/";
    private static final String MEDICAMENTO_URL = "/api/v1/medicamento/";
    private static final String UNIDADE_SAUDE_URL = "/api/v1/unidade-saude/";
    private static final String FEDERAL_URL = "/api/v1/federal/";
    private static final String ESTADUAL_URL = "/api/v1/estadual/";
    private static final String MUNICIPAL_URL = "/api/v1/municipal/";
    private static final String PREFIXO_NOME_TESTE = "Lote Teste - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @AfterEach
    void limparDadosDeTeste() {
        // Lote não tem campo de teste próprio para filtrar sem navegar associações LAZY fora de
        // transação (mesmo raciocínio do TriagemControllerTest). Este é o único teste que cria
        // Lote, então apagar tudo é seguro.
        loteRepository.deleteAll();

        List<Medicamento> medicamentos = medicamentoRepository.findAll().stream()
                .filter(m -> m.getNome() != null && m.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        medicamentoRepository.deleteAll(medicamentos);

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

        mockMvc.perform(post(FEDERAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "tipo": "FEDERAL",
                                  "endereco": {
                                    "cep": "70058-900",
                                    "logradouro": "Esplanada dos Ministérios",
                                    "numeroLogradouro": "Bloco G",
                                    "bairro": "Zona Cívico-Administrativa",
                                    "cidade": "Brasília",
                                    "estado": "DF"
                                  },
                                  "email": "contato@saude.gov.br"
                                }
                                """.formatted(nomeFederal)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(ESTADUAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "tipo": "ESTADUAL",
                                  "administracaoSuperior": "%s",
                                  "estado": "SP",
                                  "endereco": {
                                    "cep": "01037-000",
                                    "logradouro": "Rua Conselheiro Crispiniano",
                                    "numeroLogradouro": "20",
                                    "bairro": "Centro",
                                    "cidade": "São Paulo",
                                    "estado": "SP"
                                  },
                                  "email": "contato@saude.sp.gov.br"
                                }
                                """.formatted(nomeEstadual, nomeFederal)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(MUNICIPAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "tipo": "MUNICIPAL",
                                  "administracaoSuperior": "%s",
                                  "municipio": "São Paulo",
                                  "endereco": {
                                    "cep": "02012-040",
                                    "logradouro": "Rua Padre Marchetti",
                                    "numeroLogradouro": "557",
                                    "bairro": "Ipiranga",
                                    "cidade": "São Paulo",
                                    "estado": "SP"
                                  },
                                  "email": "contato@prefeitura.sp.gov.br"
                                }
                                """.formatted(nomeMunicipal, nomeEstadual)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(UNIDADE_SAUDE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "tipo": "UBS",
                                  "administracaoSuperior": "%s",
                                  "endereco": {
                                    "cep": "02012-040",
                                    "logradouro": "Rua Padre Marchetti",
                                    "numeroLogradouro": "557",
                                    "bairro": "Ipiranga",
                                    "cidade": "São Paulo",
                                    "estado": "SP"
                                  },
                                  "email": "contato@ubs.sp.gov.br"
                                }
                                """.formatted(nomeUnidade, nomeMunicipal)))
                .andExpect(status().isCreated());

        return unidadeDeSaudeRepository.findByNome(nomeUnidade).orElseThrow().getUuid();
    }

    private UUID criarMedicamentoEBuscarUuid(String sufixo) throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Medicamento " + sufixo;
        mockMvc.perform(post(MEDICAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "principioAtivo": "Dipirona Sódica",
                                  "apresentacao": "Comprimido 500mg",
                                  "codigo": "COD-%s",
                                  "ativo": true
                                }
                                """.formatted(nome, sufixo)))
                .andExpect(status().isCreated());

        return medicamentoRepository.findAll().stream()
                .filter(m -> nome.equals(m.getNome()))
                .findFirst()
                .orElseThrow()
                .getUuid();
    }

    private record LoteSeed(UUID medicamentoId, UUID unidadeId) {
    }

    private LoteSeed criarDependenciasDeLote(String sufixo) throws Exception {
        UUID medicamentoId = criarMedicamentoEBuscarUuid(sufixo);
        UUID unidadeId = criarUnidadeSaudeUbs(sufixo);
        return new LoteSeed(medicamentoId, unidadeId);
    }

    private String corpoLote(UUID medicamentoId, UUID unidadeId, String numeroLote, int quantidade) {
        return """
                {
                  "medicamentoId": "%s",
                  "unidadeId": "%s",
                  "numeroLote": "%s",
                  "validade": "2027-01-01",
                  "quantidade": %d
                }
                """.formatted(medicamentoId, unidadeId, numeroLote, quantidade);
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        LoteSeed seed = criarDependenciasDeLote("01");

        mockMvc.perform(post(LOTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLote(seed.medicamentoId(), seed.unidadeId(), "L001", 100)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Lote criado com sucesso!"));
    }

    @Test
    void deveRetornarBadRequestAoCriarSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post(LOTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Erro de Validação"));
    }

    @Test
    void deveRetornarNotFoundQuandoMedicamentoNaoExiste() throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs("02");

        mockMvc.perform(post(LOTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLote(UUID.randomUUID(), unidadeId, "L002", 50)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundQuandoUnidadeNaoExiste() throws Exception {
        UUID medicamentoId = criarMedicamentoEBuscarUuid("03");

        mockMvc.perform(post(LOTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLote(medicamentoId, UUID.randomUUID(), "L003", 50)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        LoteSeed seed = criarDependenciasDeLote("04");

        mockMvc.perform(post(LOTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLote(seed.medicamentoId(), seed.unidadeId(), "L004", 200)))
                .andExpect(status().isCreated());

        UUID uuid = loteRepository.findAll().stream()
                .filter(l -> l.getMedicamento().getUuid().equals(seed.medicamentoId()))
                .findFirst()
                .orElseThrow()
                .getUuid();

        mockMvc.perform(get(LOTE_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroLote").value("L004"))
                .andExpect(jsonPath("$.quantidade").value(200));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(LOTE_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarQuantidade() throws Exception {
        LoteSeed seed = criarDependenciasDeLote("05");

        mockMvc.perform(post(LOTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLote(seed.medicamentoId(), seed.unidadeId(), "L005", 100)))
                .andExpect(status().isCreated());

        UUID uuid = loteRepository.findAll().stream()
                .filter(l -> l.getMedicamento().getUuid().equals(seed.medicamentoId()))
                .findFirst()
                .orElseThrow()
                .getUuid();

        String bodyAtualizado = corpoLote(seed.medicamentoId(), seed.unidadeId(), "L005", 80);

        mockMvc.perform(patch(LOTE_URL + uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizado))
                .andExpect(status().isOk());

        Lote atualizado = loteRepository.findById(uuid).orElseThrow();
        assertThat(atualizado.getQuantidade()).isEqualTo(80);
    }
}
