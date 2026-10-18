package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.CategoriaPrograma;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ParticipacaoPrograma;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.AbrangenciaPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.CategoriaProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Acompanhamento com login, autorização e prontuário por vínculo ligados (ADR-0119): o enfermeiro da unidade inscreve; o
 * gestor e o enfermeiro de outra unidade não; ler os programas do paciente pede vínculo (ou acesso justificado); programa
 * sensível de outra unidade só conta em restritos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true",
        "app.security.prontuario-por-vinculo.enabled=true"})
class AcompanhamentoAutorizacaoControllerTest {

    private static final String PREFIXO = "AcompAut Teste ";
    private static final String CODIGO = "TESTE_ACA_";
    private static final String ENF_A = "94501867360";
    private static final String ENF_B = "94501867361";
    private static final String GESTOR_A = "94501867362";
    private static final List<String> CPFS = List.of(ENF_A, ENF_B, GESTOR_A);

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

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private CategoriaProgramaRepository categoriaRepository;

    @Autowired
    private ParticipacaoProgramaRepository participacaoRepository;

    private UnidadeDeSaude ubsA;
    private Programa hiperdia;
    private Programa mental;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        ubsA = unidade("UBS A");
        UnidadeDeSaude ubsB = unidade("UBS B");
        atribuir(ENF_A, "ENFERMEIRO", ubsA);
        atribuir(ENF_B, "ENFERMEIRO", ubsB);
        atribuir(GESTOR_A, "GESTOR", ubsA);
        hiperdia = programa("HD", false);
        mental = programa("SM", true);
        participacaoRepository.save(new ParticipacaoPrograma(null, hiperdia, ubsA, null, LocalDate.now().minusYears(1), null, null, null));
        participacaoRepository.save(new ParticipacaoPrograma(null, mental, ubsA, null, LocalDate.now().minusYears(1), null, null, null));
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90033344455");
        p.setAtivo(true);
        paciente = pacienteRepository.save(p);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_evento_acompanhamento where acompanhamento_id in (select a.uuid from tb_acompanhamento_programatico a "
                + "join tb_programa p on p.uuid = a.programa_id where p.codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_acompanhamento_programatico where programa_id in (select uuid from tb_programa where codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_participacao_programa where programa_id in (select uuid from tb_programa where codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_programa where codigo like ?", CODIGO + "%");
        jdbc.update("delete from tb_acesso_justificado where usuario_cpf in (?, ?, ?)", ENF_A, ENF_B, GESTOR_A);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream().filter(x -> x.getNome() != null && x.getNome().startsWith(PREFIXO)).toList());
        unidadeDeSaudeRepository.deleteAllById(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList());
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Programa programa(String sufixo, boolean sensivel) {
        CategoriaPrograma cat = categoriaRepository.findByNomeIgnoreCase("Crônicos").orElseThrow();
        Programa p = new Programa();
        p.setCodigo(CODIGO + sufixo);
        p.setNome(PREFIXO + sufixo);
        p.setCategoria(cat);
        p.setAbrangencia(AbrangenciaPrograma.MUNICIPAL);
        p.setSituacao(SituacaoPrograma.ATIVO);
        p.setPeriodicidade(PeriodicidadeAcompanhamento.MENSAL);
        p.setSensivel(sensivel);
        return programaRepository.save(p);
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

    private String inscricao() {
        return "{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\"}".formatted(paciente.getUuid(), ubsA.getUuid());
    }

    @Test
    void enfermeiroDaUnidadeInscreveGestorEOutraUnidadeNao() throws Exception {
        String url = "/api/v1/programa/" + hiperdia.getUuid() + "/acompanhamento";
        mockMvc.perform(como(GESTOR_A, post(url)).contentType(MediaType.APPLICATION_JSON).content(inscricao())).andExpect(status().isForbidden());
        mockMvc.perform(como(ENF_B, post(url)).contentType(MediaType.APPLICATION_JSON).content(inscricao())).andExpect(status().isForbidden());
        mockMvc.perform(como(ENF_A, post(url)).contentType(MediaType.APPLICATION_JSON).content(inscricao())).andExpect(status().isCreated());
        mockMvc.perform(como(ENF_B, get("/api/v1/programa/" + hiperdia.getUuid() + "/acompanhamentos")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.inscritos").value(0));
        mockMvc.perform(como(ENF_A, get("/api/v1/programa/" + hiperdia.getUuid() + "/acompanhamentos")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.inscritos").value(1));
    }

    @Test
    void lerOsProgramasDoPacientePedeVinculoESensivelDeOutraUnidadeFicaRestrito() throws Exception {
        for (Programa p : List.of(hiperdia, mental)) {
            mockMvc.perform(como(ENF_A, post("/api/v1/programa/" + p.getUuid() + "/acompanhamento")).contentType(MediaType.APPLICATION_JSON)
                    .content(inscricao())).andExpect(status().isCreated());
        }
        String programas = "/api/v1/paciente/" + paciente.getUuid() + "/programas";
        mockMvc.perform(como(ENF_B, get(programas))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.details").value("VINCULO_ASSISTENCIAL_AUSENTE"));
        mockMvc.perform(como(ENF_B, post("/api/v1/prontuario/" + paciente.getUuid() + "/acesso-justificado")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"CONTINUIDADE_DO_CUIDADO\", \"justificativa\": \"Paciente transferido para a UBS B nesta semana.\"}"))
                .andExpect(status().is2xxSuccessful());
        mockMvc.perform(como(ENF_B, get(programas))).andExpect(status().isOk())
                .andExpect(jsonPath("$.acesso.base").value("JUSTIFICADO"))
                .andExpect(jsonPath("$.vigentes.length()").value(1))
                .andExpect(jsonPath("$.vigentes[0].programaCodigo").value(CODIGO + "HD"))
                .andExpect(jsonPath("$.restritos").value(1));
    }
}
