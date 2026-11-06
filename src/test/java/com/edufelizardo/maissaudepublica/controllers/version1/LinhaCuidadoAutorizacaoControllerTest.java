package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Linhas de cuidado com login e autorização ligados (ADR-0152): o catálogo é da gestão; a enfermagem abre o percurso e
 * vê o painel na própria unidade, não em outra; a recepção não abre nem vê o painel.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class LinhaCuidadoAutorizacaoControllerTest {

    private static final String PREFIXO = "LinhaAut Teste ";
    private static final String CODIGO = "LT_AUT_";
    private static final String ENFERMEIRO = "94801867380";
    private static final String RECEPCAO = "94801867381";
    private static final String GESTOR = "94801867382";
    private static final List<String> CPFS = List.of(ENFERMEIRO, RECEPCAO, GESTOR);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("Outra");
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Gestante");
        p.setCpf("90033300001");
        p.setSexo(Sexo.FEMININO);
        p.setDataNascimento(LocalDate.of(1998, 6, 1));
        p.setAtivo(true);
        paciente = pacienteRepository.save(p);
        atribuir(ENFERMEIRO, "ENFERMEIRO", ubs);
        atribuir(RECEPCAO, "RECEPCAO", ubs);
        atribuir(GESTOR, "GESTOR", ubs);
    }

    @AfterEach
    void limpar() {
        String pacientes = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_registro_etapa_percurso where percurso_id in (select uuid from tb_percurso_linha_cuidado where paciente_id in " + pacientes + ")");
        jdbc.update("delete from tb_percurso_linha_cuidado where paciente_id in " + pacientes);
        jdbc.update("delete from tb_etapa_linha_cuidado where linha_id in (select uuid from tb_linha_cuidado where codigo like '" + CODIGO + "%')");
        jdbc.update("delete from tb_linha_cuidado where codigo like '" + CODIGO + "%'");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
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

    private int enviar(String cpf, MockHttpServletRequestBuilder r, String corpo) throws Exception {
        return mockMvc.perform(como(cpf, r).contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse().getStatus();
    }

    private int ler(String cpf, String url) throws Exception {
        return mockMvc.perform(como(cpf, get(url))).andReturn().getResponse().getStatus();
    }

    @Test
    void catalogoPercursoEPainelPorPapelEUnidade() throws Exception {
        // O catálogo: lê quem está logado; cadastra a gestão.
        assertThat(ler(RECEPCAO, "/api/v1/linhas-de-cuidado")).isEqualTo(200);
        String linha = "{\"codigo\": \"%sX\", \"nome\": \"Linha\", \"etapas\": [{\"nome\": \"Visita\", \"tipo\": \"MANUAL\"}]}".formatted(CODIGO);
        assertThat(enviar(ENFERMEIRO, post("/api/v1/linha-de-cuidado"), linha)).isEqualTo(403);
        assertThat(enviar(GESTOR, post("/api/v1/linha-de-cuidado"), linha)).isEqualTo(201);

        // O percurso: a enfermagem abre na própria unidade, não em outra; a recepção não abre.
        String gestante = jdbc.queryForObject("select cast(uuid as varchar) from tb_linha_cuidado where codigo = 'GESTANTE'", String.class);
        String abrir = "{\"pacienteId\": \"%s\", \"linhaId\": \"%s\", \"inicio\": \"%s\", \"unidadeId\": \"%s\"}";
        assertThat(enviar(RECEPCAO, post("/api/v1/percurso-linha-cuidado"), abrir.formatted(paciente.getUuid(), gestante, LocalDate.now().minusDays(30),
                ubs.getUuid()))).isEqualTo(403);
        assertThat(enviar(ENFERMEIRO, post("/api/v1/percurso-linha-cuidado"), abrir.formatted(paciente.getUuid(), gestante, LocalDate.now().minusDays(30),
                outra.getUuid()))).isEqualTo(403);
        assertThat(enviar(ENFERMEIRO, post("/api/v1/percurso-linha-cuidado"), abrir.formatted(paciente.getUuid(), gestante, LocalDate.now().minusDays(30),
                ubs.getUuid()))).isEqualTo(201);

        // O painel tem nomes: só quem acompanha.
        assertThat(ler(ENFERMEIRO, "/api/v1/linhas-de-cuidado/painel?unidadeId=" + ubs.getUuid())).isEqualTo(200);
        assertThat(ler(RECEPCAO, "/api/v1/linhas-de-cuidado/painel")).isEqualTo(403);
    }
}
