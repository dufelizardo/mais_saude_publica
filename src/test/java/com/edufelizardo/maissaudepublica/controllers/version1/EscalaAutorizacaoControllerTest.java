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

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escalas com login e autorização ligados (ADR-0105): o gestor da unidade monta a escala; o enfermeiro, sem ESCALA.GERENCIAR,
 * não; o gestor de outra unidade não vê nem mexe.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class EscalaAutorizacaoControllerTest {

    private static final String URL = "/api/v1/escala/";
    private static final String PREFIXO = "EscalaAut Teste ";
    private static final String GESTOR_A = "94501867340";
    private static final String GESTOR_B = "94501867341";
    private static final String ENF_A = "94501867342";
    private static final List<String> CPFS = List.of(GESTOR_A, GESTOR_B, ENF_A);

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

    private UnidadeDeSaude ubsA;

    @BeforeEach
    void seed() {
        limpar();
        ubsA = unidade("UBS A");
        UnidadeDeSaude ubsB = unidade("UBS B");
        atribuir(GESTOR_A, "GESTOR", ubsA);
        atribuir(GESTOR_B, "GESTOR", ubsB);
        atribuir(ENF_A, "ENFERMEIRO", ubsA);
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_turno_escala where unidade_id = ?", u);
        }
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
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
    void gestorDaUnidadeMontaEnfermeiroNaoOutroGestorNaoAlcanca() throws Exception {
        LocalDate seg = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        String vaga = """
                {"unidadeId": "%s", "funcao": "MEDICO", "tipo": "MANHA", "data": "%s", "inicio": "07:00", "fim": "13:00"}"""
                .formatted(ubsA.getUuid(), seg);
        mockMvc.perform(como(ENF_A, post(URL + "turno")).contentType(MediaType.APPLICATION_JSON).content(vaga)).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR_B, post(URL + "turno")).contentType(MediaType.APPLICATION_JSON).content(vaga)).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR_A, post(URL + "turno")).contentType(MediaType.APPLICATION_JSON).content(vaga)).andExpect(status().isCreated());

        mockMvc.perform(como(GESTOR_A, get(URL)).param("unidadeId", ubsA.getUuid().toString()).param("semana", seg.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.vagasAbertas").value(1));
        mockMvc.perform(como(GESTOR_B, get(URL)).param("unidadeId", ubsA.getUuid().toString())).andExpect(status().isForbidden());
    }
}
