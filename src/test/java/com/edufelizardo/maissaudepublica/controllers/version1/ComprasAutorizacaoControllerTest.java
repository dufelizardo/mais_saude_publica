package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.MaterialEstoque;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.CategoriaMaterial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.UnidadeMedidaMaterial;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.MaterialEstoqueRepository;
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
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Compras com login e autorização ligados (ADR-0212): o almoxarife pede na sua unidade e não na de outra; o setor de compras não pede, mas
 * analisa e só vê a solicitação depois de enviada; quem só pede lê os fornecedores com o CPF mascarado e não os cadastra; a recepção não entra.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class ComprasAutorizacaoControllerTest {

    private static final String PREFIXO = "ComprasAut Teste ";
    private static final String API = "/api/v1/compras/";
    private static final String COMPRAS = "94977867401";
    private static final String ALMOXARIFE = "94977867402";
    private static final String ALMOXARIFE_OUTRA = "94977867403";
    private static final String GESTOR = "94977867404";
    private static final String RECEPCAO = "94977867405";
    private static final List<String> CPFS = List.of(COMPRAS, ALMOXARIFE, ALMOXARIFE_OUTRA, GESTOR, RECEPCAO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private MaterialEstoqueRepository materialRepository;

    private UnidadeDeSaude ubs;
    private MaterialEstoque material;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        UnidadeDeSaude outra = unidade("Outra");
        UnidadeDeSaude secretaria = unidade("Secretaria");
        material = new MaterialEstoque();
        material.setCodigo("CPA-LUVA");
        material.setNome(PREFIXO + "Luva");
        material.setCategoria(CategoriaMaterial.EPI);
        material.setUnidadeMedida(UnidadeMedidaMaterial.CAIXA);
        material.setAtivo(true);
        material = materialRepository.save(material);
        atribuir(COMPRAS, "COMPRAS", secretaria);
        atribuir(ALMOXARIFE, "ALMOXARIFE", ubs);
        atribuir(ALMOXARIFE_OUTRA, "ALMOXARIFE", outra);
        atribuir(GESTOR, "GESTOR", ubs);
        atribuir(RECEPCAO, "RECEPCAO", ubs);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        ComprasControllerTest.limparDados(jdbc, PREFIXO);
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

    private MvcResult como(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false))).andReturn();
    }

    private int codigo(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return como(cpf, r).getResponse().getStatus();
    }

    private static String corpo(MvcResult r) throws Exception {
        return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    @Test
    void unidadePedeComprasAnalisa() throws Exception {
        String pedido = "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"justificativa\": \"Reposição.\", \"itens\": [{\"tipo\": \"MATERIAL\", \"materialId\": \""
                + material.getUuid() + "\", \"quantidade\": 10}]}";
        assertThat(codigo(ALMOXARIFE_OUTRA, json(post(API + "solicitacao"), pedido))).isEqualTo(403);
        assertThat(codigo(COMPRAS, json(post(API + "solicitacao"), pedido))).isEqualTo(403);
        MvcResult r = como(ALMOXARIFE, json(post(API + "solicitacao"), pedido));
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(201);
        String id = JsonPath.read(corpo(r), "$.uuid");

        // O rascunho é da unidade: compras ainda não vê; o almoxarife da outra unidade nunca.
        assertThat((List<String>) JsonPath.read(corpo(como(COMPRAS, get(API + "solicitacoes"))), "$[*].uuid")).doesNotContain(id);
        assertThat(codigo(COMPRAS, get(API + "solicitacao/" + id))).isEqualTo(403);
        assertThat(codigo(ALMOXARIFE_OUTRA, json(post(API + "solicitacao/" + id + "/envio"), ""))).isEqualTo(403);
        assertThat(codigo(ALMOXARIFE, post(API + "solicitacao/" + id + "/envio"))).isEqualTo(200);
        assertThat((List<String>) JsonPath.read(corpo(como(COMPRAS, get(API + "solicitacoes"))), "$[*].uuid")).contains(id);
        assertThat((List<String>) JsonPath.read(corpo(como(ALMOXARIFE_OUTRA, get(API + "solicitacoes"))), "$[*].uuid")).doesNotContain(id);

        // Só quem compra analisa.
        assertThat(codigo(ALMOXARIFE, json(post(API + "solicitacao/" + id + "/analise"), "{\"decisao\": \"APROVAR\"}"))).isEqualTo(403);
        assertThat(codigo(COMPRAS, json(post(API + "solicitacao/" + id + "/analise"), "{\"decisao\": \"APROVAR\"}"))).isEqualTo(200);
        // Aprovada, a unidade já não cancela; o gestor (que também compra) cancela.
        assertThat(codigo(ALMOXARIFE, json(post(API + "solicitacao/" + id + "/cancelamento"), "{\"motivo\": \"x\"}"))).isEqualTo(403);
        assertThat(codigo(GESTOR, json(post(API + "solicitacao/" + id + "/cancelamento"), "{\"motivo\": \"Não precisa mais.\"}"))).isEqualTo(200);

        assertThat(codigo(RECEPCAO, get(API + "solicitacoes"))).isEqualTo(403);
        assertThat(mockMvc.perform(get(API + "solicitacoes")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void fornecedores() throws Exception {
        String pf = "{\"documento\": \"529.982.247-25\", \"razaoSocial\": \"" + PREFIXO + "Autônomo\"}";
        assertThat(codigo(ALMOXARIFE, json(post(API + "fornecedor"), pf))).isEqualTo(403);
        assertThat(codigo(COMPRAS, json(post(API + "fornecedor"), pf))).isEqualTo(201);
        MvcResult r = como(ALMOXARIFE, get(API + "fornecedores?busca=Aut"));
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        assertThat((List<String>) JsonPath.read(corpo(r), "$[*].documento")).containsExactly("***.982.247-**");
        assertThat((List<String>) JsonPath.read(corpo(como(COMPRAS, get(API + "fornecedores?busca=Aut"))), "$[*].documento"))
                .containsExactly("52998224725");
        assertThat(codigo(RECEPCAO, get(API + "fornecedores"))).isEqualTo(403);
    }
}
