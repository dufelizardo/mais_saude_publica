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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Educação em saúde com a segurança ligada (ADR-0195): o enfermeiro da unidade (PROGRAMA.ACOMPANHAR) preenche a ação educativa dela e não a de
 * outra unidade; a recepção não preenche nem lê.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class EducacaoEmSaudeAutorizacaoControllerTest {

    private static final String PREFIXO = "EduSaudeAut Teste ";
    private static final String ENFERMEIRO = "94901867750";
    private static final String RECEPCAO = "94901867751";
    private static final List<String> CPFS = List.of(ENFERMEIRO, RECEPCAO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private String daUbs;
    private String daOutra;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude ubs = unidade("UBS");
        UnidadeDeSaude outra = unidade("Outra");
        atribuir(ENFERMEIRO, "ENFERMEIRO", ubs);
        atribuir(RECEPCAO, "RECEPCAO", ubs);
        daUbs = acao(ubs);
        daOutra = acao(outra);
    }

    @AfterEach
    void limpar() {
        String acoes = "(select uuid from tb_acao_programatica where descricao like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_acao_educativa_conteudo where acao_educativa_id in (select uuid from tb_acao_educativa where acao_id in " + acoes + ")");
        jdbc.update("delete from tb_acao_educativa where acao_id in " + acoes);
        jdbc.update("delete from tb_acao_programatica where descricao like '" + PREFIXO + "%'");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private String acao(UnidadeDeSaude u) {
        return jdbc.queryForObject("insert into tb_acao_programatica (uuid, programa_id, unidade_id, tipo, situacao, data_prevista, descricao, criada_em) "
                + "select gen_random_uuid(), uuid, ?, 'GRUPO', 'PENDENTE', ?, ?, now() from tb_programa where codigo = 'SAUDE_DA_FAMILIA' returning cast(uuid as varchar)",
                String.class, u.getUuid(), LocalDate.now().plusDays(2), PREFIXO + u.getNome());
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
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false)).contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void cadaPapelNoSeuLugar() throws Exception {
        String corpo = "{\"tema\": \"Dengue\"}";
        mockMvc.perform(como(ENFERMEIRO, put("/api/v1/acao-programatica/" + daUbs + "/educacao")).content(corpo)).andExpect(status().isOk());
        mockMvc.perform(como(ENFERMEIRO, put("/api/v1/acao-programatica/" + daOutra + "/educacao")).content(corpo)).andExpect(status().isForbidden());
        mockMvc.perform(como(RECEPCAO, put("/api/v1/acao-programatica/" + daUbs + "/educacao")).content(corpo)).andExpect(status().isForbidden());
        mockMvc.perform(como(RECEPCAO, get("/api/v1/acao-programatica/" + daUbs + "/educacao"))).andExpect(status().isForbidden());
        mockMvc.perform(como(ENFERMEIRO, get("/api/v1/acao-programatica/" + daUbs + "/educacao"))).andExpect(status().isOk());
    }
}
