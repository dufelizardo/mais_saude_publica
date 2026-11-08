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
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Educação permanente com login e autorização ligados (ADR-0173): o RH mantém; o médico, sem RH, não lê nem escreve. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class EducacaoPermanenteAutorizacaoControllerTest {

    private static final String URL = "/api/v1/educacao-permanente/";
    private static final String PREFIXO = "EduPermAut Teste ";
    private static final String GESTOR = "94501867500";
    private static final String MEDICO = "94501867501";
    private static final List<String> CPFS = List.of(GESTOR, MEDICO);

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

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        unidadeDeSaudeRepository.save(u);
        atribuir(GESTOR, "GESTOR", u);
        atribuir(MEDICO, "MEDICO", u);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_competencia where codigo like 'TESTE_C4AUT_%'");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(x -> x.getNome() != null && x.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        unidadeDeSaudeRepository.deleteAllById(unidades);
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

    @Test
    void soORhMantemEConsulta() throws Exception {
        mockMvc.perform(get(URL + "competencia")).andExpect(status().isUnauthorized());
        mockMvc.perform(como(MEDICO, get(URL + "competencia"))).andExpect(status().isForbidden());
        mockMvc.perform(como(MEDICO, get(URL + "treinamento"))).andExpect(status().isForbidden());
        mockMvc.perform(como(MEDICO, post(URL + "competencia").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\": \"TESTE_C4AUT_M\", \"nome\": \"M\"}"))).andExpect(status().isForbidden());

        mockMvc.perform(como(GESTOR, get(URL + "competencia"))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR, get(URL + "conteudo"))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR, post(URL + "competencia").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\": \"TESTE_C4AUT_G\", \"nome\": \"G\"}"))).andExpect(status().isCreated());
    }
}
