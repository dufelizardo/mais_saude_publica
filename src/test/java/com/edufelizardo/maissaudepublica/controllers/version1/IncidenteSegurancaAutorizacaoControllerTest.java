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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Segurança do paciente com login e autorização ligados (ADR-0203): o técnico de enfermagem e a recepção notificam e não veem os
 * incidentes; a notificação anônima não guarda quem notificou; o núcleo analisa só na sua unidade; o gestor vê o painel e não o incidente;
 * o administrador da plataforma não notifica.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class IncidenteSegurancaAutorizacaoControllerTest {

    private static final String PREFIXO = "SegAut Teste ";
    private static final String API = "/api/v1/qualidade/";
    private static final String TECNICO = "94955867401";
    private static final String RECEPCAO = "94955867402";
    private static final String NSP = "94955867403";
    private static final String NSP_OUTRA = "94955867404";
    private static final String GESTOR = "94955867405";
    private static final String ADMIN = "94955867406";
    private static final List<String> CPFS = List.of(TECNICO, RECEPCAO, NSP, NSP_OUTRA, GESTOR, ADMIN);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude hospital;
    private UnidadeDeSaude outra;

    @BeforeEach
    void seed() {
        limpar();
        hospital = unidade("Hospital");
        outra = unidade("Outra");
        atribuir(TECNICO, "TECNICO_DE_ENFERMAGEM", hospital);
        atribuir(RECEPCAO, "RECEPCAO", hospital);
        atribuir(NSP, "NUCLEO_SEGURANCA_PACIENTE", hospital);
        atribuir(NSP_OUTRA, "NUCLEO_SEGURANCA_PACIENTE", outra);
        atribuir(GESTOR, "GESTOR", hospital);
        atribuir(ADMIN, "ADMINISTRADOR_PLATAFORMA", hospital);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        IncidenteSegurancaControllerTest.limparDados(jdbc, PREFIXO);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
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

    private MvcResult como(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false))).andReturn();
    }

    private int status(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return como(cpf, r).getResponse().getStatus();
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    @Test
    void notificaQuemAtuaAnalisaONucleo() throws Exception {
        String corpo = "{\"unidadeId\": \"" + hospital.getUuid() + "\", \"ocorridoEm\": \"" + Instant.now().minus(Duration.ofHours(3))
                + "\", \"local\": \"Leito 4\", \"tipo\": \"QUEDA\", \"descricao\": \"Queda no banheiro.\"";
        assertThat(status(ADMIN, json(post(API + "incidente"), corpo + "}"))).isEqualTo(403);
        assertThat(status(RECEPCAO, json(post(API + "incidente"), corpo + "}"))).isEqualTo(201);
        MvcResult r = como(TECNICO, json(post(API + "incidente"), corpo + ", \"anonima\": true}"));
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        String anonimo = JsonPath.read(r.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.uuid");
        assertThat(jdbc.queryForObject("select notificado_por_cpf from tb_incidente_seguranca where uuid = ?", String.class, UUID.fromString(anonimo))).isNull();
        r = como(TECNICO, json(post(API + "incidente"), corpo + "}"));
        String identificado = JsonPath.read(r.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.uuid");

        // Quem notifica não vê a lista; o núcleo da unidade vê e o da outra, não.
        assertThat(status(TECNICO, get(API + "incidentes"))).isEqualTo(403);
        assertThat(status(GESTOR, get(API + "incidente/" + identificado))).isEqualTo(403);
        assertThat(status(NSP_OUTRA, get(API + "incidente/" + identificado))).isEqualTo(403);
        r = como(NSP, get(API + "incidente/" + identificado));
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        assertThat((String) JsonPath.read(r.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.notificadorNome")).isNotNull();
        r = como(NSP, get(API + "incidente/" + anonimo));
        assertThat((Object) JsonPath.read(r.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.notificadorNome")).isNull();
        r = como(NSP_OUTRA, get(API + "incidentes"));
        assertThat((List<String>) JsonPath.read(r.getResponse().getContentAsString(StandardCharsets.UTF_8), "$[*].uuid")).isEmpty();

        // A análise é do núcleo; o painel, também do gestor pelos indicadores.
        String classificacao = "{\"natureza\": \"EVENTO_ADVERSO\", \"dano\": \"LEVE\"}";
        assertThat(status(TECNICO, json(post(API + "incidente/" + identificado + "/classificacao"), classificacao))).isEqualTo(403);
        assertThat(status(NSP_OUTRA, json(post(API + "incidente/" + identificado + "/classificacao"), classificacao))).isEqualTo(403);
        assertThat(status(NSP, json(post(API + "incidente/" + identificado + "/classificacao"), classificacao))).isEqualTo(200);
        assertThat(status(GESTOR, get(API + "seguranca/painel"))).isEqualTo(200);
        assertThat(status(TECNICO, get(API + "seguranca/painel"))).isEqualTo(403);
        assertThat(mockMvc.perform(get(API + "incidentes")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
