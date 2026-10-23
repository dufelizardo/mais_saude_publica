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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Produção assistencial com login e autorização ligados (ADR-0143): INDICADOR.CONSULTAR (o gestor tem, o enfermeiro
 * não), no escopo da unidade; sem unidade, só as unidades do acesso; a exportação entra na trilha.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class ProducaoAutorizacaoControllerTest {

    private static final String PREFIXO = "ProducaoAut Teste ";
    private static final String GESTOR = "94501867360";
    private static final String ENFERMEIRO = "94501867361";
    private static final List<String> CPFS = List.of(GESTOR, ENFERMEIRO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("Outra");
        atribuir(GESTOR, "GESTOR", ubs);
        atribuir(ENFERMEIRO, "ENFERMEIRO", ubs);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        unidadeDeSaudeRepository.deleteAllById(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList());
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

    @Test
    void producaoPedeIndicadorConsultarNoEscopoDaUnidade() throws Exception {
        mockMvc.perform(get("/api/v1/producao")).andExpect(status().isUnauthorized());
        mockMvc.perform(como(ENFERMEIRO, get("/api/v1/producao"))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, get("/api/v1/producao?unidadeId=" + ubs.getUuid()))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR, get("/api/v1/producao?unidadeId=" + outra.getUuid()))).andExpect(status().isForbidden());
        String r = mockMvc.perform(como(GESTOR, get("/api/v1/producao"))).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat((String) JsonPath.read(r, "$.abrangencia")).isEqualTo("Unidades do seu acesso");
        // O ranking só traz as unidades do acesso.
        String ranking = mockMvc.perform(como(GESTOR, get("/api/v1/producao/unidades"))).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        assertThat((List<String>) JsonPath.read(ranking, "$[*].unidadeId")).doesNotContain(outra.getUuid().toString());

        mockMvc.perform(como(GESTOR, get("/api/v1/producao/exportacao?unidadeId=" + ubs.getUuid()))).andExpect(status().isOk());
        Integer exportacoes = jdbc.queryForObject("select count(*) from tb_evento_auditoria where usuario_cpf = ? and acao = 'EXPORTACAO'",
                Integer.class, GESTOR);
        assertThat(exportacoes).isEqualTo(1);
    }

    @Test
    void situacaoOperacionalPedeIndicadorConsultarNoEscopo() throws Exception {
        // BI2 (ADR-0144): a mesma permissão e o mesmo escopo da produção.
        for (String rota : List.of("situacao", "situacao/espera", "situacao/ocupacao", "situacao/escala")) {
            mockMvc.perform(get("/api/v1/" + rota)).andExpect(status().isUnauthorized());
            mockMvc.perform(como(ENFERMEIRO, get("/api/v1/" + rota))).andExpect(status().isForbidden());
            mockMvc.perform(como(GESTOR, get("/api/v1/" + rota + "?unidadeId=" + ubs.getUuid()))).andExpect(status().isOk());
            mockMvc.perform(como(GESTOR, get("/api/v1/" + rota + "?unidadeId=" + outra.getUuid()))).andExpect(status().isForbidden());
        }
        String r = mockMvc.perform(como(GESTOR, get("/api/v1/situacao"))).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat((String) JsonPath.read(r, "$.abrangencia")).isEqualTo("Unidades do seu acesso");
    }
}
