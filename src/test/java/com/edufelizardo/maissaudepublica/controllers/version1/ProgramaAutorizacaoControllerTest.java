package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.CategoriaProgramaRepository;
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

/**
 * Programas com login e autorização ligados (ADR-0118): o catálogo é lido por qualquer logado, cadastrado só com
 * PROGRAMA.GERENCIAR (o gestor tem, o enfermeiro não); sem login só a consulta pública responde.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class ProgramaAutorizacaoControllerTest {

    private static final String PREFIXO = "ProgramaAut Teste ";
    private static final String GESTOR = "94501867350";
    private static final String ENFERMEIRO = "94501867351";
    private static final List<String> CPFS = List.of(GESTOR, ENFERMEIRO);

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

    @Autowired
    private CategoriaProgramaRepository categoriaRepository;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        atribuir(GESTOR, "GESTOR", ubs);
        atribuir(ENFERMEIRO, "ENFERMEIRO", ubs);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_programa where codigo = 'TESTE_PG_AUT'");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        unidadeDeSaudeRepository.deleteAllById(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList());
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
    void catalogoAbertoAosLogadosCadastroComPermissaoEConsultaPublicaSemLogin() throws Exception {
        UUID categoria = categoriaRepository.findByNomeIgnoreCase("Crônicos").orElseThrow().getUuid();
        String corpo = "{\"codigo\": \"TESTE_PG_AUT\", \"nome\": \"Programa autorização\", \"categoriaId\": \"%s\"}".formatted(categoria);

        mockMvc.perform(get("/api/v1/programa/")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/publico/programa")).andExpect(status().isOk());
        mockMvc.perform(como(ENFERMEIRO, get("/api/v1/programa/"))).andExpect(status().isOk());
        mockMvc.perform(como(ENFERMEIRO, post("/api/v1/programa/")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, post("/api/v1/programa/")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());
    }
}
