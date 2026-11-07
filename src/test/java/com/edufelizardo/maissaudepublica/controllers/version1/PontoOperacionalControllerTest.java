package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.PerfilOperacionalRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ponto operacional, fatia MO3 (ADR-0166): pontos com histórico, base, rota e localização da unidade móvel, e os limites de
 * quem é móvel. O local de realização do atendimento fica em {@link LocalRealizacaoAtendimentoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PontoOperacionalControllerTest {

    private static final String UNIDADE = "/api/v1/unidade-saude/id/";
    private static final String PREFIXO = "Ponto Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PerfilOperacionalRepository perfilRepository;

    private UnidadeDeSaude movel;
    private UnidadeDeSaude ubs;

    @BeforeEach
    void seed() throws Exception {
        limpar();
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS);
        movel = unidade("Móvel", TipoUnidadeDeSaude.POLICLINICA);
        mockMvc.perform(post(UNIDADE + movel.getUuid() + "/modelo-operacional/perfil").contentType(MediaType.APPLICATION_JSON)
                .content("{\"perfilId\": \"%s\"}".formatted(perfilRepository.findByCodigo("ATENCAO_MOVEL").orElseThrow().getUuid())))
                .andExpect(status().isOk());
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_posicao_unidade_movel where unidade_id = ?", u);
            jdbc.update("delete from tb_rota_ponto where rota_id in (select uuid from tb_rota_programada where unidade_id = ?)", u);
            jdbc.update("delete from tb_rota_dia where rota_id in (select uuid from tb_rota_programada where unidade_id = ?)", u);
            jdbc.update("delete from tb_rota_programada where unidade_id = ?", u);
            jdbc.update("delete from tb_ponto_operacional where unidade_id = ?", u);
            jdbc.update("delete from tb_unidade_perfil_operacional where unidade_id = ?", u);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private ResultActions incluirPonto(UUID unidade, String tipo, String nome) throws Exception {
        return mockMvc.perform(post(UNIDADE + unidade + "/ponto").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\": \"%s\", \"nome\": \"%s\"}".formatted(tipo, nome)));
    }

    private String ponto(UUID unidade, String tipo, String nome) throws Exception {
        String r = incluirPonto(unidade, tipo, nome).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(r, "$.pontos[?(@.nome == '" + nome + "')].uuid");
        return ids.get(0);
    }

    private ResultActions incluirRota(UUID unidade, String nome, String... pontos) throws Exception {
        return mockMvc.perform(post(UNIDADE + unidade + "/rota").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"%s\", \"dias\": [\"TUESDAY\", \"MONDAY\"], \"horaInicio\": \"08:00\", \"horaFim\": \"12:00\", \"pontoIds\": [\"%s\"]}"
                        .formatted(nome, String.join("\", \"", pontos))));
    }

    private ResultActions registrarPosicao(UUID unidade, String ponto) throws Exception {
        return mockMvc.perform(post(UNIDADE + unidade + "/posicao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"pontoId\": \"%s\"}".formatted(ponto)));
    }

    @Test
    void qualquerUnidadeDeAtendimentoTemPontoENivelDeGestaoNao() throws Exception {
        incluirPonto(ubs.getUuid(), "PONTO_FIXO", "Escola Rui Barbosa").andExpect(status().isOk())
                .andExpect(jsonPath("$.movel").value(false))
                .andExpect(jsonPath("$.pontos[0].nome").value("Escola Rui Barbosa"));
        UnidadeDeSaude regional = unidade("Regional", TipoUnidadeDeSaude.REGIONAL);
        incluirPonto(regional.getUuid(), "PONTO_FIXO", "Ponto").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void nomeRepetidoE409ENomeDeEncerradoVolta() throws Exception {
        String id = ponto(ubs.getUuid(), "PONTO_FIXO", "Praça");
        incluirPonto(ubs.getUuid(), "PONTO_PROGRAMADO", "praça").andExpect(status().isConflict());
        mockMvc.perform(post(UNIDADE + ubs.getUuid() + "/ponto/" + id + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Reforma\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pontos.length()").value(0))
                .andExpect(jsonPath("$.pontosEncerrados[0].motivoFim").value("Reforma"));
        mockMvc.perform(post(UNIDADE + ubs.getUuid() + "/ponto/" + id + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\": \"De novo\"}")).andExpect(status().isUnprocessableEntity());
        incluirPonto(ubs.getUuid(), "PONTO_FIXO", "Praça").andExpect(status().isOk());
    }

    @Test
    void latitudeSemLongitudeEInicioFuturoSao400() throws Exception {
        mockMvc.perform(post(UNIDADE + ubs.getUuid() + "/ponto").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\": \"PONTO_FIXO\", \"nome\": \"Praça\", \"latitude\": -23.5}")).andExpect(status().isBadRequest());
        mockMvc.perform(post(UNIDADE + ubs.getUuid() + "/ponto").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\": \"PONTO_FIXO\", \"nome\": \"Praça\", \"inicio\": \"%s\"}".formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ajustarOPontoMudaNomeEPosicao() throws Exception {
        String id = ponto(ubs.getUuid(), "PONTO_FIXO", "Praça");
        mockMvc.perform(patch(UNIDADE + ubs.getUuid() + "/ponto/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Praça central\", \"referencia\": \"Em frente à igreja\", \"latitude\": -23.5, \"longitude\": -46.6}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pontos[0].nome").value("Praça central"))
                .andExpect(jsonPath("$.pontos[0].latitude").value(-23.5));
    }

    @Test
    void baseEDaUnidadeMovelEUmaVigentePorUnidade() throws Exception {
        incluirPonto(ubs.getUuid(), "BASE", "Base").andExpect(status().isUnprocessableEntity());
        incluirPonto(movel.getUuid(), "BASE", "Base Norte").andExpect(status().isOk()).andExpect(jsonPath("$.movel").value(true));
        incluirPonto(movel.getUuid(), "BASE", "Base Sul").andExpect(status().isConflict());
    }

    @Test
    void rotaGuardaOsPontosEmOrdemEOsDias() throws Exception {
        String a = ponto(movel.getUuid(), "PONTO_PROGRAMADO", "Zebra");
        String b = ponto(movel.getUuid(), "PONTO_PROGRAMADO", "Abacate");

        incluirRota(movel.getUuid(), "Rota rural", a, b).andExpect(status().isOk())
                .andExpect(jsonPath("$.rotas[0].dias[0]").value("MONDAY"))
                .andExpect(jsonPath("$.rotas[0].pontos[0].nome").value("Zebra"))
                .andExpect(jsonPath("$.rotas[0].pontos[1].nome").value("Abacate"));
        incluirRota(movel.getUuid(), "rota RURAL", a).andExpect(status().isConflict());
    }

    @Test
    void rotaEDaUnidadeMovelComPontosValidos() throws Exception {
        String daUbs = ponto(ubs.getUuid(), "PONTO_FIXO", "Praça");
        incluirRota(ubs.getUuid(), "Rota", daUbs).andExpect(status().isUnprocessableEntity());

        String a = ponto(movel.getUuid(), "PONTO_PROGRAMADO", "A");
        incluirRota(movel.getUuid(), "Alheia", daUbs).andExpect(status().isUnprocessableEntity());
        incluirRota(movel.getUuid(), "Repetida", a, a).andExpect(status().isConflict());
        mockMvc.perform(post(UNIDADE + movel.getUuid() + "/rota").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Invertida\", \"dias\": [\"MONDAY\"], \"horaInicio\": \"12:00\", \"horaFim\": \"08:00\", \"pontoIds\": [\"%s\"]}".formatted(a)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(UNIDADE + movel.getUuid() + "/rota").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Sem dias\", \"dias\": [], \"pontoIds\": [\"%s\"]}".formatted(a)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pontoDeRotaVigenteNaoEncerraEDepoisDaRotaSim() throws Exception {
        String a = ponto(movel.getUuid(), "PONTO_PROGRAMADO", "A");
        String r = incluirRota(movel.getUuid(), "Rota rural", a).andReturn().getResponse().getContentAsString();
        String rota = JsonPath.read(r, "$.rotas[0].uuid");

        mockMvc.perform(post(UNIDADE + movel.getUuid() + "/ponto/" + a + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Fim\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Rota rural")));
        mockMvc.perform(post(UNIDADE + movel.getUuid() + "/rota/" + rota + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Fim\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rotasEncerradas[0].nome").value("Rota rural"));
        mockMvc.perform(post(UNIDADE + movel.getUuid() + "/ponto/" + a + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\": \"Fim\"}")).andExpect(status().isOk());
    }

    @Test
    void ajustarRotaTrocaOsPontosEEncerradaNaoMuda() throws Exception {
        String a = ponto(movel.getUuid(), "PONTO_PROGRAMADO", "A");
        String b = ponto(movel.getUuid(), "PONTO_PROGRAMADO", "B");
        String r = incluirRota(movel.getUuid(), "Rota rural", a, b).andReturn().getResponse().getContentAsString();
        String rota = JsonPath.read(r, "$.rotas[0].uuid");
        String corpo = "{\"nome\": \"Rota norte\", \"dias\": [\"FRIDAY\"], \"pontoIds\": [\"%s\", \"%s\"]}".formatted(b, a);

        mockMvc.perform(patch(UNIDADE + movel.getUuid() + "/rota/" + rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rotas[0].nome").value("Rota norte"))
                .andExpect(jsonPath("$.rotas[0].dias.length()").value(1))
                .andExpect(jsonPath("$.rotas[0].pontos[0].nome").value("B"));
        mockMvc.perform(post(UNIDADE + movel.getUuid() + "/rota/" + rota + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\": \"Fim\"}")).andExpect(status().isOk());
        mockMvc.perform(patch(UNIDADE + movel.getUuid() + "/rota/" + rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void localizacaoGuardaAsPassagens() throws Exception {
        String a = ponto(movel.getUuid(), "PONTO_PROGRAMADO", "A");
        String b = ponto(movel.getUuid(), "PONTO_PROGRAMADO", "B");

        registrarPosicao(movel.getUuid(), a).andExpect(status().isOk());
        registrarPosicao(movel.getUuid(), a).andExpect(status().isConflict());
        registrarPosicao(movel.getUuid(), b).andExpect(status().isOk())
                .andExpect(jsonPath("$.posicoes[0].pontoNome").value("B"))
                .andExpect(jsonPath("$.posicoes[1].pontoNome").value("A"));
    }

    @Test
    void localizacaoRecusaDataInvalidaEUnidadeNaoMovel() throws Exception {
        String daUbs = ponto(ubs.getUuid(), "PONTO_FIXO", "Praça");
        registrarPosicao(ubs.getUuid(), daUbs).andExpect(status().isUnprocessableEntity());

        String a = ponto(movel.getUuid(), "PONTO_PROGRAMADO", "A");
        String b = ponto(movel.getUuid(), "PONTO_PROGRAMADO", "B");
        registrarPosicao(movel.getUuid(), daUbs).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(UNIDADE + movel.getUuid() + "/posicao").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pontoId\": \"%s\", \"desde\": \"%s\"}".formatted(a, LocalDateTime.now().plusHours(1))))
                .andExpect(status().isBadRequest());
        registrarPosicao(movel.getUuid(), a).andExpect(status().isOk());
        mockMvc.perform(post(UNIDADE + movel.getUuid() + "/posicao").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pontoId\": \"%s\", \"desde\": \"%s\"}".formatted(b, LocalDateTime.now().minusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void perfilMovelNaoEncerraComBaseVigente() throws Exception {
        String base = ponto(movel.getUuid(), "BASE", "Base");
        String associacao = JsonPath.read(mockMvc.perform(get(UNIDADE + movel.getUuid() + "/modelo-operacional"))
                .andReturn().getResponse().getContentAsString(), "$.perfis[0].uuid");
        String url = UNIDADE + movel.getUuid() + "/modelo-operacional/perfil/" + associacao + "/encerramento";

        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Fim\"}")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(UNIDADE + movel.getUuid() + "/ponto/" + base + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\": \"Fim\"}")).andExpect(status().isOk());
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Fim\"}")).andExpect(status().isOk());
    }
}
