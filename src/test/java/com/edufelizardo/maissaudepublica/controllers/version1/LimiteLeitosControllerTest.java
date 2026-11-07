package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Leitos autorizados por capacidade (ADR-0171): sem número não há limite; com número, o cadastro, a reativação e a troca de tipo param nele. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LimiteLeitosControllerTest {

    private static final String LEITO = "/api/v1/leito/";
    private static final String PREFIXO = "Limite Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private CapacidadeAssistencialRepository capacidadeRepository;

    private UnidadeDeSaude hospital;
    private Setor setor;
    private String url;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "Hospital");
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        u.setAtivo(true);
        hospital = unidadeDeSaudeRepository.save(u);
        Setor s = new Setor();
        s.setUnidade(hospital);
        s.setNome("Ala");
        s.setCodigo("ALA-LIMITE");
        s.setTipo(TipoSetor.ASSISTENCIAL);
        s.setAtivo(true);
        setor = setorRepository.save(s);
        CapacidadesDeTeste.habilitar(jdbc, hospital.getUuid(), "UTI", "INTERNACAO_CLINICA");
        url = "/api/v1/unidade-saude/id/" + hospital.getUuid() + "/limites-de-leitos";
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like ?)";
        jdbc.update("delete from tb_limite_leitos where unidade_id in " + unidades, PREFIXO + "%");
        jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id in " + unidades + ")", PREFIXO + "%");
        jdbc.update("delete from tb_leito where unidade_id in " + unidades, PREFIXO + "%");
        jdbc.update("delete from tb_setor where unidade_id in " + unidades, PREFIXO + "%");
        CapacidadesDeTeste.limpar(jdbc, PREFIXO);
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private UUID capacidade(String codigo) {
        return capacidadeRepository.findByCodigo(codigo).orElseThrow().getUuid();
    }

    private ResultActions limite(String codigo, int quantidade) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content("{\"capacidadeId\": \"%s\", \"quantidade\": %d, \"motivo\": \"Habilitação\"}".formatted(capacidade(codigo), quantidade)));
    }

    private ResultActions criarLeito(String identificacao, String tipo, boolean ativo) throws Exception {
        return mockMvc.perform(post(LEITO).contentType(MediaType.APPLICATION_JSON).content(corpo(identificacao, tipo, ativo)));
    }

    private String corpo(String identificacao, String tipo, boolean ativo) {
        return "{\"unidadeId\": \"%s\", \"setorId\": \"%s\", \"identificacao\": \"%s\", \"tipo\": \"%s\", \"sexo\": \"MISTO\", \"ativo\": %s}"
                .formatted(hospital.getUuid(), setor.getUuid(), identificacao, tipo, ativo);
    }

    private String idDe(ResultActions r) throws Exception {
        return JsonPath.read(r.andReturn().getResponse().getContentAsString(), "$.uuid");
    }

    @Test
    void semNumeroNaoHaLimite() throws Exception {
        for (int i = 1; i <= 3; i++) {
            criarLeito("U-" + i, "UTI_ADULTO", true).andExpect(status().isCreated());
        }
        mockMvc.perform(get(url)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.capacidadeCodigo == 'UTI')].ativos").value(3))
                .andExpect(jsonPath("$[?(@.capacidadeCodigo == 'UTI')].autorizados").value(org.hamcrest.Matchers.contains((Object) null)));
    }

    @Test
    void cadastroParaNoLimite() throws Exception {
        limite("UTI", 2).andExpect(status().isOk());
        criarLeito("U-1", "UTI_ADULTO", true).andExpect(status().isCreated());
        criarLeito("U-2", "UTI_NEONATAL", true).andExpect(status().isCreated());
        criarLeito("U-3", "UTI_ADULTO", true).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("2 de 2")));
    }

    @Test
    void foraDeUsoNaoContaEReativarConfere() throws Exception {
        limite("UTI", 1).andExpect(status().isOk());
        criarLeito("U-1", "UTI_ADULTO", true).andExpect(status().isCreated());
        String reserva = idDe(criarLeito("U-2", "UTI_ADULTO", false).andExpect(status().isCreated()));

        mockMvc.perform(patch(LEITO + reserva).contentType(MediaType.APPLICATION_JSON).content(corpo("U-2", "UTI_ADULTO", true)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void trocarOTipoConfereSoQuandoMudaACapacidade() throws Exception {
        limite("UTI", 1).andExpect(status().isOk());
        String uti = idDe(criarLeito("U-1", "UTI_ADULTO", true).andExpect(status().isCreated()));
        String clinico = idDe(criarLeito("C-1", "CLINICO", true).andExpect(status().isCreated()));

        mockMvc.perform(patch(LEITO + clinico).contentType(MediaType.APPLICATION_JSON).content(corpo("C-1", "UTI_ADULTO", true)))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch(LEITO + uti).contentType(MediaType.APPLICATION_JSON).content(corpo("U-1", "UTI_PEDIATRICA", true)))
                .andExpect(status().isOk());
    }

    @Test
    void naoReduzAbaixoDosAtivosNemRepeteOValor() throws Exception {
        limite("UTI", 2).andExpect(status().isOk());
        criarLeito("U-1", "UTI_ADULTO", true).andExpect(status().isCreated());
        criarLeito("U-2", "UTI_ADULTO", true).andExpect(status().isCreated());

        limite("UTI", 1).andExpect(status().isUnprocessableEntity());
        limite("UTI", 2).andExpect(status().isUnprocessableEntity());
        limite("UTI", 5).andExpect(status().isOk());
    }

    @Test
    void historicoGuardaAsAlteracoesEOMaisRecenteVale() throws Exception {
        limite("UTI", 2).andExpect(status().isOk());
        limite("UTI", 4).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.capacidadeCodigo == 'UTI')].autorizados").value(4))
                .andExpect(jsonPath("$[?(@.capacidadeCodigo == 'UTI')].historico[0].quantidade").value(4))
                .andExpect(jsonPath("$[?(@.capacidadeCodigo == 'UTI')].historico[1].quantidade").value(2));
    }

    @Test
    void capacidadeAusenteOuSemLeitosE422() throws Exception {
        limite("CIRURGIA", 2).andExpect(status().isUnprocessableEntity());
        CapacidadesDeTeste.habilitar(jdbc, hospital.getUuid(), "ACOLHIMENTO");
        limite("ACOLHIMENTO", 2).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void motivoObrigatorioEQuantidadeNegativaSao400() throws Exception {
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content("{\"capacidadeId\": \"%s\", \"quantidade\": 3}".formatted(capacidade("UTI")))).andExpect(status().isBadRequest());
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content("{\"capacidadeId\": \"%s\", \"quantidade\": -1, \"motivo\": \"x\"}".formatted(capacidade("UTI")))).andExpect(status().isBadRequest());
    }
}
