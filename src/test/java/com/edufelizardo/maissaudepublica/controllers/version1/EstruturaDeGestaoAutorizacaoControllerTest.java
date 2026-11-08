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

/** Coordenações e gerência com a segurança ligada (ADR-0175): o gestor da regional mantém a coordenação e lê a regional; o da UBS, não. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class EstruturaDeGestaoAutorizacaoControllerTest {

    private static final String PREFIXO = "GestaoAut Teste ";
    private static final String GESTOR_REGIONAL = "94501867700";
    private static final String GESTOR_UBS = "94501867701";
    private static final String ACS = "94501867702";
    private static final List<String> CPFS = List.of(GESTOR_REGIONAL, GESTOR_UBS, ACS);

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

    private UnidadeDeSaude regional;

    @BeforeEach
    void seed() {
        limpar();
        regional = unidade("Regional", TipoUnidadeDeSaude.REGIONAL, null);
        UnidadeDeSaude ubs = unidade("UBS", TipoUnidadeDeSaude.UBS, regional);
        atribuir(GESTOR_REGIONAL, "GESTOR", regional);
        atribuir(GESTOR_UBS, "GESTOR", ubs);
        atribuir(ACS, "AGENTE_COMUNITARIO_SAUDE", ubs);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_area_gestao where nome like ?", PREFIXO + "%");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("update tb_unidade_de_saude set supervisao_regional_id = null where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo, UnidadeDeSaude supervisao) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        u.setSupervisaoRegional(supervisao);
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

    private MockHttpServletRequestBuilder coordenacao() {
        return post("/api/v1/gestao/area").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\": \"COORDENACAO\", \"tema\": \"ATENCAO_BASICA\", \"nome\": \"%sCoordenação\", \"regionalId\": \"%s\"}".formatted(PREFIXO, regional.getUuid()));
    }

    @Test
    void gestorDaRegionalMantemELeARegional() throws Exception {
        String vigilancia = "/api/v1/indicadores-rede/vigilancia?unidadeId=" + regional.getUuid();
        mockMvc.perform(get("/api/v1/gestao/area")).andExpect(status().isUnauthorized());
        mockMvc.perform(como(ACS, get("/api/v1/gestao/area"))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR_UBS, coordenacao())).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR_UBS, get(vigilancia))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR_REGIONAL, coordenacao())).andExpect(status().isCreated());
        mockMvc.perform(como(GESTOR_REGIONAL, get(vigilancia))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR_UBS, get("/api/v1/gestao/area"))).andExpect(status().isOk());
    }
}
