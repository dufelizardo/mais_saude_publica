package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AcessoJustificado;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.EventoAuditoria;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Papel;
import com.edufelizardo.maissaudepublica.models.Permissao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoAcessoJustificado;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AcessoJustificadoRepository;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoAuditoriaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.PermissaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import com.edufelizardo.maissaudepublica.services.version1.JwtService;
import com.edufelizardo.maissaudepublica.services.version1.VinculoAssistencialService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prontuário por vínculo assistencial (ADR-0076): atendimento na unidade, profissional do atendimento ou do
 * agendamento, acesso justificado com validade, permissão ampla fora dos papéis padrão e toggle próprio.
 * Login, autorização e vínculo ligados só nesta classe.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true",
        "app.security.prontuario-por-vinculo.enabled=true"})
class ProntuarioVinculoControllerTest {

    private static final String PREFIXO = "Vinc Teste ";
    // CPFs com dígito verificador válido, exclusivos desta suíte.
    private static final String ENF_A = "21025748301";
    private static final String ENF_B = "32855765455";
    private static final String MED_B = "30179743830";
    private static final String AUDITOR = "08636970709";
    private static final String OUTRO_A = "26862210700";
    private static final List<String> CPFS = List.of(ENF_A, ENF_B, MED_B, AUDITOR, OUTRO_A);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private VinculoAssistencialService vinculoAssistencialService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private PermissaoRepository permissaoRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private AcessoJustificadoRepository acessoJustificadoRepository;

    @Autowired
    private EventoAuditoriaRepository auditoriaRepository;

    private UnidadeDeSaude ubsA;
    private UnidadeDeSaude ubsB;
    private Paciente paciente;
    private Profissional enfermeiraA;

    @BeforeEach
    void seed() {
        limpar();
        ubsA = unidade("UBS A");
        ubsB = unidade("UBS B");
        usuario(ENF_A, "ENFERMEIRO", ubsA);
        usuario(ENF_B, "ENFERMEIRO", ubsB);
        usuario(MED_B, "MEDICO", ubsB);
        usuario(OUTRO_A, "ENFERMEIRO", ubsA);
        usuario(AUDITOR, null, null);

        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        enfermeiraA = profissional(ENF_A, "VINC-ENF-A");
        profissional(MED_B, "VINC-MED-B");
    }

    @AfterEach
    void limpar() {
        List<Paciente> pacientes = pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList();
        for (Paciente p : pacientes) {
            acessoJustificadoRepository.deleteAll(acessoJustificadoRepository.findByPaciente_Uuid(p.getUuid()));
            atendimentoRepository.deleteAll(atendimentoRepository.findByPacienteUuid(p.getUuid()));
            agendamentoRepository.deleteAll(agendamentoRepository.findByPaciente_UuidOrderByDataHoraAsc(p.getUuid()));
        }
        pacienteRepository.deleteAll(pacientes);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
        papelRepository.findByCodigo("VINC_AUDITOR").ifPresent(papelRepository::delete);
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("VINC-")).toList());
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome().startsWith(PREFIXO)).toList());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void usuario(String cpf, String papel, UnidadeDeSaude unidade) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        if (papel != null) {
            atribuir(usuario, papelRepository.findByCodigo(papel).orElseThrow(), unidade);
        }
    }

    private void atribuir(Usuario usuario, Papel papel, UnidadeDeSaude unidade) {
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papel);
        a.setUnidade(unidade);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private Profissional profissional(String cpf, String matricula) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private Atendimento atendimento(UnidadeDeSaude unidade, StatusAtendimento status, LocalDateTime quando) {
        return atendimentoRepository.save(new Atendimento(paciente, enfermeiraA, unidade, null, null,
                TipoAtendimento.URGENCIA, status, quando));
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder requisicao) {
        return requisicao.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private MockHttpServletRequestBuilder prontuario() {
        return get("/api/v1/prontuario/" + paciente.getUuid());
    }

    private MockHttpServletRequestBuilder justificar(String motivo, String texto) {
        return post("/api/v1/prontuario/" + paciente.getUuid() + "/acesso-justificado")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"motivo": "%s", "justificativa": "%s"}
                        """.formatted(motivo, texto));
    }

    private List<EventoAuditoria> eventosDe(String cpf, Instant desde) {
        return auditoriaRepository.findByUsuarioCpfAndOcorridoEmGreaterThanEqualOrderByOcorridoEmAsc(cpf, desde);
    }

    // ── Vínculo ────────────────────────────────────────────────────────────────────────────────

    @Test
    void atendimentoEmAbertoNaUnidadeDaVinculoEOutraUnidadeNao() throws Exception {
        atendimento(ubsA, StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now());
        Instant inicio = Instant.now().minus(1, ChronoUnit.SECONDS);

        mockMvc.perform(como(OUTRO_A, prontuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acesso.base").value("VINCULO"))
                .andExpect(jsonPath("$.atendimentos.length()").value(1));

        mockMvc.perform(como(ENF_B, prontuario()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.details").value("VINCULO_ASSISTENCIAL_AUSENTE"));

        EventoAuditoria negado = eventosDe(ENF_B, inicio).get(0);
        assertThat(negado.getAcao()).isEqualTo(AcaoAuditoria.LEITURA);
        assertThat(negado.getResultado()).isEqualTo(ResultadoAuditoria.NEGADO);
        assertThat(negado.getPacienteId()).isEqualTo(paciente.getUuid());

        EventoAuditoria permitido = eventosDe(OUTRO_A, inicio).get(0);
        assertThat(permitido.getResultado()).isEqualTo(ResultadoAuditoria.PERMITIDO);
        assertThat(permitido.getDetalhe()).contains("Atendimento na unidade");
    }

    @Test
    void atendimentoConcluidoValeSoDentroDaJanela() throws Exception {
        Atendimento antigo = atendimento(ubsA, StatusAtendimento.CONCLUIDO, LocalDateTime.now().minusDays(60));
        mockMvc.perform(como(OUTRO_A, prontuario())).andExpect(status().isForbidden());

        atendimentoRepository.delete(antigo);
        atendimento(ubsA, StatusAtendimento.CONCLUIDO, LocalDateTime.now().minusDays(10));
        mockMvc.perform(como(OUTRO_A, prontuario())).andExpect(status().isOk());
    }

    @Test
    void profissionalDoAtendimentoTemVinculoMesmoForaDaUnidade() throws Exception {
        // Atendimento feito pela enfermeira A na UBS B: ela não tem acesso na UBS B, mas é a profissional.
        atendimento(ubsB, StatusAtendimento.CONCLUIDO, LocalDateTime.now().minusDays(3));

        mockMvc.perform(como(ENF_A, prontuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acesso.descricao").value(org.hamcrest.Matchers.startsWith("Profissional do atendimento")));
        mockMvc.perform(como(OUTRO_A, prontuario())).andExpect(status().isForbidden());
    }

    @Test
    void medicoDoAgendamentoProximoTemVinculoECanceladoNao() throws Exception {
        Profissional medico = profissionalRepository.findAll().stream()
                .filter(p -> "VINC-MED-B".equals(p.getMatricula())).findFirst().orElseThrow();
        Agendamento cancelado = agendamentoRepository.save(new Agendamento(paciente, medico, LocalDateTime.now().plusDays(5),
                StatusAgendamento.CANCELADO, TipoAgendamento.CONSULTA, null));
        mockMvc.perform(como(MED_B, prontuario())).andExpect(status().isForbidden());

        agendamentoRepository.delete(cancelado);
        agendamentoRepository.save(new Agendamento(paciente, medico, LocalDateTime.now().plusDays(5),
                StatusAgendamento.AGENDADO, TipoAgendamento.CONSULTA, null));
        mockMvc.perform(como(MED_B, prontuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acesso.base").value("VINCULO"));
    }

    // ── Acesso justificado ─────────────────────────────────────────────────────────────────────

    @Test
    void acessoJustificadoLiberaPorAlgumasHorasEEntraNaAuditoriaSemOTexto() throws Exception {
        Instant inicio = Instant.now().minus(1, ChronoUnit.SECONDS);
        String texto = "Paciente chegou desacordado na UPA, preciso do histórico de alergias";

        mockMvc.perform(como(ENF_B, justificar("EMERGENCIA", "curto"))).andExpect(status().isBadRequest());
        mockMvc.perform(como(ENF_B, justificar("EMERGENCIA", texto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid", notNullValue()))
                .andExpect(jsonPath("$.motivo").value("EMERGENCIA"))
                .andExpect(jsonPath("$.expiraEm", notNullValue()));

        mockMvc.perform(como(ENF_B, prontuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acesso.base").value("JUSTIFICADO"))
                .andExpect(jsonPath("$.acesso.expiraEm", notNullValue()));
        // Vale só para quem justificou.
        mockMvc.perform(como(MED_B, prontuario())).andExpect(status().isForbidden());

        AcessoJustificado registrado = acessoJustificadoRepository.findByPaciente_Uuid(paciente.getUuid()).get(0);
        assertThat(registrado.getJustificativa()).isEqualTo(texto);
        assertThat(registrado.getExpiraEm()).isBetween(Instant.now().plus(3, ChronoUnit.HOURS), Instant.now().plus(5, ChronoUnit.HOURS));

        EventoAuditoria evento = eventosDe(ENF_B, inicio).stream()
                .filter(e -> e.getAcao() == AcaoAuditoria.ACESSO_JUSTIFICADO).findFirst().orElseThrow();
        assertThat(evento.getResultado()).isEqualTo(ResultadoAuditoria.PERMITIDO);
        assertThat(evento.getRegistroId()).isEqualTo(registrado.getUuid());
        assertThat(evento.getPacienteId()).isEqualTo(paciente.getUuid());
        assertThat(evento.getDetalhe()).contains("EMERGENCIA").doesNotContain("alergias");
    }

    @Test
    void acessoJustificadoVencidoNaoVale() throws Exception {
        AcessoJustificado vencido = new AcessoJustificado();
        vencido.setUsuarioCpf(ENF_B);
        vencido.setPaciente(paciente);
        vencido.setMotivo(MotivoAcessoJustificado.CONTINUIDADE_DO_CUIDADO);
        vencido.setJustificativa("Acesso de ontem para continuidade do cuidado");
        vencido.setConcedidoEm(Instant.now().minus(1, ChronoUnit.DAYS));
        vencido.setExpiraEm(Instant.now().minus(20, ChronoUnit.HOURS));
        acessoJustificadoRepository.save(vencido);

        mockMvc.perform(como(ENF_B, prontuario())).andExpect(status().isForbidden());
    }

    // ── Permissão ampla e toggle ───────────────────────────────────────────────────────────────

    @Test
    void permissaoAmplaDispensaOVinculoENaoEstaEmPapelPadrao() throws Exception {
        assertThat(papelRepository.findAll().stream()
                .filter(p -> !p.getCodigo().startsWith("VINC_"))
                .flatMap(p -> p.getPermissoes().stream())
                .map(Permissao::getCodigo))
                .doesNotContain(VinculoAssistencialService.SEM_VINCULO);

        Set<Permissao> permissoes = new HashSet<>(permissaoRepository.findByCodigoIn(
                List.of(VinculoAssistencialService.CONSULTAR, VinculoAssistencialService.SEM_VINCULO)));
        Papel auditor = papelRepository.save(new Papel("VINC_AUDITOR", "Auditor clínico de teste", null, permissoes));
        atribuir(usuarioRepository.findByCpf(AUDITOR).orElseThrow(), auditor, null);

        mockMvc.perform(como(AUDITOR, prontuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acesso.base").value("PERMISSAO_AMPLA"));
    }

    @Test
    void comOToggleDesligadoOProntuarioSegueAbertoARede() throws Exception {
        ReflectionTestUtils.setField(vinculoAssistencialService, "ligado", false);
        try {
            mockMvc.perform(como(ENF_B, prontuario()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.acesso.base").value("LIVRE"));
        } finally {
            ReflectionTestUtils.setField(vinculoAssistencialService, "ligado", true);
        }
    }

    @Test
    void justificarPacienteInexistenteDa404() throws Exception {
        mockMvc.perform(como(ENF_B, post("/api/v1/prontuario/" + UUID.randomUUID() + "/acesso-justificado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo": "OUTRO", "justificativa": "Justificativa longa o bastante para passar"}
                                """)))
                .andExpect(status().isNotFound());
    }
}
