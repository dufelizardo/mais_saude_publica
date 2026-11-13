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
 * Indicadores de qualidade com login e autorização ligados (ADR-0205): o núcleo define a meta e audita na sua unidade, não na alheia; o
 * gestor vê o painel da sua unidade, não o da outra; o técnico não vê o painel nem cadastra checklist; a recepção não audita.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class IndicadoresQualidadeAutorizacaoControllerTest {

    private static final String PREFIXO = "IndQualAut Teste ";
    private static final String API = "/api/v1/qualidade/";
    private static final String NSP = "94966867501";
    private static final String TECNICO = "94966867502";
    private static final String TECNICO_OUTRO = "94966867503";
    private static final String GESTOR_OUTRA = "94966867504";
    private static final String GESTOR = "94966867505";
    private static final String RECEPCAO = "94966867506";
    private static final List<String> CPFS = List.of(NSP, TECNICO, TECNICO_OUTRO, GESTOR_OUTRA, GESTOR, RECEPCAO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude hospital;

    @BeforeEach
    void seed() {
        limpar();
        hospital = unidade("Hospital");
        UnidadeDeSaude outra = unidade("Outra");
        Profissional p = new Profissional();
        p.setMatricula("IQA-TEC");
        p.setCpf(TECNICO);
        p.setNome(PREFIXO + "Técnico");
        p.setAtivo(true);
        profissionalRepository.save(p);
        atribuir(NSP, "NUCLEO_SEGURANCA_PACIENTE", hospital);
        atribuir(TECNICO, "TECNICO_DE_ENFERMAGEM", hospital);
        atribuir(TECNICO_OUTRO, "TECNICO_DE_ENFERMAGEM", hospital);
        atribuir(GESTOR_OUTRA, "GESTOR", outra);
        atribuir(GESTOR, "GESTOR", hospital);
        atribuir(RECEPCAO, "RECEPCAO", hospital);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        IndicadoresQualidadeControllerTest.limparDados(jdbc, PREFIXO, "IQA-%");
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

    private static String corpo(MvcResult r) throws Exception {
        return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    @Test
    void metasPainelEAuditoria() throws Exception {
        UnidadeDeSaude outra = unidadeDeSaudeRepository.findAll().stream().filter(u -> u.getNome().equals(PREFIXO + "Outra")).findFirst().orElseThrow();
        String meta = "{\"indicador\": \"TAXA_QUEDAS\", \"valor\": 3, \"unidadeId\": \"";
        assertThat(status(NSP, json(post(API + "meta"), meta + outra.getUuid() + "\"}"))).isEqualTo(403);
        assertThat(status(GESTOR_OUTRA, json(post(API + "meta"), meta + hospital.getUuid() + "\"}"))).isEqualTo(403);
        assertThat(status(TECNICO, json(post(API + "meta"), meta + hospital.getUuid() + "\"}"))).isEqualTo(403);
        assertThat(status(NSP, json(post(API + "meta"), meta + hospital.getUuid() + "\"}"))).isEqualTo(201);

        // O painel no escopo da unidade.
        assertThat(status(GESTOR, get(API + "indicadores?unidadeId=" + hospital.getUuid()))).isEqualTo(200);
        assertThat(status(GESTOR, get(API + "indicadores?unidadeId=" + outra.getUuid()))).isEqualTo(403);
        assertThat(status(TECNICO, get(API + "indicadores"))).isEqualTo(403);
        MvcResult r = como(GESTOR_OUTRA, get(API + "metas"));
        assertThat((List<String>) JsonPath.read(corpo(r), "$[*]")).isEmpty();
        assertThat((List<String>) JsonPath.read(corpo(como(GESTOR, get(API + "metas"))), "$[*].indicador")).containsExactly("TAXA_QUEDAS");

        // A auditoria de protocolo e o catálogo de checklists.
        r = como(NSP, get(API + "checklists"));
        List<String> versoes = JsonPath.read(corpo(r), "$[?(@.codigo == 'HIGIENE_DAS_MAOS')].versaoId");
        String auditoria = "{\"versaoId\": \"" + versoes.get(0) + "\", \"respostas\": {\"M1\": \"CONFORME\", \"M2\": \"CONFORME\", \"M3\": \"CONFORME\", "
                + "\"M4\": \"NAO_CONFORME\", \"M5\": \"CONFORME\"}, \"unidadeId\": \"";
        assertThat(status(RECEPCAO, json(post(API + "auditoria-protocolo"), auditoria + hospital.getUuid() + "\"}"))).isEqualTo(403);
        assertThat(status(NSP, json(post(API + "auditoria-protocolo"), auditoria + outra.getUuid() + "\"}"))).isEqualTo(403);
        assertThat(status(NSP, json(post(API + "auditoria-protocolo"), auditoria + hospital.getUuid() + "\"}"))).isEqualTo(201);
        assertThat(status(TECNICO, json(post(API + "checklist"), "{\"codigo\": \"TSTQ3_AUT\", \"nome\": \"x\", \"itens\": [{\"codigo\": \"A\", "
                + "\"texto\": \"x\"}]}"))).isEqualTo(403);
        assertThat(mockMvc.perform(get(API + "indicadores")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
