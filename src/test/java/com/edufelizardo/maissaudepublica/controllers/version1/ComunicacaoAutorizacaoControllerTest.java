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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comunicação com login e autorização ligados (ADR-0168): as permissões separadas, o comunicado emergencial com a coordenação da
 * emergência, quem escreve não aprova o próprio conteúdo, e o portal aberto sem login.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class ComunicacaoAutorizacaoControllerTest {

    private static final String PREFIXO = "ComAut Teste ";
    private static final String URL = "/api/v1/comunicacao/";
    private static final String ADMIN = "94501867380";
    private static final String GESTOR = "94501867381";
    private static final String GESTOR_OUTRA = "94501867382";
    private static final String MEDICO = "94501867383";
    private static final List<String> CPFS = List.of(ADMIN, GESTOR, GESTOR_OUTRA, MEDICO);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private PapelRepository papelRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("Outra");
        atribuir(ADMIN, "ADMINISTRADOR_PLATAFORMA", ubs);
        atribuir(GESTOR, "GESTOR", ubs);
        atribuir(GESTOR_OUTRA, "GESTOR", outra);
        atribuir(MEDICO, "MEDICO", ubs);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_publicacao where comunicado_id in (select uuid from tb_comunicado where titulo like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_publicacao where conteudo_id in (select uuid from tb_conteudo_educativo where titulo like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_comunicado_canal where comunicado_id in (select uuid from tb_comunicado where titulo like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_comunicado where titulo like ?", PREFIXO + "%");
        jdbc.update("delete from tb_conteudo_educativo where titulo like ?", PREFIXO + "%");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        unidadeDeSaudeRepository.deleteAllById(unidades);
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

    private String portal() throws Exception {
        String r = mockMvc.perform(como(ADMIN, get(URL + "canal"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(r, "$[?(@.tipo == 'PORTAL')].uuid");
        return ids.get(0);
    }

    private String corpo(String prioridade, UnidadeDeSaude unidade) throws Exception {
        return "{\"titulo\": \"%sAviso\", \"mensagem\": \"M\", \"prioridade\": \"%s\", \"publico\": \"POPULACAO\", \"inicio\": \"%s\", \"fim\": \"%s\", \"unidadeId\": \"%s\", \"canalIds\": [\"%s\"]}"
                .formatted(PREFIXO, prioridade, LocalDate.now(), LocalDate.now(), unidade.getUuid(), portal());
    }

    private String criar(String cpf, String prioridade) throws Exception {
        String r = mockMvc.perform(como(cpf, post(URL + "comunicado")).contentType(MediaType.APPLICATION_JSON).content(corpo(prioridade, ubs)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(r, "$.uuid");
    }

    @Test
    void publicarPedeAPermissaoNaUnidade() throws Exception {
        mockMvc.perform(get(URL + "comunicado")).andExpect(status().isUnauthorized());
        mockMvc.perform(como(MEDICO, get(URL + "comunicado"))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR_OUTRA, post(URL + "comunicado")).contentType(MediaType.APPLICATION_JSON).content(corpo("NORMAL", ubs))).andExpect(status().isForbidden());
        String id = criar(GESTOR, "NORMAL");
        mockMvc.perform(como(GESTOR_OUTRA, post(URL + "comunicado/" + id + "/publicacao"))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, post(URL + "comunicado/" + id + "/publicacao"))).andExpect(status().isOk());
    }

    @Test
    void emergencialPedeACoordenacaoDaEmergencia() throws Exception {
        String id = criar(ADMIN, "EMERGENCIAL");
        mockMvc.perform(como(ADMIN, post(URL + "comunicado/" + id + "/publicacao"))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, post(URL + "comunicado/" + id + "/publicacao"))).andExpect(status().isOk());
    }

    @Test
    void quemEscreveuNaoAprovaOProprioConteudo() throws Exception {
        String corpo = "{\"titulo\": \"%sDengue\", \"resumo\": \"R\", \"texto\": \"T\", \"tema\": \"D\", \"publico\": \"POPULACAO\", \"validade\": \"%s\"}"
                .formatted(PREFIXO, LocalDate.now().plusMonths(1));
        String r = mockMvc.perform(como(GESTOR, post(URL + "conteudo")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(r, "$.uuid");
        mockMvc.perform(como(GESTOR, post(URL + "conteudo/" + id + "/envio-revisao"))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR, post(URL + "conteudo/" + id + "/aprovacao"))).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(como(ADMIN, post(URL + "conteudo/" + id + "/aprovacao"))).andExpect(status().isOk());
    }

    @Test
    void portalAbreSemLogin() throws Exception {
        mockMvc.perform(get("/api/v1/publico/comunicacao/avisos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/publico/comunicacao/orientacoes")).andExpect(status().isOk());
    }
}
