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
 * Contratos com login e autorização ligados (ADR-0213): o gestor da secretaria conduz a contratação até o contrato; o almoxarife não abre
 * contratação nem lista os contratos; o fiscal designado, mesmo sem permissão de Compras, vê o próprio contrato em "meus contratos" e o
 * detalhe; outro profissional, não; sem login, 401.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class ContratosAutorizacaoControllerTest {

    private static final String PREFIXO = "ContratosAut Teste ";
    private static final String API = "/api/v1/compras/";
    private static final String GESTOR = "94988867401";
    private static final String ALMOXARIFE = "94988867402";
    private static final String FISCAL = "94988867403";
    private static final String OUTRO = "94988867404";
    private static final List<String> CPFS = List.of(GESTOR, ALMOXARIFE, FISCAL, OUTRO);

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

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(u);
        profissional("CTA-GESTOR", GESTOR);
        profissional("CTA-FISCAL", FISCAL);
        atribuir(GESTOR, "GESTOR");
        atribuir(ALMOXARIFE, "ALMOXARIFE");
        atribuir(FISCAL, "RECEPCAO");
        atribuir(OUTRO, "RECEPCAO");
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        ComprasControllerTest.limparDados(jdbc, PREFIXO);
        jdbc.update("delete from tb_profissional where matricula like 'CTA-%'");
    }

    private void profissional(String matricula, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    private void atribuir(String cpf, String papel) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(ubs);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private MvcResult como(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false))).andReturn();
    }

    /** Como o gestor, esperando o status. */
    private String gestor(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        MvcResult res = como(GESTOR, r.contentType(MediaType.APPLICATION_JSON).content(corpo));
        String texto = res.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(res.getResponse().getStatus()).as(texto).isEqualTo(esperado);
        return texto;
    }

    private int codigo(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return como(cpf, r).getResponse().getStatus();
    }

    @Test
    void conducaoEFiscal() throws Exception {
        String s = JsonPath.read(gestor(post(API + "solicitacao"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"justificativa\": \"Limpeza.\", \"itens\": [{\"tipo\": "
                + "\"SERVICO\", \"descricao\": \"Limpeza\", \"unidadeMedida\": \"mes\", \"quantidade\": 12}]}", 201), "$.uuid");
        gestor(post(API + "solicitacao/" + s + "/envio"), "", 200);
        String item = JsonPath.read(gestor(post(API + "solicitacao/" + s + "/analise"), "{\"decisao\": \"APROVAR\"}", 200), "$.itens[0].uuid");
        gestor(post(API + "item/" + item + "/cotacao"), "{\"fonte\": \"SITE\", \"referencia\": \"x\", \"precoUnitario\": 100, \"cotadoEm\": \"" + hoje + "\"}", 201);
        gestor(post(API + "solicitacao/" + s + "/pesquisa"), "{\"justificativas\": [{\"itemId\": \"" + item + "\", \"justificativa\": \"Único.\"}]}", 200);
        String forn = JsonPath.read(gestor(post(API + "fornecedor"), "{\"documento\": \"" + ComprasControllerTest.cnpj("904100000001") + "\", \"razaoSocial\": \""
                + PREFIXO + "Limpeza\"}", 201), "$.uuid");
        for (String tipo : List.of("FEDERAL", "FGTS", "TRABALHISTA", "ESTADUAL", "MUNICIPAL")) {
            gestor(post(API + "fornecedor/" + forn + "/certidao"), "{\"tipo\": \"" + tipo + "\", \"emitidaEm\": \"" + hoje + "\", \"validaAte\": \""
                    + hoje.plusDays(90) + "\"}", 201);
        }
        String abrir = "{\"numeroProcesso\": \"" + PREFIXO + "1\", \"modalidade\": \"PREGAO_ELETRONICO\", \"objeto\": \"Limpeza.\", \"solicitacaoIds\": [\"" + s + "\"]}";
        assertThat(codigo(ALMOXARIFE, post(API + "contratacao").contentType(MediaType.APPLICATION_JSON).content(abrir))).isEqualTo(403);
        String c = gestor(post(API + "contratacao"), abrir, 201);
        gestor(post(API + "item-contratacao/" + JsonPath.read(c, "$.itens[0].uuid") + "/resultado"), "{\"resultado\": \"ADJUDICADO\", \"fornecedorId\": \"" + forn
                + "\", \"precoUnitario\": 100}", 200);
        c = gestor(post(API + "contratacao/" + JsonPath.read(c, "$.uuid") + "/homologacao"), "{\"instrumentos\": [{\"fornecedorId\": \"" + forn + "\", \"tipo\": "
                + "\"CONTRATO\", \"numero\": \"" + PREFIXO + "CT\", \"inicio\": \"" + hoje + "\", \"fim\": \"" + hoje.plusYears(1) + "\", \"servicoContinuo\": true, "
                + "\"gestorMatricula\": \"CTA-GESTOR\", \"fiscalMatricula\": \"CTA-FISCAL\"}]}", 200);
        String contrato = JsonPath.read(c, "$.instrumentos[0].uuid");

        // O fiscal (recepção, sem permissão de Compras) vê o próprio contrato; o outro, não; o almoxarife não lista.
        MvcResult r = como(FISCAL, get(API + "meus-contratos"));
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        assertThat((List<String>) JsonPath.read(r.getResponse().getContentAsString(StandardCharsets.UTF_8), "$[*].uuid")).containsExactly(contrato);
        assertThat(codigo(FISCAL, get(API + "instrumento/" + contrato))).isEqualTo(200);
        assertThat(codigo(FISCAL, post(API + "instrumento/" + contrato + "/aditivo").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\": \"PRAZO\", \"novoFim\": \"" + hoje.plusYears(2) + "\", \"justificativa\": \"x\", \"assinadoEm\": \"" + hoje + "\"}"))).isEqualTo(403);
        assertThat((List<?>) JsonPath.read(como(OUTRO, get(API + "meus-contratos")).getResponse().getContentAsString(StandardCharsets.UTF_8), "$")).isEmpty();
        assertThat(codigo(OUTRO, get(API + "instrumento/" + contrato))).isEqualTo(403);
        assertThat(codigo(ALMOXARIFE, get(API + "instrumentos"))).isEqualTo(403);
        assertThat(codigo(GESTOR, get(API + "instrumentos"))).isEqualTo(200);
        assertThat(mockMvc.perform(get(API + "meus-contratos")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
