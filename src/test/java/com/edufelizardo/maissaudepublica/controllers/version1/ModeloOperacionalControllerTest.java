package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.PerfilOperacionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.services.version1.ModeloOperacionalService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Modelo operacional, fatia MO1 (ADR-0117): carga inicial dos catálogos, edição do catálogo, perfis e capacidades da unidade
 * como histórico, a coerência entre capacidade e perfil, os níveis de gestão de fora e os perfis na rede.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ModeloOperacionalControllerTest {

    private static final String UNIDADE = "/api/v1/unidade-saude/id/";
    private static final String PERFIL = "/api/v1/perfil-operacional/";
    private static final String CAPACIDADE = "/api/v1/capacidade-assistencial/";
    private static final String PREFIXO = "ModeloOp Teste ";
    private static final String CODIGO = "TESTE_MO_";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PerfilOperacionalRepository perfilRepository;

    @Autowired
    private CapacidadeAssistencialRepository capacidadeRepository;

    @Autowired
    private ModeloOperacionalService service;

    private UnidadeDeSaude caps;

    @BeforeEach
    void seed() {
        limpar();
        caps = unidade("CAPS Centro", TipoUnidadeDeSaude.CAPS);
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id = ?", u);
            jdbc.update("delete from tb_unidade_perfil_operacional where unidade_id = ?", u);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
        jdbc.update("delete from tb_capacidade_assistencial where codigo like ?", CODIGO + "%");
        jdbc.update("delete from tb_perfil_operacional where codigo like ?", CODIGO + "%");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private UUID perfil(String codigo) {
        return perfilRepository.findByCodigo(codigo).orElseThrow().getUuid();
    }

    private UUID capacidade(String codigo) {
        return capacidadeRepository.findByCodigo(codigo).orElseThrow().getUuid();
    }

    private ResultActions incluirPerfil(UUID unidade, UUID perfil, String inicio) throws Exception {
        return mockMvc.perform(post(UNIDADE + unidade + "/modelo-operacional/perfil").contentType(MediaType.APPLICATION_JSON)
                .content("{\"perfilId\": \"%s\", \"inicio\": %s}".formatted(perfil, inicio == null ? "null" : "\"" + inicio + "\"")));
    }

    private ResultActions incluirCapacidade(UUID unidade, UUID capacidade) throws Exception {
        return mockMvc.perform(post(UNIDADE + unidade + "/modelo-operacional/capacidade").contentType(MediaType.APPLICATION_JSON)
                .content("{\"capacidadeId\": \"%s\", \"observacao\": \"Portaria de habilitação 123/2026\"}".formatted(capacidade)));
    }

    private ResultActions encerrar(UUID unidade, String tipo, String associacao, String motivo) throws Exception {
        return mockMvc.perform(post(UNIDADE + unidade + "/modelo-operacional/" + tipo + "/" + associacao + "/encerramento")
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"%s\"}".formatted(motivo)));
    }

    private static String associacao(ResultActions r, String lista, String codigo) throws Exception {
        String corpo = r.andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(corpo, "$." + lista + "[?(@.codigo == '" + codigo + "')].uuid");
        return ids.get(0);
    }

    @Test
    void cargaInicialTrazOsDezPerfisEAsCapacidadesAgrupadas() throws Exception {
        mockMvc.perform(get(PERFIL)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", hasItems("ATENCAO_PRIMARIA", "URGENCIA", "INTERNACAO", "ATENCAO_ESPECIALIZADA",
                        "ATENCAO_PSICOSSOCIAL", "DIAGNOSTICO", "ASSISTENCIA_FARMACEUTICA", "VIGILANCIA", "REABILITACAO", "ATENCAO_MOVEL")))
                .andExpect(jsonPath("$[?(@.codigo == 'ATENCAO_PSICOSSOCIAL')].capacidades", hasItem(7)));
        mockMvc.perform(get(CAPACIDADE).param("perfilId", perfil("ATENCAO_PSICOSSOCIAL").toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", containsInAnyOrder("CAPS_I", "CAPS_II", "CAPS_III", "CAPS_INFANTOJUVENIL", "CAPS_AD",
                        "CAPS_AD_III", "ACOLHIMENTO_NOTURNO")));
        mockMvc.perform(get(CAPACIDADE + capacidade("ACOLHIMENTO"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.perfilId").doesNotExist());
    }

    @Test
    void catalogoComCodigoUnicoEPerfilInativoForaDeCapacidadeNova() throws Exception {
        String corpo = mockMvc.perform(post(PERFIL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"teste_mo_hospital_dia\", \"nome\": \"Hospital-dia\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("TESTE_MO_HOSPITAL_DIA"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(corpo, "$.uuid");
        mockMvc.perform(post(PERFIL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\": \"TESTE_MO_HOSPITAL_DIA\", \"nome\": \"Repetido\"}")).andExpect(status().isConflict());
        mockMvc.perform(post(PERFIL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\": \"TESTE MO\", \"nome\": \"Com espaço\"}")).andExpect(status().isBadRequest());

        mockMvc.perform(post(CAPACIDADE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"TESTE_MO_CAP\", \"nome\": \"Cuidado-dia\", \"perfilId\": \"%s\"}".formatted(id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfilCodigo").value("TESTE_MO_HOSPITAL_DIA"));
        mockMvc.perform(patch(PERFIL + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"TESTE_MO_HOSPITAL_DIA\", \"nome\": \"Hospital-dia\", \"ativo\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.capacidades").value(1));
        mockMvc.perform(post(CAPACIDADE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"TESTE_MO_CAP2\", \"nome\": \"Outra\", \"perfilId\": \"%s\"}".formatted(id)))
                .andExpect(status().isUnprocessableEntity());
        incluirPerfil(caps.getUuid(), UUID.fromString(id), null).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void perfisECapacidadesDaUnidadeComoHistoricoECoerenciaDoGrupo() throws Exception {
        UUID psicossocial = perfil("ATENCAO_PSICOSSOCIAL");
        String perfilCaps = associacao(incluirPerfil(caps.getUuid(), psicossocial, LocalDate.now().minusMonths(6).toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfis[0].codigo").value("ATENCAO_PSICOSSOCIAL")), "perfis", "ATENCAO_PSICOSSOCIAL");
        incluirPerfil(caps.getUuid(), psicossocial, null).andExpect(status().isConflict());
        incluirPerfil(caps.getUuid(), perfil("URGENCIA"), LocalDate.now().plusDays(1).toString()).andExpect(status().isBadRequest());

        // Capacidade do grupo exige o perfil; a transversal, não.
        String capsAd = associacao(incluirCapacidade(caps.getUuid(), capacidade("CAPS_AD")).andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidades[0].perfilNome").value("Atenção psicossocial"))
                .andExpect(jsonPath("$.capacidades[0].observacao").value("Portaria de habilitação 123/2026")), "capacidades", "CAPS_AD");
        incluirCapacidade(caps.getUuid(), capacidade("CAPS_AD")).andExpect(status().isConflict());
        incluirCapacidade(caps.getUuid(), capacidade("UTI")).andExpect(status().isUnprocessableEntity());
        incluirCapacidade(caps.getUuid(), capacidade("ACOLHIMENTO")).andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidades.length()").value(2));
        assertThat(service.unidadeTemCapacidade(caps.getUuid(), "CAPS_AD")).isTrue();
        assertThat(service.unidadeTemCapacidade(caps.getUuid(), "CAPS_III")).isFalse();

        // O perfil de uma capacidade vigente não muda.
        UUID capsAdId = capacidade("CAPS_AD");
        mockMvc.perform(patch(CAPACIDADE + capsAdId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"CAPS_AD\", \"nome\": \"CAPS álcool e drogas (CAPS AD)\", \"perfilId\": \"%s\"}"
                                .formatted(perfil("URGENCIA"))))
                .andExpect(status().isUnprocessableEntity());

        // O perfil só encerra depois das capacidades do grupo.
        encerrar(caps.getUuid(), "perfil", perfilCaps, "Fechamento do serviço").andExpect(status().isUnprocessableEntity());
        encerrar(caps.getUuid(), "capacidade", capsAd, "Reclassificado como CAPS AD III").andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidadesEncerradas[0].codigo").value("CAPS_AD"))
                .andExpect(jsonPath("$.capacidadesEncerradas[0].motivoFim").value("Reclassificado como CAPS AD III"));
        assertThat(service.unidadeTemCapacidade(caps.getUuid(), "CAPS_AD")).isFalse();
        encerrar(caps.getUuid(), "capacidade", capsAd, "De novo").andExpect(status().isUnprocessableEntity());
        encerrar(caps.getUuid(), "perfil", perfilCaps, "Fechamento do serviço").andExpect(status().isOk())
                .andExpect(jsonPath("$.perfis.length()").value(0))
                .andExpect(jsonPath("$.perfisEncerrados[0].codigo").value("ATENCAO_PSICOSSOCIAL"));

        // Volta com um novo registro: o encerrado fica no histórico.
        incluirPerfil(caps.getUuid(), psicossocial, null).andExpect(status().isOk());
        mockMvc.perform(get(UNIDADE + caps.getUuid() + "/modelo-operacional")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelDeGestao").value(false))
                .andExpect(jsonPath("$.perfis.length()").value(1))
                .andExpect(jsonPath("$.perfisEncerrados.length()").value(1))
                .andExpect(jsonPath("$.capacidades[*].codigo", not(hasItem("CAPS_AD"))));

        // A rede traz os perfis vigentes de cada unidade.
        mockMvc.perform(get("/api/v1/unidade-saude/rede")).andExpect(status().isOk())
                .andExpect(jsonPath("$.rede[?(@.uuid == '%s')].perfis[0]".formatted(caps.getUuid()), hasItem("ATENCAO_PSICOSSOCIAL")));
    }

    @Test
    void nivelDeGestaoNaoTemPerfilEAssociacaoDeOutraUnidadeNaoEncerra() throws Exception {
        UnidadeDeSaude municipio = unidade("Município", TipoUnidadeDeSaude.MUNICIPAL);
        incluirPerfil(municipio.getUuid(), perfil("ATENCAO_PRIMARIA"), null).andExpect(status().isUnprocessableEntity());
        incluirCapacidade(municipio.getUuid(), capacidade("ACOLHIMENTO")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(UNIDADE + municipio.getUuid() + "/modelo-operacional")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelDeGestao").value(true));

        String perfilCaps = associacao(incluirPerfil(caps.getUuid(), perfil("ATENCAO_PSICOSSOCIAL"), null), "perfis", "ATENCAO_PSICOSSOCIAL");
        UnidadeDeSaude outra = unidade("CAPS Norte", TipoUnidadeDeSaude.CAPS);
        encerrar(outra.getUuid(), "perfil", perfilCaps, "Engano").andExpect(status().isNotFound());
        mockMvc.perform(post(UNIDADE + caps.getUuid() + "/modelo-operacional/perfil/" + perfilCaps + "/encerramento")
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"\"}")).andExpect(status().isBadRequest());
    }
}
