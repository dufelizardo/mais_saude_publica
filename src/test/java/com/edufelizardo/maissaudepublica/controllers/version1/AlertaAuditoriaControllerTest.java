package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Papel;
import com.edufelizardo.maissaudepublica.models.Permissao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.SeveridadeAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AlertaAuditoriaRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.PermissaoRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Alertas da auditoria com login e autorização ligados (ADR-0096): o auditor da UBS A vê e analisa os alertas da UBS A;
 * o da UBS B, não; alerta sem unidade fica para a rede inteira; ninguém analisa o próprio alerta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class AlertaAuditoriaControllerTest {

    private static final String URL = "/api/v1/auditoria/alerta/";
    private static final String PREFIXO = "AlertaApi Teste ";
    private static final String PAPEL = "ALERTA_TESTE_AUDITOR";
    private static final String AUDITOR_A = "90200000001";
    private static final String AUDITOR_B = "90200000002";
    private static final String ALERTADO = "90200000003";
    private static final List<String> CPFS = List.of(AUDITOR_A, AUDITOR_B, ALERTADO);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AlertaAuditoriaRepository alertaRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private PermissaoRepository permissaoRepository;

    private UnidadeDeSaude ubsA;
    private AlertaAuditoria daUbsA;
    private AlertaAuditoria sobreOAuditor;
    private AlertaAuditoria semUnidade;

    @BeforeEach
    void seed() {
        limpar();
        ubsA = unidade("UBS A");
        UnidadeDeSaude ubsB = unidade("UBS B");
        Permissao auditar = permissaoRepository.findByCodigo("AUDITORIA.CONSULTAR").orElseThrow();
        Papel auditor = papelRepository.save(new Papel(PAPEL, "Auditor de teste", null, new HashSet<>(Set.of(auditar))));
        atribuir(AUDITOR_A, auditor, ubsA);
        atribuir(AUDITOR_B, auditor, ubsB);
        usuarioRepository.save(new Usuario(ALERTADO, PREFIXO + ALERTADO, "hash-irrelevante"));
        daUbsA = alerta(ALERTADO, ubsA.getUuid(), "a");
        sobreOAuditor = alerta(AUDITOR_A, ubsA.getUuid(), "b");
        semUnidade = alerta(ALERTADO, null, "c");
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_alerta_auditoria where chave like ?", PREFIXO + "%");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        papelRepository.findByCodigo(PAPEL).ifPresent(papelRepository::delete);
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList());
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void atribuir(String cpf, Papel papel, UnidadeDeSaude unidade) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papel);
        a.setUnidade(unidade);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private AlertaAuditoria alerta(String cpf, UUID unidade, String sufixo) {
        Instant agora = Instant.now();
        AlertaAuditoria a = new AlertaAuditoria();
        a.setTipo(TipoAlertaAuditoria.RECUSAS_SEGUIDAS);
        a.setSeveridade(SeveridadeAlertaAuditoria.ALTA);
        a.setStatus(StatusAlertaAuditoria.ABERTO);
        a.setChave(PREFIXO + sufixo + UUID.randomUUID());
        a.setSujeito(cpf);
        a.setUsuarioCpf(cpf);
        a.setUnidadeId(unidade);
        a.setPrimeiroEventoEm(agora.minusSeconds(600));
        a.setUltimoEventoEm(agora);
        a.setQuantidade(12);
        a.setDescricao("12 acessos recusados em até 15 minutos.");
        a.setDetectadoEm(agora);
        a.setAtualizadoEm(agora);
        return alertaRepository.save(a);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private MockHttpServletRequestBuilder analise(UUID alerta, String conclusao, String parecer) {
        return post(URL + alerta + "/analise").contentType(MediaType.APPLICATION_JSON)
                .content("{\"conclusao\": \"%s\", \"parecer\": \"%s\"}".formatted(conclusao, parecer));
    }

    @Test
    void auditorVeSoOsAlertasDaSuaUnidade() throws Exception {
        mockMvc.perform(como(AUDITOR_A, get(URL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(daUbsA.getUuid())).isNotEmpty())
                .andExpect(jsonPath("$[?(@.uuid == '%s')].usuarioNome".formatted(daUbsA.getUuid())).value(PREFIXO + ALERTADO))
                .andExpect(jsonPath("$[?(@.uuid == '%s')].unidadeNome".formatted(daUbsA.getUuid())).value(ubsA.getNome()))
                .andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(semUnidade.getUuid())).isEmpty());
        mockMvc.perform(como(AUDITOR_B, get(URL)))
                .andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(daUbsA.getUuid())).isEmpty());
        mockMvc.perform(como(AUDITOR_B, get(URL + daUbsA.getUuid()))).andExpect(status().isForbidden());
        mockMvc.perform(como(AUDITOR_A, get(URL + "resumo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abertos").value(2))
                .andExpect(jsonPath("$.abertosAlta").value(2));
        mockMvc.perform(como(ALERTADO, get(URL))).andExpect(status().isForbidden());
    }

    @Test
    void analiseExigeParecerNaoRepeteENaoValeParaOProprioAlerta() throws Exception {
        mockMvc.perform(como(AUDITOR_A, analise(daUbsA.getUuid(), "PROCEDENTE", "curto"))).andExpect(status().isBadRequest());
        mockMvc.perform(como(AUDITOR_A, analise(sobreOAuditor.getUuid(), "IMPROCEDENTE", "Fui eu testando o acesso.")))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(como(AUDITOR_A, analise(daUbsA.getUuid(), "PROCEDENTE", "Tentativas de abrir a farmácia sem permissão.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCEDENTE"))
                .andExpect(jsonPath("$.analisadoPorCpf").value(AUDITOR_A))
                .andExpect(jsonPath("$.analisadoPorNome").value(PREFIXO + AUDITOR_A));
        mockMvc.perform(como(AUDITOR_A, analise(daUbsA.getUuid(), "IMPROCEDENTE", "Mudando de ideia depois.")))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(como(AUDITOR_A, analise(UUID.randomUUID(), "PROCEDENTE", "Alerta que não existe.")))
                .andExpect(status().isNotFound());
        mockMvc.perform(como(AUDITOR_A, get(URL).param("status", "PROCEDENTE")))
                .andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(daUbsA.getUuid())).isNotEmpty())
                .andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(sobreOAuditor.getUuid())).isEmpty());
    }
}
