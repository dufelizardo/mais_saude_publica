package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Patrimônio com login e autorização ligados (ADR-0215): o papel Patrimônio cadastra e envia na própria unidade, não na dos outros, e não
 * aceita pelo destino; o gestor do destino aceita; o responsável vê o bem em "Meus bens" e na ficha sem permissão de Patrimônio; outro
 * profissional recebe 403; sem login, 401.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class PatrimonioAutorizacaoControllerTest {

    private static final String PREFIXO = "PatrimonioAut Teste ";
    private static final String API = "/api/v1/patrimonio/";
    private static final String PATRIMONIO = "94888767401";
    private static final String GESTOR_DESTINO = "94888767402";
    private static final String RESPONSAVEL = "94888767403";
    private static final String OUTRO = "94888767404";
    private static final List<String> CPFS = List.of(PATRIMONIO, GESTOR_DESTINO, RESPONSAVEL, OUTRO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude ubs2;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS Centro");
        ubs2 = unidade("UBS Norte");
        profissional("PTA-RESP", RESPONSAVEL);
        profissional("PTA-DEST", GESTOR_DESTINO);
        atribuir(PATRIMONIO, "PATRIMONIO", ubs);
        atribuir(GESTOR_DESTINO, "GESTOR", ubs2);
        atribuir(RESPONSAVEL, "RECEPCAO", ubs);
        atribuir(OUTRO, "RECEPCAO", ubs);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        PatrimonioControllerTest.limparDados(jdbc, PREFIXO);
        jdbc.update("delete from tb_profissional where matricula like 'PTA-%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void profissional(String matricula, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        profissionalRepository.save(p);
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

    private MvcResult como(String cpf, MockHttpServletRequestBuilder r, String corpo) throws Exception {
        return mockMvc.perform(r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false))
                .contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn();
    }

    private int codigo(String cpf, MockHttpServletRequestBuilder r, String corpo) throws Exception {
        return como(cpf, r, corpo).getResponse().getStatus();
    }

    private static String corpo(MvcResult r) throws Exception {
        return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String bem(UnidadeDeSaude u) {
        return "{\"descricao\": \"Mesa de escritório\", \"classe\": \"MOBILIARIO\", \"unidadeId\": \"" + u.getUuid() + "\", \"origem\": \"DOACAO\", "
                + "\"dataAquisicao\": \"" + hoje + "\", \"responsavelMatricula\": \"PTA-RESP\"}";
    }

    @Test
    void escopoPorUnidadeEResponsavel() throws Exception {
        assertThat(codigo(PATRIMONIO, post(API + "bem"), bem(ubs2))).isEqualTo(403);
        assertThat(codigo(OUTRO, post(API + "bem"), bem(ubs))).isEqualTo(403);
        MvcResult r = como(PATRIMONIO, post(API + "bem"), bem(ubs));
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(201);
        String id = JsonPath.read(corpo(r), "$.bem.uuid");

        // Quem vê: o Patrimônio da unidade e o responsável; o outro, não.
        assertThat(codigo(PATRIMONIO, get(API + "bens"), "")).isEqualTo(200);
        assertThat(codigo(OUTRO, get(API + "bens"), "")).isEqualTo(403);
        assertThat(codigo(OUTRO, get(API + "bem/" + id), "")).isEqualTo(403);
        assertThat(codigo(RESPONSAVEL, get(API + "bem/" + id), "")).isEqualTo(200);
        r = como(RESPONSAVEL, get(API + "meus-bens"), "");
        assertThat((List<?>) JsonPath.read(corpo(r), "$[?(@.uuid == '" + id + "')]")).hasSize(1);
        assertThat((List<?>) JsonPath.read(corpo(como(OUTRO, get(API + "meus-bens"), "")), "$")).isEmpty();
        assertThat((List<?>) JsonPath.read(corpo(como(GESTOR_DESTINO, get(API + "bens"), "")), "$[?(@.uuid == '" + id + "')]")).isEmpty();

        // A transferência: a origem envia; só o destino aceita.
        r = como(PATRIMONIO, post(API + "bem/" + id + "/transferencia"), "{\"destinoId\": \"" + ubs2.getUuid() + "\", \"motivo\": \"Remanejamento.\"}");
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(201);
        String t = JsonPath.read(corpo(r), "$.bem.transferenciaPendente.uuid");
        String aceite = "{\"responsavelMatricula\": \"PTA-DEST\"}";
        assertThat(codigo(PATRIMONIO, post(API + "transferencia/" + t + "/aceite"), aceite)).isEqualTo(403);
        assertThat(codigo(GESTOR_DESTINO, post(API + "transferencia/" + t + "/cancelamento"), "")).isEqualTo(403);
        r = como(GESTOR_DESTINO, post(API + "transferencia/" + t + "/aceite"), aceite);
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(200);
        assertThat((String) JsonPath.read(corpo(r), "$.bem.unidadeId")).isEqualTo(ubs2.getUuid().toString());

        // Depois do aceite, o bem saiu da unidade do Patrimônio e da responsabilidade do anterior.
        assertThat(codigo(PATRIMONIO, get(API + "bem/" + id), "")).isEqualTo(403);
        assertThat(codigo(RESPONSAVEL, get(API + "bem/" + id), "")).isEqualTo(403);
        assertThat(mockMvc.perform(get(API + "meus-bens")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
