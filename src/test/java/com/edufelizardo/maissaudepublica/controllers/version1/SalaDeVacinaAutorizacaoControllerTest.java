package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import com.edufelizardo.maissaudepublica.services.version1.JwtService;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sala de vacina com login e autorização ligados (ADR-0148): o técnico de enfermagem recebe o lote e lê a temperatura na
 * própria unidade, não em outra; a recepção não vê o estoque; a gestão lê o estoque e cadastra a câmara; o ajuste e a
 * avaliação do lote sob suspeita são da coordenação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class SalaDeVacinaAutorizacaoControllerTest {

    private static final String PREFIXO = "SalaVacAut Teste ";
    private static final String TECNICO = "94501867380";
    private static final String RECEPCAO = "94501867381";
    private static final String GESTOR = "94501867382";
    private static final String COORDENADOR = "94501867383";
    private static final List<String> CPFS = List.of(TECNICO, RECEPCAO, GESTOR, COORDENADOR);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("Outra");
        for (UnidadeDeSaude u : List.of(ubs, outra)) {
            jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                    + "from tb_capacidade_assistencial where codigo = 'SALA_VACINA'", u.getUuid(), LocalDate.now().minusDays(1));
        }
        atribuir(TECNICO, "TECNICO_DE_ENFERMAGEM", ubs);
        atribuir(RECEPCAO, "RECEPCAO", ubs);
        atribuir(GESTOR, "GESTOR", ubs);
        atribuir(COORDENADOR, "COORDENADOR_DE_ENFERMAGEM", ubs);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String lotes = "(select uuid from tb_lote_imunobiologico where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_movimentacao_imunobiologico where lote_id in " + lotes);
        jdbc.update("delete from tb_frasco_aberto where lote_id in " + lotes);
        jdbc.update("delete from tb_lote_imunobiologico where unidade_id in " + unidades);
        jdbc.update("delete from tb_leitura_temperatura where camara_id in (select uuid from tb_camara_refrigeracao where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_camara_refrigeracao where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void atribuir(String cpf, String papel, UnidadeDeSaude unidade) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(unidade);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private int enviar(String cpf, MockHttpServletRequestBuilder r, String corpo) throws Exception {
        return mockMvc.perform(como(cpf, r).contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse().getStatus();
    }

    private String entrada(UnidadeDeSaude u, String camaraId) {
        String vip = jdbc.queryForObject("select cast(uuid as varchar) from tb_imunobiologico where codigo = 'VIP'", String.class);
        return ("{\"unidadeId\": \"%s\", \"imunobiologicoId\": \"%s\", \"numeroLote\": \"AUT-1\", \"validade\": \"%s\", \"doses\": 10%s}")
                .formatted(u.getUuid(), vip, LocalDate.now().plusDays(60), camaraId == null ? "" : ", \"camaraId\": \"" + camaraId + "\"");
    }

    @Test
    void estoqueECadeiaDeFrioPorPapelEUnidade() throws Exception {
        String camara = "{\"unidadeId\": \"%s\", \"nome\": \"Câmara A\"}".formatted(ubs.getUuid());
        org.assertj.core.api.Assertions.assertThat(enviar(TECNICO, post("/api/v1/camara-refrigeracao"), camara)).isEqualTo(403);
        String c = mockMvc.perform(como(GESTOR, post("/api/v1/camara-refrigeracao")).contentType(MediaType.APPLICATION_JSON).content(camara))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String camaraId = JsonPath.read(c, "$.uuid");

        // Entrada: o técnico na própria unidade, não em outra; a recepção não.
        org.assertj.core.api.Assertions.assertThat(enviar(TECNICO, post("/api/v1/sala-de-vacina/entrada"), entrada(outra, null))).isEqualTo(403);
        org.assertj.core.api.Assertions.assertThat(enviar(RECEPCAO, post("/api/v1/sala-de-vacina/entrada"), entrada(ubs, null))).isEqualTo(403);
        String l = mockMvc.perform(como(TECNICO, post("/api/v1/sala-de-vacina/entrada")).contentType(MediaType.APPLICATION_JSON).content(entrada(ubs, camaraId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String lote = JsonPath.read(l, "$.uuid");

        // Leitura do estoque: quem vacina e a gestão; a recepção não; o técnico, só na própria unidade.
        mockMvc.perform(como(TECNICO, get("/api/v1/sala-de-vacina/estoque?unidadeId=" + ubs.getUuid()))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR, get("/api/v1/sala-de-vacina/estoque?unidadeId=" + ubs.getUuid()))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR, get("/api/v1/sala-de-vacina/cadeia-de-frio?unidadeId=" + ubs.getUuid()))).andExpect(status().isOk());
        mockMvc.perform(como(RECEPCAO, get("/api/v1/sala-de-vacina/estoque?unidadeId=" + ubs.getUuid()))).andExpect(status().isForbidden());
        mockMvc.perform(como(TECNICO, get("/api/v1/sala-de-vacina/estoque?unidadeId=" + outra.getUuid()))).andExpect(status().isForbidden());

        // A leitura de temperatura é de quem vacina; a excursão põe o lote sob suspeita; a avaliação é da coordenação.
        String excursao = "{\"temperaturaAtual\": 6, \"temperaturaMinima\": 4, \"temperaturaMaxima\": 11}";
        org.assertj.core.api.Assertions.assertThat(enviar(RECEPCAO, post("/api/v1/camara-refrigeracao/" + camaraId + "/leitura"), excursao)).isEqualTo(403);
        org.assertj.core.api.Assertions.assertThat(enviar(TECNICO, post("/api/v1/camara-refrigeracao/" + camaraId + "/leitura"), excursao)).isEqualTo(201);
        String avaliacao = "{\"decisao\": \"LIBERAR\", \"parecer\": \"Dentro da estabilidade do fabricante.\"}";
        org.assertj.core.api.Assertions.assertThat(enviar(TECNICO, post("/api/v1/lote-imunobiologico/" + lote + "/avaliacao"), avaliacao)).isEqualTo(403);
        org.assertj.core.api.Assertions.assertThat(enviar(COORDENADOR, post("/api/v1/lote-imunobiologico/" + lote + "/avaliacao"), avaliacao)).isEqualTo(200);

        // Perda física, de quem vacina; ajuste, da coordenação.
        org.assertj.core.api.Assertions.assertThat(enviar(TECNICO, post("/api/v1/lote-imunobiologico/" + lote + "/perda"), "{\"doses\": 1, \"motivo\": \"QUEBRA\"}"))
                .isEqualTo(200);
        String ajuste = "{\"saldo\": 8, \"justificativa\": \"Contagem\"}";
        org.assertj.core.api.Assertions.assertThat(enviar(TECNICO, post("/api/v1/lote-imunobiologico/" + lote + "/ajuste"), ajuste)).isEqualTo(403);
        org.assertj.core.api.Assertions.assertThat(enviar(COORDENADOR, post("/api/v1/lote-imunobiologico/" + lote + "/ajuste"), ajuste)).isEqualTo(200);
    }
}
