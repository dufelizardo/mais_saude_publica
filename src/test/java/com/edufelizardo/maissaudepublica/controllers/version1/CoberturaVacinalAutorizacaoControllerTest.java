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
 * Cobertura vacinal com login e autorização ligados (ADR-0149): o técnico de enfermagem vê a cobertura e a busca ativa
 * da própria unidade, não de outra, e sem filtro só as unidades do acesso; a recepção não vê; a gestão vê a cobertura e
 * a vacinação na Sala de Situação, que é de quem consulta indicadores.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class CoberturaVacinalAutorizacaoControllerTest {

    private static final String PREFIXO = "CobVacAut Teste ";
    private static final String TECNICO = "94601867380";
    private static final String RECEPCAO = "94601867381";
    private static final String GESTOR = "94601867382";
    private static final List<String> CPFS = List.of(TECNICO, RECEPCAO, GESTOR);

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
        atribuir(TECNICO, "TECNICO_DE_ENFERMAGEM", ubs);
        atribuir(RECEPCAO, "RECEPCAO", ubs);
        atribuir(GESTOR, "GESTOR", ubs);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
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
    void coberturaBuscaAtivaESalaDeSituacaoPorPapelEUnidade() throws Exception {
        String daUbs = "/api/v1/vacinacao/cobertura?unidadeId=" + ubs.getUuid();
        mockMvc.perform(como(TECNICO, get(daUbs))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR, get(daUbs))).andExpect(status().isOk());
        mockMvc.perform(como(RECEPCAO, get(daUbs))).andExpect(status().isForbidden());
        mockMvc.perform(como(TECNICO, get("/api/v1/vacinacao/cobertura?unidadeId=" + outra.getUuid()))).andExpect(status().isForbidden());

        // Sem filtro, só as unidades do acesso.
        String rede = mockMvc.perform(como(TECNICO, get("/api/v1/vacinacao/cobertura"))).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        assertThat((String) JsonPath.read(rede, "$.abrangencia")).isEqualTo("Unidades do seu acesso");

        // A busca ativa tem nome de paciente: só quem vacina, na própria unidade.
        mockMvc.perform(como(TECNICO, get("/api/v1/vacinacao/atrasados?unidadeId=" + ubs.getUuid()))).andExpect(status().isOk());
        mockMvc.perform(como(TECNICO, get("/api/v1/vacinacao/atrasados?unidadeId=" + outra.getUuid()))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, get("/api/v1/vacinacao/atrasados?unidadeId=" + ubs.getUuid()))).andExpect(status().isForbidden());

        // A vacinação na Sala de Situação é de quem consulta indicadores.
        String sala = "/api/v1/indicadores-rede/vacinacao?unidadeId=" + ubs.getUuid();
        mockMvc.perform(como(GESTOR, get(sala))).andExpect(status().isOk());
        mockMvc.perform(como(TECNICO, get(sala))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, get("/api/v1/indicadores-rede/vacinacao?unidadeId=" + outra.getUuid()))).andExpect(status().isForbidden());
    }
}
