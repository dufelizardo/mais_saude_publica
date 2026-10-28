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
import com.jayway.jsonpath.JsonPath;
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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Intersetorialidade e SM6 com login e autorização ligados (ADRs 0154 e 0155): o catálogo é lido por quem está logado e
 * mantido por quem tem INTERSETORIAL.GERENCIAR (gestor, assistente social); encaminhar o caso segue o sigilo da Saúde
 * Mental; os indicadores do programa sensível são de quem acompanha.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class IntersetorialidadeAutorizacaoControllerTest {

    private static final String PREFIXO = "IntersetAut Teste ";
    private static final String PSICOLOGO = "94901867390";
    private static final String OUTRO_PSICOLOGO = "94901867391";
    private static final String ASSISTENTE_SOCIAL = "94901867392";
    private static final String RECEPCAO = "94901867393";
    private static final String GESTOR = "94901867394";
    private static final List<String> CPFS = List.of(PSICOLOGO, OUTRO_PSICOLOGO, ASSISTENTE_SOCIAL, RECEPCAO, GESTOR);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude caps;
    private String caso;

    @BeforeEach
    void seed() {
        limpar();
        caps = new UnidadeDeSaude();
        caps.setNome(PREFIXO + "CAPS");
        caps.setTipo(TipoUnidadeDeSaude.CAPS);
        caps.setAtivo(true);
        caps = unidadeDeSaudeRepository.save(caps);
        atribuir(PSICOLOGO, "PSICOLOGO");
        atribuir(OUTRO_PSICOLOGO, "PSICOLOGO");
        atribuir(ASSISTENTE_SOCIAL, "ASSISTENTE_SOCIAL");
        atribuir(RECEPCAO, "RECEPCAO");
        atribuir(GESTOR, "GESTOR");
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90044400001");
        p.setSexo(Sexo.FEMININO);
        p.setAtivo(true);
        p = pacienteRepository.save(p);
        String programa = jdbc.queryForObject("insert into tb_acompanhamento_programatico (uuid, paciente_id, programa_id, unidade_id, inicio, situacao) "
                + "select gen_random_uuid(), ?, uuid, ?, ?, 'ATIVO' from tb_programa where codigo = 'SAUDE_MENTAL' returning cast(uuid as varchar)", String.class,
                p.getUuid(), caps.getUuid(), LocalDate.now().minusDays(10));
        // O psicólogo abriu o caso: o conteúdo é dele; o outro psicólogo da unidade não tem vínculo com o caso.
        caso = jdbc.queryForObject("insert into tb_acompanhamento_saude_mental (uuid, paciente_id, unidade_id, acompanhamento_programatico_id, inicio, "
                + "registrado_por_cpf) values (gen_random_uuid(), ?, ?, cast(? as uuid), ?, ?) returning cast(uuid as varchar)", String.class, p.getUuid(),
                caps.getUuid(), programa, LocalDate.now().minusDays(10), PSICOLOGO);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_encaminhamento_intersetorial where unidade_id in " + unidades);
        jdbc.update("delete from tb_instituicao_externa where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_acompanhamento_saude_mental where unidade_id in " + unidades);
        jdbc.update("delete from tb_acompanhamento_programatico where unidade_id in " + unidades);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private void atribuir(String cpf, String papel) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(caps);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private int status(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(como(cpf, r)).andReturn().getResponse().getStatus();
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    @Test
    void catalogoEncaminhamentoEIndicadoresPorPapel() throws Exception {
        String corpo = "{\"nome\": \"" + PREFIXO + "CREAS\", \"setor\": \"ASSISTENCIA_SOCIAL\", \"tipo\": \"CREAS\"}";
        // O catálogo: a recepção lê, mas não mantém; o assistente social e o gestor mantêm.
        assertThat(status(RECEPCAO, json(post("/api/v1/instituicao-externa"), corpo))).isEqualTo(403);
        assertThat(status(PSICOLOGO, json(post("/api/v1/instituicao-externa"), corpo))).isEqualTo(403);
        String r = mockMvc.perform(como(ASSISTENTE_SOCIAL, json(post("/api/v1/instituicao-externa"), corpo))).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String creas = JsonPath.read(r, "$.uuid");
        assertThat(status(GESTOR, json(put("/api/v1/instituicao-externa/" + creas), corpo))).isEqualTo(200);
        assertThat(status(RECEPCAO, get("/api/v1/instituicoes-externas"))).isEqualTo(200);

        // O encaminhamento: quem escreve no caso; o outro psicólogo da unidade, sem vínculo, não; a recepção, sem permissão.
        String enc = "{\"instituicaoId\": \"" + creas + "\", \"motivo\": \"Violação de direitos\", \"retornoEsperadoEm\": \""
                + LocalDate.now().plusDays(10) + "\"}";
        String url = "/api/v1/saude-mental/acompanhamento/" + caso + "/encaminhamento-intersetorial";
        assertThat(status(RECEPCAO, json(post(url), enc))).isEqualTo(403);
        assertThat(status(OUTRO_PSICOLOGO, json(post(url), enc))).isEqualTo(403);
        assertThat(status(PSICOLOGO, json(post(url), enc))).isEqualTo(201);

        // Os indicadores do programa sensível: quem acompanha; o gestor, sem PROGRAMA.ACOMPANHAR, não.
        String programa = jdbc.queryForObject("select cast(uuid as varchar) from tb_programa where codigo = 'SAUDE_MENTAL'", String.class);
        assertThat(status(PSICOLOGO, get("/api/v1/programa/" + programa + "/indicadores"))).isEqualTo(200);
        assertThat(status(GESTOR, get("/api/v1/programa/" + programa + "/indicadores"))).isEqualTo(403);
    }
}
