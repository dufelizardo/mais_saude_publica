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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * CAPS e RAPS com login e autorização ligados (ADR-0153): a RAPS é de quem acompanha em saúde mental ou consulta
 * indicadores; o acolhimento noturno, de quem acompanha na unidade; as vagas, da administração da rede.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class RapsAutorizacaoControllerTest {

    private static final String PREFIXO = "RapsAut Teste ";
    private static final String PSICOLOGO = "94901867380";
    private static final String RECEPCAO = "94901867381";
    private static final String GESTOR = "94901867382";
    private static final String ADMIN = "94901867383";
    private static final List<String> CPFS = List.of(PSICOLOGO, RECEPCAO, GESTOR, ADMIN);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude caps;
    private UnidadeDeSaude outra;

    @BeforeEach
    void seed() {
        limpar();
        caps = unidade("CAPS");
        outra = unidade("Outra");
        jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_capacidade_assistencial where codigo = 'ACOLHIMENTO_NOTURNO'", caps.getUuid(), LocalDate.now().minusDays(1));
        atribuir(PSICOLOGO, "PSICOLOGO", caps);
        atribuir(RECEPCAO, "RECEPCAO", caps);
        atribuir(GESTOR, "GESTOR", caps);
        atribuir(ADMIN, "ADMINISTRADOR_PLATAFORMA", caps);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_configuracao_acolhimento_noturno where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
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
        u.setTipo(TipoUnidadeDeSaude.CAPS);
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

    private int status(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(como(cpf, r)).andReturn().getResponse().getStatus();
    }

    @Test
    void rapsAcolhimentoEVagasPorPapel() throws Exception {
        // A RAPS: quem acompanha em saúde mental e quem consulta indicadores; a recepção não.
        assertThat(status(PSICOLOGO, get("/api/v1/saude-mental/raps"))).isEqualTo(200);
        assertThat(status(GESTOR, get("/api/v1/saude-mental/raps"))).isEqualTo(200);
        assertThat(status(RECEPCAO, get("/api/v1/saude-mental/raps"))).isEqualTo(403);

        // O acolhimento noturno: quem acompanha, na própria unidade.
        String url = "/api/v1/saude-mental/acolhimento-noturno?unidadeId=";
        assertThat(status(PSICOLOGO, get(url + caps.getUuid()))).isEqualTo(200);
        assertThat(status(PSICOLOGO, get(url + outra.getUuid()))).isEqualTo(403);
        assertThat(status(GESTOR, get(url + caps.getUuid()))).isEqualTo(403);

        // As vagas: a administração da rede, que liga as capacidades.
        String config = "{\"vagas\": 3, \"permanenciaMaximaDias\": 14}";
        String configurar = "/api/v1/saude-mental/acolhimento-noturno/configuracao?unidadeId=" + caps.getUuid();
        assertThat(status(PSICOLOGO, put(configurar).contentType(MediaType.APPLICATION_JSON).content(config))).isEqualTo(403);
        assertThat(status(ADMIN, put(configurar).contentType(MediaType.APPLICATION_JSON).content(config))).isEqualTo(200);
    }
}
