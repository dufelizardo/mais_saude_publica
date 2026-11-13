package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Não conformidades com login e autorização ligados (ADR-0204): o núcleo gere na sua unidade; o responsável atualiza a própria ação e
 * a vê em "minhas ações"; outro técnico não; o gestor de outra unidade não registra; o gestor da unidade vê o painel; a recepção não lista.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class NaoConformidadeAutorizacaoControllerTest {

    private static final String PREFIXO = "NaoConfAut Teste ";
    private static final String API = "/api/v1/qualidade/";
    private static final String NSP = "94966867401";
    private static final String TECNICO = "94966867402";
    private static final String TECNICO_OUTRO = "94966867403";
    private static final String GESTOR_OUTRA = "94966867404";
    private static final String GESTOR = "94966867405";
    private static final String RECEPCAO = "94966867406";
    private static final List<String> CPFS = List.of(NSP, TECNICO, TECNICO_OUTRO, GESTOR_OUTRA, GESTOR, RECEPCAO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude hospital;

    @BeforeEach
    void seed() {
        limpar();
        hospital = unidade("Hospital");
        UnidadeDeSaude outra = unidade("Outra");
        Profissional p = new Profissional();
        p.setMatricula("NCA-TEC");
        p.setCpf(TECNICO);
        p.setNome(PREFIXO + "Técnico");
        p.setAtivo(true);
        profissionalRepository.save(p);
        atribuir(NSP, "NUCLEO_SEGURANCA_PACIENTE", hospital);
        atribuir(TECNICO, "TECNICO_DE_ENFERMAGEM", hospital);
        atribuir(TECNICO_OUTRO, "TECNICO_DE_ENFERMAGEM", hospital);
        atribuir(GESTOR_OUTRA, "GESTOR", outra);
        atribuir(GESTOR, "GESTOR", hospital);
        atribuir(RECEPCAO, "RECEPCAO", hospital);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        NaoConformidadeControllerTest.limparDados(jdbc, PREFIXO, "NCA-%");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
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

    private MvcResult como(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false))).andReturn();
    }

    private int status(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return como(cpf, r).getResponse().getStatus();
    }

    private static String corpo(MvcResult r) throws Exception {
        return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    @Test
    void gestaoEResponsavel() throws Exception {
        String nc = "{\"unidadeId\": \"" + hospital.getUuid() + "\", \"origem\": \"RECLAMACAO\", \"descricao\": \"Demora na sala de espera.\", "
                + "\"gravidade\": \"MENOR\", \"responsavelMatricula\": \"NCA-TEC\"}";
        assertThat(status(GESTOR_OUTRA, json(post(API + "nao-conformidade"), nc))).isEqualTo(403);
        assertThat(status(TECNICO, json(post(API + "nao-conformidade"), nc))).isEqualTo(403);
        MvcResult r = como(NSP, json(post(API + "nao-conformidade"), nc));
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        String id = JsonPath.read(corpo(r), "$.uuid");
        r = como(NSP, json(post(API + "nao-conformidade/" + id + "/acao"), "{\"descricao\": \"Reorganizar a escala\", \"justificativa\": \"Fila no pico.\", "
                + "\"responsavelMatricula\": \"NCA-TEC\", \"prazo\": \"" + LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).plusDays(7) + "\"}"));
        String acao = JsonPath.read(corpo(r), "$.acoes[0].uuid");

        // O responsável vê e atualiza a própria ação; o colega, não.
        r = como(TECNICO, get(API + "minhas-acoes"));
        assertThat((List<String>) JsonPath.read(corpo(r), "$[*].acao.uuid")).containsExactly(acao);
        assertThat((List<String>) JsonPath.read(corpo(como(TECNICO_OUTRO, get(API + "minhas-acoes"))), "$[*]")).isEmpty();
        assertThat(status(TECNICO_OUTRO, json(post(API + "acao/" + acao + "/andamento"), "{\"situacao\": \"EM_ANDAMENTO\"}"))).isEqualTo(403);
        assertThat(status(TECNICO, json(post(API + "acao/" + acao + "/andamento"), "{\"situacao\": \"CONCLUIDA\", \"evidencia\": \"Escala nova publicada.\"}")))
                .isEqualTo(200);
        assertThat(status(TECNICO, json(post(API + "acao/" + acao + "/cancelamento"), "{\"motivo\": \"x\"}"))).isEqualTo(403);
        assertThat(status(TECNICO, json(post(API + "nao-conformidade/" + id + "/verificacao"), "{\"eficaz\": true}"))).isEqualTo(403);
        assertThat(status(GESTOR, json(post(API + "nao-conformidade/" + id + "/verificacao"), "{\"eficaz\": true}"))).isEqualTo(200);

        // Listas e painel.
        assertThat(status(RECEPCAO, get(API + "nao-conformidades"))).isEqualTo(403);
        assertThat(status(GESTOR_OUTRA, get(API + "nao-conformidade/" + id))).isEqualTo(403);
        assertThat(status(GESTOR, get(API + "nao-conformidades/painel"))).isEqualTo(200);
        assertThat(mockMvc.perform(get(API + "nao-conformidades")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
