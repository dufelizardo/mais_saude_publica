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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Campanhas de comunicação com login e autorização ligados (ADR-0174): o gestor publica na própria unidade; o médico não; o portal abre sem login. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class CampanhaComunicacaoAutorizacaoControllerTest {

    private static final String URL = "/api/v1/comunicacao/campanha";
    private static final String PREFIXO = "CampComAut Teste ";
    private static final String GESTOR = "94501867600";
    private static final String MEDICO = "94501867601";
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

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("Outra");
        atribuir(GESTOR, "GESTOR", ubs);
        atribuir(MEDICO, "MEDICO", ubs);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_campanha_comunicacao_tema where campanha_id in (select uuid from tb_campanha_comunicacao where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_campanha_comunicacao where nome like ?", PREFIXO + "%");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(x -> x.getNome() != null && x.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
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

    private MockHttpServletRequestBuilder campanha(UUID unidade) {
        LocalDate hoje = LocalDate.now();
        return post(URL).contentType(MediaType.APPLICATION_JSON).content(("{\"nome\": \"%sCampanha\", \"objetivo\": \"O\", \"inicio\": \"%s\", \"fim\": \"%s\", "
                + "\"publicoAlvo\": \"POPULACAO_GERAL\", \"unidadeId\": \"%s\"}").formatted(PREFIXO, hoje, hoje.plusDays(5), unidade));
    }

    @Test
    void gestorPublicaNaPropriaUnidade() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mockMvc.perform(como(MEDICO, get(URL))).andExpect(status().isForbidden());
        mockMvc.perform(como(MEDICO, campanha(ubs.getUuid()))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, campanha(outra.getUuid()))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, campanha(ubs.getUuid()))).andExpect(status().isCreated());
        mockMvc.perform(como(GESTOR, get(URL))).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/publico/comunicacao/campanhas")).andExpect(status().isOk());
    }
}
