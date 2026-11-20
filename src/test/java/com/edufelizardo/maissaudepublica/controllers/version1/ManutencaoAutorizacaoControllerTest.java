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
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Manutenção com login e autorização ligados (ADR-0216): qualquer pessoa logada abre o chamado e o acompanha, sem triar nem ver as ordens;
 * só a manutenção da unidade do bem executa e mantém os planos; a de outra unidade não vê nem executa; sem login, 401.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class ManutencaoAutorizacaoControllerTest {

    private static final String PREFIXO = "ManutencaoAut Teste ";
    private static final String API = "/api/v1/patrimonio/";
    private static final String GESTOR = "94866767401";
    private static final String MANUTENCAO = "94866767402";
    private static final String MANUTENCAO_OUTRA = "94866767403";
    private static final String OUTRO = "94866767404";
    private static final List<String> CPFS = List.of(GESTOR, MANUTENCAO, MANUTENCAO_OUTRA, OUTRO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude ubs2;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS Centro");
        ubs2 = unidade("UBS Norte");
        profissional("MTA-RESP", GESTOR);
        atribuir(GESTOR, "GESTOR", ubs);
        atribuir(MANUTENCAO, "MANUTENCAO", ubs);
        atribuir(MANUTENCAO_OUTRA, "MANUTENCAO", ubs2);
        atribuir(OUTRO, "RECEPCAO", ubs);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        PatrimonioControllerTest.limparDados(jdbc, PREFIXO);
        jdbc.update("delete from tb_profissional where matricula like 'MTA-%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void profissional(String matricula, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        profissionalRepository.save(p);
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

    private MvcResult como(String cpf, MockHttpServletRequestBuilder r, String corpo) throws Exception {
        return mockMvc.perform(r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false))
                .contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn();
    }

    private int codigo(String cpf, MockHttpServletRequestBuilder r, String corpo) throws Exception {
        return como(cpf, r, corpo).getResponse().getStatus();
    }

    private static String corpo(MvcResult r) throws Exception {
        return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String bem(UnidadeDeSaude u) {
        return "{\"descricao\": \"Mesa de escritório\", \"classe\": \"MOBILIARIO\", \"unidadeId\": \"" + u.getUuid() + "\", \"origem\": \"DOACAO\", "
                + "\"dataAquisicao\": \"" + hoje + "\", \"responsavelMatricula\": \"MTA-RESP\"}";
    }

    @Test
    void chamadoPorQualquerUmExecucaoPelaManutencaoDaUnidade() throws Exception {
        MvcResult r = como(GESTOR, post(API + "bem"), bem(ubs));
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(201);
        String bem = JsonPath.read(corpo(r), "$.bem.uuid");

        // Qualquer pessoa logada abre o chamado e acompanha o próprio.
        r = como(OUTRO, post(API + "bem/" + bem + "/chamado"), "{\"defeito\": \"Gaveta emperrada.\"}");
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(201);
        String os = JsonPath.read(corpo(r), "$.uuid");
        assertThat((List<?>) JsonPath.read(corpo(como(OUTRO, get(API + "meus-chamados"), "")), "$[?(@.uuid == '" + os + "')]")).hasSize(1);
        assertThat(codigo(OUTRO, get(API + "ordem-servico/" + os), "")).isEqualTo(200);
        assertThat(codigo(OUTRO, post(API + "ordem-servico/" + os + "/triagem"), "{\"prioridade\": \"ALTA\"}")).isEqualTo(403);
        assertThat(codigo(OUTRO, get(API + "ordens-servico"), "")).isEqualTo(403);
        assertThat(codigo(OUTRO, get(API + "bem/" + bem + "/manutencao"), "")).isEqualTo(403);

        // Só a manutenção da unidade do bem executa.
        String inicio = "{\"execucao\": \"INTERNA\", \"tecnico\": \"Marcenaria\"}";
        assertThat(codigo(MANUTENCAO_OUTRA, post(API + "ordem-servico/" + os + "/inicio"), inicio)).isEqualTo(403);
        assertThat(codigo(MANUTENCAO_OUTRA, get(API + "ordem-servico/" + os), "")).isEqualTo(403);
        r = como(MANUTENCAO, post(API + "ordem-servico/" + os + "/inicio"), inicio);
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(200);
        assertThat(codigo(MANUTENCAO, get(API + "bem/" + bem + "/manutencao"), "")).isEqualTo(200);
        assertThat(codigo(GESTOR, get(API + "bem/" + bem + "/manutencao"), "")).isEqualTo(200);
        assertThat((List<?>) JsonPath.read(corpo(como(MANUTENCAO_OUTRA, get(API + "ordens-servico"), "")), "$[?(@.uuid == '" + os + "')]")).isEmpty();
        assertThat(codigo(MANUTENCAO, post(API + "bem/" + bem + "/plano"), "{\"tipo\": \"PREVENTIVA\", \"periodicidadeDias\": 90}")).isEqualTo(201);
        assertThat(codigo(OUTRO, post(API + "bem/" + bem + "/plano"), "{\"tipo\": \"CALIBRACAO\", \"periodicidadeDias\": 90}")).isEqualTo(403);
        assertThat(mockMvc.perform(get(API + "meus-chamados")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
