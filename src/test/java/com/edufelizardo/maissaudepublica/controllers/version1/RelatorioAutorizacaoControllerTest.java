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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Relatórios com login e autorização ligados (ADR-0167): pedem INDICADOR.CONSULTAR no escopo da unidade, e a exportação entra na trilha. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class RelatorioAutorizacaoControllerTest {

    private static final String PREFIXO = "RelatorioAut Teste ";
    private static final String GESTOR = "94501867370";
    private static final String ENFERMEIRO = "94501867371";
    private static final List<String> CPFS = List.of(GESTOR, ENFERMEIRO);
    private static final String URL = "/api/v1/relatorios";

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
        atribuir(ENFERMEIRO, "ENFERMEIRO", ubs);
    }

    @AfterEach
    void limpar() {
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

    @Test
    void relatorioPedeIndicadorConsultarNoEscopoDaUnidade() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mockMvc.perform(como(ENFERMEIRO, get(URL))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, get(URL))).andExpect(status().isOk());
        for (String codigo : List.of("PRODUCAO", "SITUACAO", "PROGRAMAS", "VACINACAO", "VIGILANCIA", "CAMPANHAS_COMUNICACAO", "EDUCACAO_EM_SAUDE")) {
            mockMvc.perform(como(GESTOR, get(URL + "/" + codigo + "?unidadeId=" + ubs.getUuid()))).andExpect(status().isOk());
            mockMvc.perform(como(GESTOR, get(URL + "/" + codigo + "?unidadeId=" + outra.getUuid()))).andExpect(status().isForbidden());
            mockMvc.perform(como(ENFERMEIRO, get(URL + "/" + codigo))).andExpect(status().isForbidden());
        }
    }

    @Test
    void exportacaoEntraNaTrilhaEARecusadaNaoGeraArquivo() throws Exception {
        mockMvc.perform(como(GESTOR, get(URL + "/PRODUCAO/exportacao?unidadeId=" + outra.getUuid()))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, get(URL + "/PRODUCAO/exportacao?unidadeId=" + ubs.getUuid()))).andExpect(status().isOk());
        Integer exportacoes = jdbc.queryForObject("select count(*) from tb_evento_auditoria where usuario_cpf = ? and acao = 'EXPORTACAO' and status_http = 200",
                Integer.class, GESTOR);
        assertThat(exportacoes).isEqualTo(1);
    }
}
