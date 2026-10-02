package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.CatalogoDeAcesso;
import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Papel;
import com.edufelizardo.maissaudepublica.models.Permissao;
import com.edufelizardo.maissaudepublica.models.ProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.PermissaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoReguladoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regulação com login e autorização ligados (ADR-0087): quem solicita responde pela unidade de origem, o
 * regulador enxerga as unidades abaixo do seu escopo, a recepção acompanha sem ver o dado clínico, e a
 * permissão nova entra nos papéis padrão que já existiam. Com o prontuário por vínculo ligado, a unidade
 * executante ganha vínculo com o paciente pela regulação (ADR-0089).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true",
        "app.security.prontuario-por-vinculo.enabled=true"})
class RegulacaoAutorizacaoControllerTest {

    private static final String URL = "/api/v1/solicitacao-regulacao/";
    private static final String PREFIXO = "RegAut Teste ";
    // CPFs com dígito verificador válido, exclusivos desta suíte.
    private static final String MEDICO_A = "47120697030";
    private static final String RECEPCAO_A = "57374853070";
    private static final String REGULADOR_MUN = "86246853004";
    private static final String REGULADOR_OUTRO = "20548817002";
    private static final String MEDICO_POLICLINICA = "52601815906";
    private static final List<String> CPFS = List.of(MEDICO_A, RECEPCAO_A, REGULADOR_MUN, REGULADOR_OUTRO, MEDICO_POLICLINICA);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CatalogoDeAcesso catalogoDeAcesso;

    @Autowired
    private TransactionTemplate transacao;

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
    private ProcedimentoReguladoRepository procedimentoRepository;

    @Autowired
    private SolicitacaoRegulacaoRepository solicitacaoRepository;

    @Autowired
    private EventoRegulacaoRepository eventoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    private UnidadeDeSaude ubsA;
    private UnidadeDeSaude ubsB;
    private UnidadeDeSaude policlinica;
    private Paciente paciente;
    private ProcedimentoRegulado ortopedia;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude municipio = unidade("Município", TipoUnidadeDeSaude.MUNICIPAL, null);
        UnidadeDeSaude outroMunicipio = unidade("Outro município", TipoUnidadeDeSaude.MUNICIPAL, null);
        ubsA = unidade("UBS A", TipoUnidadeDeSaude.UBS, municipio);
        ubsB = unidade("UBS B", TipoUnidadeDeSaude.UBS, outroMunicipio);
        policlinica = unidade("Policlínica", TipoUnidadeDeSaude.POLICLINICA, municipio);

        usuario(MEDICO_A, "MEDICO", ubsA);
        usuario(RECEPCAO_A, "RECEPCAO", ubsA);
        usuario(REGULADOR_MUN, "MEDICO_REGULADOR", municipio);
        usuario(REGULADOR_OUTRO, "MEDICO_REGULADOR", outroMunicipio);
        usuario(MEDICO_POLICLINICA, "MEDICO", policlinica);
        profissional(MEDICO_A, "REGAUT-MED");
        profissional(REGULADOR_MUN, "REGAUT-REG");
        profissional(REGULADOR_OUTRO, "REGAUT-OUT");

        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        ortopedia = procedimentoRepository.save(new ProcedimentoRegulado(PREFIXO + "Ortopedia",
                TipoProcedimentoRegulado.CONSULTA_ESPECIALIZADA, true));
    }

    @AfterEach
    void limpar() {
        Set<UUID> procedimentosDoTeste = procedimentoRepository.findAll().stream()
                .filter(p -> p.getNome().startsWith(PREFIXO)).map(ProcedimentoRegulado::getUuid).collect(Collectors.toSet());
        List<SolicitacaoRegulacao> solicitacoes = solicitacaoRepository.findAll().stream()
                .filter(s -> procedimentosDoTeste.contains(s.getProcedimento().getUuid())).toList();
        for (SolicitacaoRegulacao s : solicitacoes) {
            eventoRepository.deleteAll(eventoRepository.findBySolicitacao_UuidOrderByOcorridoEmAsc(s.getUuid()));
        }
        solicitacaoRepository.deleteAll(solicitacoes);
        // O agendamento criado pela regulação (ADR-0089) sai depois da solicitação que aponta para ele.
        for (Paciente p : pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList()) {
            agendamentoRepository.deleteAll(agendamentoRepository.findByPaciente_UuidOrderByDataHoraAsc(p.getUuid()));
        }
        procedimentoRepository.deleteAll(procedimentoRepository.findAll().stream()
                .filter(p -> p.getNome().startsWith(PREFIXO)).toList());
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("REGAUT-")).toList());
        List<UnidadeDeSaude> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList();
        // As de baixo antes das de cima.
        unidadeDeSaudeRepository.deleteAll(unidades.stream().filter(u -> u.getUnidadeSuperior() != null).toList());
        unidadeDeSaudeRepository.deleteAll(unidades.stream().filter(u -> u.getUnidadeSuperior() == null).toList());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo, UnidadeDeSaude superior) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setUnidadeSuperior(superior);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void usuario(String cpf, String papel, UnidadeDeSaude unidade) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(unidade);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private void profissional(String cpf, String matricula) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder requisicao) {
        return requisicao.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private MockHttpServletRequestBuilder solicitacao(UnidadeDeSaude unidade) {
        return post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "procedimentoId": "%s", "unidadeSolicitanteId": "%s", "profissionalMatricula": "REGAUT-MED",
                 "cid": "M17.1", "justificativa": "Gonartrose com dor refratária ao tratamento clínico.", "prioridade": "VERDE"}
                """.formatted(paciente.getUuid(), ortopedia.getUuid(), unidade.getUuid()));
    }

    private MockHttpServletRequestBuilder autorizacao(UUID uuid, String matricula) {
        return post(URL + uuid + "/autorizacao").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "%s", "unidadeExecutanteId": "%s", "dataHoraPrevista": "%s"}
                """.formatted(matricula, policlinica.getUuid(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusDays(5).withNano(0))));
    }

    private UUID solicitadaNaUbsA() throws Exception {
        mockMvc.perform(como(MEDICO_A, solicitacao(ubsA))).andExpect(status().isCreated());
        return solicitacaoRepository.findByPaciente_Uuid(paciente.getUuid()).get(0).getUuid();
    }

    // ── Escopo ─────────────────────────────────────────────────────────────────────────────────

    @Test
    void medicoSolicitaNaPropriaUnidadeENaoNaDeOutroMunicipio() throws Exception {
        mockMvc.perform(como(MEDICO_A, solicitacao(ubsB))).andExpect(status().isForbidden());
        mockMvc.perform(como(MEDICO_A, solicitacao(ubsA))).andExpect(status().isCreated());
        mockMvc.perform(como(RECEPCAO_A, solicitacao(ubsA))).andExpect(status().isForbidden());
    }

    @Test
    void reguladorDoMunicipioVeEAutorizaEODeOutroMunicipioNao() throws Exception {
        UUID uuid = solicitadaNaUbsA();

        mockMvc.perform(como(REGULADOR_OUTRO, get(URL + "fila?procedimentoId=" + ortopedia.getUuid())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(como(REGULADOR_OUTRO, autorizacao(uuid, "REGAUT-OUT"))).andExpect(status().isForbidden());
        mockMvc.perform(como(REGULADOR_OUTRO, get(URL + uuid))).andExpect(status().isForbidden());

        mockMvc.perform(como(REGULADOR_MUN, get(URL + "fila?procedimentoId=" + ortopedia.getUuid())))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].uuid").value(uuid.toString()));
        mockMvc.perform(como(REGULADOR_MUN, get(URL + uuid)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cid").value("M171"));
        mockMvc.perform(como(REGULADOR_MUN, autorizacao(uuid, "REGAUT-REG"))).andExpect(status().isOk());

        // O médico solicitante não regula: a rota exige REGULACAO.REGULAR.
        mockMvc.perform(como(MEDICO_A, get(URL + "fila"))).andExpect(status().isForbidden());
    }

    @Test
    void recepcaoAcompanhaOAndamentoSemVerODadoClinico() throws Exception {
        UUID uuid = solicitadaNaUbsA();
        mockMvc.perform(como(RECEPCAO_A, get(URL + "?pacienteId=" + paciente.getUuid())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("SOLICITADA"))
                .andExpect(jsonPath("$[0].posicaoNaFila").value(1))
                .andExpect(jsonPath("$[0].cid").doesNotExist());
        mockMvc.perform(como(RECEPCAO_A, get(URL + uuid))).andExpect(status().isForbidden());
    }

    // ── Vínculo assistencial pela regulação (ADR-0089) ─────────────────────────────────────────

    @Test
    void unidadeExecutanteGanhaVinculoComOPacienteQuandoAutorizada() throws Exception {
        UUID uuid = solicitadaNaUbsA();
        mockMvc.perform(como(MEDICO_POLICLINICA, get("/api/v1/prontuario/" + paciente.getUuid())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.details").value("VINCULO_ASSISTENCIAL_AUSENTE"));

        mockMvc.perform(como(REGULADOR_MUN, autorizacao(uuid, "REGAUT-REG"))).andExpect(status().isOk());

        mockMvc.perform(como(MEDICO_POLICLINICA, get("/api/v1/prontuario/" + paciente.getUuid())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acesso.base").value("VINCULO"))
                .andExpect(jsonPath("$.acesso.descricao").value(org.hamcrest.Matchers.startsWith("Regulação: unidade executante")));
    }

    // ── Catálogo de acesso ─────────────────────────────────────────────────────────────────────

    @Test
    void permissaoNovaEntraNoPapelPadraoExistenteSoNaCriacao() {
        // Simula um ambiente semeado antes da regulação: o papel Médico existe, a permissão ainda não.
        transacao.executeWithoutResult(t -> {
            Permissao solicitar = permissaoRepository.findByCodigo("REGULACAO.SOLICITAR").orElseThrow();
            for (Papel papel : papelRepository.findAll()) {
                if (papel.getPermissoes().removeIf(p -> p.getCodigo().equals("REGULACAO.SOLICITAR"))) {
                    papelRepository.save(papel);
                }
            }
            permissaoRepository.delete(solicitar);
        });

        catalogoDeAcesso.run(null);
        assertThat(codigosDoMedico()).contains("REGULACAO.SOLICITAR");

        // Depois de criada, o que a administração tira do papel continua tirado.
        transacao.executeWithoutResult(t -> {
            Papel medico = papelRepository.findByCodigo("MEDICO").orElseThrow();
            medico.getPermissoes().removeIf(p -> p.getCodigo().equals("REGULACAO.SOLICITAR"));
            papelRepository.save(medico);
        });
        catalogoDeAcesso.run(null);
        assertThat(codigosDoMedico()).doesNotContain("REGULACAO.SOLICITAR");

        // Devolve o estado padrão para as outras suítes.
        transacao.executeWithoutResult(t -> {
            Papel medico = papelRepository.findByCodigo("MEDICO").orElseThrow();
            medico.getPermissoes().add(permissaoRepository.findByCodigo("REGULACAO.SOLICITAR").orElseThrow());
            papelRepository.save(medico);
        });
    }

    private Set<String> codigosDoMedico() {
        return transacao.execute(t -> papelRepository.findByCodigo("MEDICO").orElseThrow().getPermissoes().stream()
                .map(Permissao::getCodigo).collect(Collectors.toSet()));
    }
}
