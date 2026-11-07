package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.TipoServicoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Os domínios consultando capacidades, fatia MO4 (ADR-0170): o tipo de leito, o encerramento da capacidade, o leito emergencial e a sala de vacina. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CapacidadesNosDominiosControllerTest {

    private static final String LEITO = "/api/v1/leito/";
    private static final String UNIDADE = "/api/v1/unidade-saude/id/";
    private static final String PREFIXO = "CapDominio Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private TipoServicoRepository tipoServicoRepository;

    private UnidadeDeSaude hospital;
    private Setor setor;

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
        s.setCodigo("ALA-MO4");
        s.setTipo(TipoSetor.ASSISTENCIAL);
        s.setAtivo(true);
        setor = setorRepository.save(s);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like ?)";
        jdbc.update("delete from tb_servico_unidade where unidade_id in " + unidades, PREFIXO + "%");
        jdbc.update("delete from tb_capacidade_emergencial where unidade_id in " + unidades, PREFIXO + "%");
        jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id in " + unidades + ")", PREFIXO + "%");
        jdbc.update("delete from tb_leito where unidade_id in " + unidades, PREFIXO + "%");
        jdbc.update("delete from tb_setor where unidade_id in " + unidades, PREFIXO + "%");
        CapacidadesDeTeste.limpar(jdbc, PREFIXO);
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private ResultActions criarLeito(String identificacao, String tipo) throws Exception {
        return mockMvc.perform(post(LEITO).contentType(MediaType.APPLICATION_JSON)
                .content("{\"unidadeId\": \"%s\", \"setorId\": \"%s\", \"identificacao\": \"%s\", \"tipo\": \"%s\", \"sexo\": \"MISTO\"}"
                        .formatted(hospital.getUuid(), setor.getUuid(), identificacao, tipo)));
    }

    private ResultActions capacidadeEmergencial(UUID unidade, String recurso) throws Exception {
        return mockMvc.perform(post("/api/v1/emergencias/capacidade").contentType(MediaType.APPLICATION_JSON)
                .content("{\"unidadeId\": \"%s\", \"recurso\": \"%s\", \"normal\": 10, \"emergencial\": 5}".formatted(unidade, recurso)));
    }

    @ParameterizedTest
    @CsvSource({"CLINICO,INTERNACAO_CLINICA", "PEDIATRICO,INTERNACAO_CLINICA", "ISOLAMENTO,INTERNACAO_CLINICA", "CIRURGICO,CIRURGIA",
            "OBSTETRICO,OBSTETRICIA", "UTI_ADULTO,UTI", "UTI_PEDIATRICA,UTI", "UTI_NEONATAL,UTI", "OBSERVACAO,OBSERVACAO"})
    void cadaTipoDeLeitoPedeASuaCapacidade(String tipo, String capacidade) throws Exception {
        criarLeito("L-" + tipo, tipo).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString(capacidade)));
        CapacidadesDeTeste.habilitar(jdbc, hospital.getUuid(), capacidade);
        criarLeito("L-" + tipo, tipo).andExpect(status().isCreated());
    }

    @Test
    void trocarOTipoDoLeitoPedeACapacidadeNovaEEditarSemTrocarNao() throws Exception {
        CapacidadesDeTeste.habilitar(jdbc, hospital.getUuid(), "INTERNACAO_CLINICA");
        String r = criarLeito("L-1", "CLINICO").andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(r, "$.uuid");
        String corpo = "{\"unidadeId\": \"%s\", \"setorId\": \"%s\", \"identificacao\": \"L-1\", \"tipo\": \"%s\", \"sexo\": \"MISTO\"}";

        mockMvc.perform(patch(LEITO + id).contentType(MediaType.APPLICATION_JSON).content(corpo.formatted(hospital.getUuid(), setor.getUuid(), "UTI_ADULTO")))
                .andExpect(status().isUnprocessableEntity());
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id = ?", hospital.getUuid());
        mockMvc.perform(patch(LEITO + id).contentType(MediaType.APPLICATION_JSON).content(corpo.formatted(hospital.getUuid(), setor.getUuid(), "CLINICO")))
                .andExpect(status().isOk());
    }

    @Test
    void capacidadeNaoEncerraComLeitoAtivo() throws Exception {
        CapacidadesDeTeste.habilitar(jdbc, hospital.getUuid(), "UTI");
        criarLeito("U-1", "UTI_ADULTO").andExpect(status().isCreated());
        String modelo = mockMvc.perform(get(UNIDADE + hospital.getUuid() + "/modelo-operacional")).andReturn().getResponse().getContentAsString();
        String associacao = JsonPath.read(modelo, "$.capacidades[0].uuid");
        String url = UNIDADE + hospital.getUuid() + "/modelo-operacional/capacidade/" + associacao + "/encerramento";

        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Fim\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("1 leito")));
        jdbc.update("update tb_leito set ativo = false where unidade_id = ?", hospital.getUuid());
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Fim\"}")).andExpect(status().isOk());
    }

    @Test
    void leitoEmergencialPedeCapacidadeDeInternacaoOuObservacao() throws Exception {
        capacidadeEmergencial(hospital.getUuid(), "LEITOS").andExpect(status().isUnprocessableEntity());
        capacidadeEmergencial(hospital.getUuid(), "CONSULTORIOS").andExpect(status().isCreated());
        CapacidadesDeTeste.habilitar(jdbc, hospital.getUuid(), "OBSERVACAO");
        capacidadeEmergencial(hospital.getUuid(), "LEITOS").andExpect(status().isCreated());
    }

    @Test
    void salaDeVacinaExigeACapacidade() throws Exception {
        var tipo = tipoServicoRepository.findByCodigo("SALA_DE_VACINA").orElseThrow();
        String corpo = "{\"tipoId\": \"%s\", \"nome\": \"Sala de vacina\"}".formatted(tipo.getUuid());
        String url = UNIDADE + hospital.getUuid() + "/servico";

        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isUnprocessableEntity());
        CapacidadesDeTeste.habilitar(jdbc, hospital.getUuid(), "SALA_VACINA");
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isOk());
    }

    @Test
    void cargaApontaACapacidadeDaSalaDeVacina() {
        Integer semCapacidade = jdbc.queryForObject("select count(*) from tb_tipo_servico t where t.codigo = 'SALA_DE_VACINA' and t.capacidade_id is null", Integer.class);
        assertThat(semCapacidade).isZero();
    }
}
