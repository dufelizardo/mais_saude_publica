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
 * Financeiro com login e autorização ligados (ADR-0218): só o papel Financeiro registra a dotação; o gestor e o setor de compras consultam;
 * compras vê os empenhos para a ordem; outro profissional recebe 403; sem login, 401.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class FinanceiroAutorizacaoControllerTest {

    private static final String PREFIXO = "FinanceiroAut Teste ";
    private static final String API = "/api/v1/financeiro/";
    private static final String FINANCEIRO = "94844767401";
    private static final String GESTOR = "94844767402";
    private static final String COMPRAS = "94844767403";
    private static final String OUTRO = "94844767404";
    private static final List<String> CPFS = List.of(FINANCEIRO, GESTOR, COMPRAS, OUTRO);

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
        atribuir(FINANCEIRO, "FINANCEIRO", ubs);
        atribuir(GESTOR, "GESTOR", ubs);
        atribuir(COMPRAS, "COMPRAS", ubs);
        atribuir(OUTRO, "RECEPCAO", ubs);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        ComprasControllerTest.limparDados(jdbc, PREFIXO);
        jdbc.update("delete from tb_profissional where matricula like 'FNA-%'");
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

    @Test
    void financeiroRegistraOsOutrosConsultam() throws Exception {
        int ano = hoje.getYear();
        String dotacao = FinanceiroControllerTest.dotacao(ano, "10.302.0011.2050", "3.3.90.39", "5000", PREFIXO);
        assertThat(codigo(GESTOR, post(API + "dotacao"), dotacao)).isEqualTo(403);
        assertThat(codigo(COMPRAS, post(API + "dotacao"), dotacao)).isEqualTo(403);
        MvcResult r = como(FINANCEIRO, post(API + "dotacao"), dotacao);
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(201);
        assertThat(codigo(GESTOR, get(API + "dotacoes"), "")).isEqualTo(200);
        assertThat(codigo(COMPRAS, get(API + "empenhos"), "")).isEqualTo(200);
        assertThat(codigo(OUTRO, get(API + "dotacoes"), "")).isEqualTo(403);
        assertThat(codigo(OUTRO, get(API + "empenhos-disponiveis?instrumentoId=" + ubs.getUuid()), "")).isEqualTo(403);
        assertThat(codigo(COMPRAS, get(API + "empenhos-disponiveis?instrumentoId=" + ubs.getUuid()), "")).isEqualTo(404);
        assertThat(mockMvc.perform(get(API + "dotacoes")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
