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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Vigilância Sanitária com login e autorização ligados (ADR-0199): o fiscal cadastra e inspeciona, assina com a própria matrícula e
 * não licencia nem vê o denunciante; a coordenação licencia, registra a denúncia e vê o denunciante; o gestor gerencia sem inspecionar;
 * a recepção não entra.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class VigilanciaSanitariaAutorizacaoControllerTest {

    private static final String PREFIXO = "VisaAut Teste ";
    private static final String API = "/api/v1/vigilancia-sanitaria/";
    private static final String FISCAL = "94922867401";
    private static final String COORDENACAO = "94922867402";
    private static final String GESTOR = "94922867403";
    private static final String RECEPCAO = "94922867404";
    private static final List<String> CPFS = List.of(FISCAL, COORDENACAO, GESTOR, RECEPCAO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude unidade;

    @BeforeEach
    void seed() {
        limpar();
        unidade = new UnidadeDeSaude();
        unidade.setNome(PREFIXO + "Vigilância Sanitária");
        unidade.setTipo(TipoUnidadeDeSaude.UBS);
        unidade.setAtivo(true);
        unidade = unidadeDeSaudeRepository.save(unidade);
        profissional("VISAA-FISC", FISCAL, "Fiscal");
        profissional("VISAA-OUTRO", "90177700002", "Outro fiscal");
        atribuir(FISCAL, "FISCAL_SANITARIO");
        atribuir(COORDENACAO, "COORDENACAO_VISA");
        atribuir(GESTOR, "GESTOR");
        atribuir(RECEPCAO, "RECEPCAO");
    }

    @AfterEach
    void limpar() {
        VigilanciaSanitariaControllerTest.limparDados(jdbc, PREFIXO, "VISAA-%");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private void profissional(String matricula, String cpf, String nome) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + nome);
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    private void atribuir(String cpf, String papel) {
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

    private static String corpo(MvcResult r) throws Exception {
        return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static String estabelecimento(String nome) {
        return VigilanciaSanitariaControllerTest.corpoEstabelecimento(VigilanciaSanitariaControllerTest.cnpj(), nome, "5611201:MEDIO")
                .replace(VigilanciaSanitariaControllerTest.PREFIXO, PREFIXO);
    }

    @Test
    void visaPorPapel() throws Exception {
        assertThat(mockMvc.perform(get(API + "estabelecimentos")).andReturn().getResponse().getStatus()).isEqualTo(401);
        assertThat(status(RECEPCAO, get(API + "estabelecimentos"))).isEqualTo(403);
        assertThat(status(RECEPCAO, json(post(API + "estabelecimento"), estabelecimento("Bar")))).isEqualTo(403);

        // O fiscal cadastra e inspeciona; não licencia nem registra denúncia.
        MvcResult r = como(FISCAL, json(post(API + "estabelecimento"), estabelecimento("Bar")));
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        String bar = JsonPath.read(corpo(r), "$.uuid");
        assertThat(status(FISCAL, json(post(API + "estabelecimento/" + bar + "/licenca"), "{}"))).isEqualTo(403);
        String denuncia = "{\"canal\": \"TELEFONE\", \"descricao\": \"" + PREFIXO + "Bar sujo\", \"estabelecimentoId\": \"" + bar
                + "\", \"denuncianteNome\": \"João\"}";
        assertThat(status(FISCAL, json(post(API + "denuncia"), denuncia))).isEqualTo(403);

        String inspecao = "{\"estabelecimentoId\": \"" + bar + "\", \"motivo\": \"PROGRAMADA\", \"resultado\": \"SATISFATORIA\", \"relatorio\": \"Ok.\", ";
        // Só a matrícula de outro fiscal: quem registra assina e precisa estar entre eles.
        assertThat(status(FISCAL, json(post(API + "inspecao"), inspecao + "\"fiscais\": [\"VISAA-OUTRO\"]}"))).isEqualTo(403);
        r = como(FISCAL, json(post(API + "inspecao"), inspecao + "\"fiscais\": [\"VISAA-FISC\", \"VISAA-OUTRO\"]}"));
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        assertThat((Boolean) JsonPath.read(corpo(r), "$.assinada")).isTrue();
        assertThat((Integer) JsonPath.read(corpo(r), "$.fiscais.length()")).isEqualTo(2);

        // A coordenação licencia e registra a denúncia; o denunciante só aparece para ela.
        assertThat(status(COORDENACAO, json(post(API + "estabelecimento/" + bar + "/licenca"), "{}"))).isEqualTo(201);
        assertThat(status(COORDENACAO, json(post(API + "denuncia"), denuncia))).isEqualTo(201);
        assertThat((List<String>) JsonPath.read(corpo(como(COORDENACAO, get(API + "denuncias"))), "$[?(@.estabelecimentoId == '" + bar + "')].denuncianteNome"))
                .containsExactly("João");
        assertThat((List<Object>) JsonPath.read(corpo(como(FISCAL, get(API + "denuncias"))), "$[?(@.estabelecimentoId == '" + bar + "')].denuncianteNome"))
                .containsExactly((Object) null);
        // A coordenação inspeciona com a própria matrícula: ela não é profissional cadastrado aqui, então nenhum fiscal confere.
        assertThat(status(COORDENACAO, json(post(API + "inspecao"), inspecao + "\"fiscais\": [\"VISAA-FISC\"]}"))).isEqualTo(403);

        // O gestor lê e gerencia, mas não inspeciona; o fiscal não mantém os roteiros; a recepção não abre o termo.
        assertThat(status(GESTOR, get(API + "pendencias"))).isEqualTo(200);
        assertThat(status(GESTOR, json(post(API + "inspecao"), inspecao + "\"fiscais\": [\"VISAA-FISC\"]}"))).isEqualTo(403);
        assertThat(status(FISCAL, json(post(API + "roteiro"), "{\"codigo\": \"TSTVISA_AUT\", \"nome\": \"x\", \"itens\": [{\"codigo\": \"X1\", "
                + "\"texto\": \"x\", \"gravidade\": \"LEVE\"}]}"))).isEqualTo(403);
        assertThat(status(RECEPCAO, get(API + "inspecao/" + JsonPath.read(corpo(r), "$.uuid") + "/termo.pdf"))).isEqualTo(403);
        assertThat(status(FISCAL, get(API + "inspecao/" + JsonPath.read(corpo(r), "$.uuid") + "/termo.pdf"))).isEqualTo(200);
    }
}
