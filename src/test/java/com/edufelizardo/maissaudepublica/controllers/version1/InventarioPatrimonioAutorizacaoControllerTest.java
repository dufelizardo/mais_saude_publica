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
 * Inventário e baixa com login e autorização ligados (ADR-0217): a comissão confere só com o login e não fecha; quem não é da comissão
 * não vê nem confere; o Patrimônio pede a baixa e não a confirma; o gestor confirma a dos outros e não a própria; o painel é de quem
 * consulta o Patrimônio; sem login, 401.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class InventarioPatrimonioAutorizacaoControllerTest {

    private static final String PREFIXO = "InventarioAut Teste ";
    private static final String API = "/api/v1/patrimonio/";
    private static final String PATRIMONIO = "94855767401";
    private static final String GESTOR = "94855767402";
    private static final String MEMBRO = "94855767403";
    private static final String OUTRO = "94855767404";
    private static final List<String> CPFS = List.of(PATRIMONIO, GESTOR, MEMBRO, OUTRO);

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
        profissional("IVA-MEMBRO", MEMBRO);
        profissional("IVA-M2", "94855767405");
        profissional("IVA-M3", "94855767406");
        atribuir(PATRIMONIO, "PATRIMONIO", ubs);
        atribuir(GESTOR, "GESTOR", ubs);
        atribuir(MEMBRO, "RECEPCAO", ubs);
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
        jdbc.update("delete from tb_profissional where matricula like 'IVA-%'");
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

    private String bem(String quem, String valor) throws Exception {
        MvcResult r = como(quem, post(API + "bem"), "{\"descricao\": \"Ultrassom portátil\", \"classe\": \"EQUIPAMENTO_MEDICO_HOSPITALAR\", \"unidadeId\": \""
                + ubs.getUuid() + "\", \"origem\": \"DOACAO\", \"valorAquisicao\": " + valor + ", \"dataAquisicao\": \"" + hoje
                + "\", \"responsavelMatricula\": \"IVA-M2\"}");
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(201);
        return JsonPath.read(corpo(r), "$.bem.uuid");
    }

    @Test
    void comissaoConfereEGestaoConfirmaABaixa() throws Exception {
        String bem = bem(PATRIMONIO, "20000");

        // O inventário: o Patrimônio abre e fecha; a comissão confere com o login; os outros, não.
        MvcResult r = como(PATRIMONIO, post(API + "inventario"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"membrosMatriculas\": [\"IVA-MEMBRO\", \"IVA-M2\", \"IVA-M3\"]}");
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(201);
        String inv = JsonPath.read(corpo(r), "$.uuid");
        String item = JsonPath.read(corpo(r), "$.itens[0].uuid");
        assertThat(codigo(OUTRO, get(API + "inventario/" + inv), "")).isEqualTo(403);
        assertThat(codigo(MEMBRO, get(API + "inventario/" + inv), "")).isEqualTo(200);
        assertThat((List<?>) JsonPath.read(corpo(como(MEMBRO, get(API + "meus-inventarios"), "")), "$[?(@.uuid == '" + inv + "')]")).hasSize(1);
        String conferencia = "{\"resultado\": \"ENCONTRADO\", \"conservacao\": \"BOM\"}";
        assertThat(codigo(OUTRO, post(API + "inventario-item/" + item + "/conferencia"), conferencia)).isEqualTo(403);
        assertThat(codigo(MEMBRO, post(API + "inventario-item/" + item + "/conferencia"), conferencia)).isEqualTo(200);
        assertThat(codigo(MEMBRO, post(API + "inventario/" + inv + "/fechamento"), "")).isEqualTo(403);
        assertThat(codigo(PATRIMONIO, post(API + "inventario/" + inv + "/fechamento"), "")).isEqualTo(200);

        // A baixa acima da alçada: o Patrimônio pede e não confirma; o gestor confirma a dos outros, não a própria.
        String baixa = "{\"motivo\": \"INSERVIVEL\", \"processo\": \"PA 1/2026\", \"dataBaixa\": \"" + hoje + "\"}";
        r = como(PATRIMONIO, post(API + "bem/" + bem + "/baixa"), baixa);
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(201);
        String pedida = JsonPath.read(corpo(r), "$.uuid");
        assertThat(codigo(PATRIMONIO, post(API + "baixa/" + pedida + "/confirmacao"), "")).isEqualTo(403);
        String outroBem = bem(GESTOR, "30000");
        r = como(GESTOR, post(API + "bem/" + outroBem + "/baixa"), baixa);
        assertThat(codigo(GESTOR, post(API + "baixa/" + JsonPath.read(corpo(r), "$.uuid") + "/confirmacao"), "")).isEqualTo(422);
        r = como(GESTOR, post(API + "baixa/" + pedida + "/confirmacao"), "");
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(200);

        assertThat(codigo(OUTRO, get(API + "painel"), "")).isEqualTo(403);
        assertThat(codigo(PATRIMONIO, get(API + "painel"), "")).isEqualTo(200);
        assertThat(mockMvc.perform(get(API + "meus-inventarios")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
