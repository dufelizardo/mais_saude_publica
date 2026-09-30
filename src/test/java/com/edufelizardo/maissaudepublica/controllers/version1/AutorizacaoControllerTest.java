package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.config.LiberadoParaAutenticados;
import com.edufelizardo.maissaudepublica.config.RequerPermissao;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Permissao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Triagem;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoFarmaciaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.PermissaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TransferenciaFarmaciaRepository;
import com.edufelizardo.maissaudepublica.repositories.TriagemRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import com.edufelizardo.maissaudepublica.services.version1.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorização aplicada (ADR-0067): permissão por rota, escopo da unidade nos serviços, retificação só
 * pelo autor ou supervisão, recebimento de transferência só no destino e concessão de acesso dentro do
 * próprio escopo. Login e autorização ligados só nesta classe; o resto da suíte roda com os dois desligados.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class AutorizacaoControllerTest {

    private static final String PREFIXO = "Autz Teste ";
    // CPFs com dígito verificador válido, exclusivos desta suíte.
    private static final String ENF_A = "86288366757";
    private static final String ENF2_A = "71428793860";
    private static final String COORD_R = "52613991048";
    private static final String TEC_A = "35316384023";
    private static final String RECEP_A = "68486431000";
    private static final String FARM_A = "15350946056";
    private static final String FARM_B = "46959218083";
    private static final String GESTOR_A = "95265812070";
    private static final String SEM_PAPEL = "30183726077";
    private static final List<String> CPFS = List.of(ENF_A, ENF2_A, COORD_R, TEC_A, RECEP_A, FARM_A, FARM_B, GESTOR_A, SEM_PAPEL);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

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
    private TriagemRepository triagemRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimentacaoFarmaciaRepository movimentacaoRepository;

    @Autowired
    private TransferenciaFarmaciaRepository transferenciaRepository;

    private UnidadeDeSaude regional;
    private UnidadeDeSaude ubsA;
    private UnidadeDeSaude ubsB;
    private Atendimento atendimentoA;
    private Atendimento atendimentoB;
    private Lote loteA;

    @BeforeEach
    void seed() {
        limpar();
        regional = unidade("Regional", null);
        ubsA = unidade("UBS A", regional);
        ubsB = unidade("UBS B", null);

        usuario(ENF_A, "ENFERMEIRO", ubsA);
        usuario(ENF2_A, "ENFERMEIRO", ubsA);
        usuario(COORD_R, "COORDENADOR_DE_ENFERMAGEM", regional);
        usuario(TEC_A, "TECNICO_DE_ENFERMAGEM", ubsA);
        usuario(RECEP_A, "RECEPCAO", ubsA);
        usuario(FARM_A, "FARMACEUTICO", ubsA);
        usuario(FARM_B, "FARMACEUTICO", ubsB);
        usuario(GESTOR_A, "GESTOR", ubsA);
        usuario(SEM_PAPEL, null, null);

        Paciente paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setCpf(SEM_PAPEL);
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        Profissional responsavel = profissional(ENF_A, "AUTZ-ENF-A");
        for (String cpf : List.of(ENF2_A, COORD_R, FARM_A, FARM_B)) {
            profissional(cpf, "AUTZ-" + cpf.substring(0, 5));
        }
        atendimentoA = atendimentoRepository.save(new Atendimento(paciente, responsavel, ubsA, null, null,
                TipoAtendimento.URGENCIA, StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now()));
        atendimentoB = atendimentoRepository.save(new Atendimento(paciente, responsavel, ubsB, null, null,
                TipoAtendimento.URGENCIA, StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now()));

        Medicamento medicamento = new Medicamento();
        medicamento.setNome(PREFIXO + "Dipirona");
        medicamento = medicamentoRepository.save(medicamento);
        loteA = loteRepository.save(new Lote(medicamento, ubsA, "AUTZ-L1", LocalDate.now().plusYears(1), 20));
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        List<Lote> lotes = loteRepository.findAll().stream()
                .filter(l -> unidades.contains(l.getUnidade().getUuid())).toList();
        for (Lote l : lotes) {
            movimentacaoRepository.deleteAll(movimentacaoRepository.findByLote_UuidOrderByRegistradoEmAsc(l.getUuid()));
        }
        Set<UUID> idsLotes = lotes.stream().map(Lote::getUuid).collect(Collectors.toSet());
        transferenciaRepository.deleteAll(transferenciaRepository.findAll().stream()
                .filter(t -> idsLotes.contains(t.getLoteOrigem().getUuid())).toList());
        loteRepository.deleteAll(lotes);
        medicamentoRepository.deleteAll(medicamentoRepository.findAll().stream()
                .filter(m -> m.getNome().startsWith(PREFIXO)).toList());

        List<Atendimento> atendimentos = atendimentoRepository.findAll().stream()
                .filter(a -> unidades.contains(a.getUnidade().getUuid())).toList();
        Set<UUID> idsAtendimentos = atendimentos.stream().map(Atendimento::getUuid).collect(Collectors.toSet());
        // Versões que corrigem antes das corrigidas.
        triagemRepository.findAll().stream()
                .filter(t -> idsAtendimentos.contains(t.getAtendimento().getUuid()))
                .sorted(Comparator.comparing(Triagem::getRegistradoEm, Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                .forEach(triagemRepository::delete);
        atendimentoRepository.deleteAll(atendimentos);
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome().startsWith(PREFIXO)).toList());
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("AUTZ-")).toList());
        List<UnidadeDeSaude> nossas = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome().startsWith(PREFIXO)).toList();
        unidadeDeSaudeRepository.deleteAll(nossas.stream().filter(u -> u.getUnidadeSuperior() != null).toList());
        unidadeDeSaudeRepository.deleteAll(nossas.stream().filter(u -> u.getUnidadeSuperior() == null).toList());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private UnidadeDeSaude unidade(String nome, UnidadeDeSaude superior) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        u.setUnidadeSuperior(superior);
        return unidadeDeSaudeRepository.save(u);
    }

    private void usuario(String cpf, String papel, UnidadeDeSaude unidade) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        if (papel != null) {
            AtribuicaoAcesso a = new AtribuicaoAcesso();
            a.setUsuario(usuario);
            a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
            a.setUnidade(unidade);
            a.setConcedidoEm(Instant.now());
            atribuicaoRepository.save(a);
        }
    }

    private Profissional profissional(String cpf, String matricula) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder requisicao) {
        return requisicao.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private String triagem(UUID atendimentoId, String matricula, String extra) {
        return """
                {"atendimentoId": "%s", "profissionalMatricula": "%s", "dataHora": "2026-09-30T10:00:00",
                 "classificacaoRisco": "AMARELO"%s}
                """.formatted(atendimentoId, matricula, extra);
    }

    private UUID triagemVigenteDe(UUID atendimentoId) {
        List<Triagem> triagens = triagemRepository.findAll().stream()
                .filter(t -> t.getAtendimento().getUuid().equals(atendimentoId)).toList();
        Set<UUID> corrigidas = triagens.stream().filter(t -> t.getRetificacaoDe() != null)
                .map(t -> t.getRetificacaoDe().getUuid()).collect(Collectors.toSet());
        return triagens.stream().map(Triagem::getUuid).filter(id -> !corrigidas.contains(id)).findFirst().orElseThrow();
    }

    // ── Rotas ──────────────────────────────────────────────────────────────────────────────────

    @Test
    void todaRotaDaApiTemPermissaoDefinidaComCodigosDoCatalogo() {
        Set<String> catalogo = permissaoRepository.findAll().stream().map(Permissao::getCodigo).collect(Collectors.toSet());
        List<String> semDefinicao = new ArrayList<>();
        List<String> foraDoCatalogo = new ArrayList<>();
        handlerMapping.getHandlerMethods().forEach((info, metodo) -> {
            boolean daApi = info.getPatternValues().stream().anyMatch(p -> p.startsWith("/api/"));
            if (!daApi) {
                return;
            }
            RequerPermissao requer = metodo.getMethodAnnotation(RequerPermissao.class);
            if (requer == null) {
                requer = AnnotatedElementUtils.findMergedAnnotation(metodo.getBeanType(), RequerPermissao.class);
            }
            boolean liberado = metodo.hasMethodAnnotation(LiberadoParaAutenticados.class)
                    || AnnotatedElementUtils.hasAnnotation(metodo.getBeanType(), LiberadoParaAutenticados.class);
            if (requer == null && !liberado) {
                semDefinicao.add(nome(metodo));
            }
            if (requer != null) {
                for (String p : requer.value()) {
                    if (!catalogo.contains(p)) {
                        foraDoCatalogo.add(nome(metodo) + " → " + p);
                    }
                }
            }
        });
        assertThat(semDefinicao).as("rotas sem @RequerPermissao nem @LiberadoParaAutenticados").isEmpty();
        assertThat(foraDoCatalogo).as("permissões fora do catálogo").isEmpty();
    }

    private static String nome(HandlerMethod metodo) {
        return metodo.getBeanType().getSimpleName() + "." + metodo.getMethod().getName();
    }

    @Test
    void semAPermissaoDaRotaResponde403() throws Exception {
        mockMvc.perform(como(RECEP_A, get("/api/v1/triagem/"))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("PRONTUARIO.CONSULTAR")));
        mockMvc.perform(como(SEM_PAPEL, get("/api/v1/paciente/"))).andExpect(status().isForbidden());
        mockMvc.perform(como(FARM_A, get("/api/v1/papel/"))).andExpect(status().isForbidden());
    }

    @Test
    void estruturaEQuemEstaLogadoSaoLiberadosParaQualquerAutenticado() throws Exception {
        mockMvc.perform(como(SEM_PAPEL, get("/api/v1/unidade-saude/"))).andExpect(status().is(not403()));
        mockMvc.perform(como(SEM_PAPEL, get("/api/v1/auth/eu"))).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/auth/status")).andExpect(jsonPath("$.authorizationEnabled").value(true));
    }

    private static org.hamcrest.Matcher<Integer> not403() {
        return not(403);
    }

    // ── Escopo nos registros clínicos ──────────────────────────────────────────────────────────

    @Test
    void tecnicoDeEnfermagemNaoRegistraClassificacaoDeRisco() throws Exception {
        mockMvc.perform(como(TEC_A, post("/api/v1/triagem/")).contentType(MediaType.APPLICATION_JSON)
                        .content(triagem(atendimentoA.getUuid(), "AUTZ-ENF-A", "")))
                .andExpect(status().isForbidden());
    }

    @Test
    void enfermeiroRegistraSoNaUnidadeDoSeuAcesso() throws Exception {
        mockMvc.perform(como(ENF_A, post("/api/v1/triagem/")).contentType(MediaType.APPLICATION_JSON)
                        .content(triagem(atendimentoA.getUuid(), "AUTZ-ENF-A", "")))
                .andExpect(status().isCreated());
        mockMvc.perform(como(ENF_A, post("/api/v1/triagem/")).contentType(MediaType.APPLICATION_JSON)
                        .content(triagem(atendimentoB.getUuid(), "AUTZ-ENF-A", "")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listaEDetalheDeAtendimentosRespeitamOEscopo() throws Exception {
        mockMvc.perform(como(ENF_A, get("/api/v1/atendimento/")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].uuid", hasItem(atendimentoA.getUuid().toString())))
                .andExpect(jsonPath("$[*].uuid", not(hasItem(atendimentoB.getUuid().toString()))));
        mockMvc.perform(como(ENF_A, get("/api/v1/atendimento/" + atendimentoB.getUuid()))).andExpect(status().isForbidden());
        // Acesso na Regional vale para a UBS de baixo, não para a UBS de fora.
        mockMvc.perform(como(COORD_R, get("/api/v1/atendimento/" + atendimentoA.getUuid()))).andExpect(status().isOk());
        mockMvc.perform(como(COORD_R, get("/api/v1/atendimento/" + atendimentoB.getUuid()))).andExpect(status().isForbidden());
    }

    @Test
    void retificacaoSoPeloAutorOuPelaSupervisao() throws Exception {
        mockMvc.perform(como(ENF_A, post("/api/v1/triagem/")).contentType(MediaType.APPLICATION_JSON)
                        .content(triagem(atendimentoA.getUuid(), "AUTZ-ENF-A", "")))
                .andExpect(status().isCreated());
        UUID original = triagemVigenteDe(atendimentoA.getUuid());
        String motivo = ", \"motivoRetificacao\": \"Pressão digitada errada\"";

        // Outro enfermeiro da mesma unidade não corrige o registro do colega.
        mockMvc.perform(como(ENF2_A, post("/api/v1/triagem/" + original + "/retificacao")).contentType(MediaType.APPLICATION_JSON)
                        .content(triagem(atendimentoA.getUuid(), "AUTZ-71428", motivo)))
                .andExpect(status().isForbidden());
        // O autor corrige.
        mockMvc.perform(como(ENF_A, post("/api/v1/triagem/" + original + "/retificacao")).contentType(MediaType.APPLICATION_JSON)
                        .content(triagem(atendimentoA.getUuid(), "AUTZ-ENF-A", motivo)))
                .andExpect(status().isCreated());
        // A coordenação da Regional corrige a versão vigente, feita por outra pessoa.
        UUID vigente = triagemVigenteDe(atendimentoA.getUuid());
        mockMvc.perform(como(COORD_R, post("/api/v1/triagem/" + vigente + "/retificacao")).contentType(MediaType.APPLICATION_JSON)
                        .content(triagem(atendimentoA.getUuid(), "AUTZ-52613", motivo)))
                .andExpect(status().isCreated());
    }

    // ── Farmácia ───────────────────────────────────────────────────────────────────────────────

    @Test
    void recebimentoDeTransferenciaSoNaUnidadeDeDestino() throws Exception {
        mockMvc.perform(como(FARM_A, post("/api/v1/transferencia-farmacia/")).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loteOrigemId": "%s", "unidadeDestinoId": "%s", "quantidade": 5, "profissionalMatricula": "AUTZ-15350"}
                                """.formatted(loteA.getUuid(), ubsB.getUuid())))
                .andExpect(status().isCreated());
        UUID transferencia = transferenciaRepository.findAll().stream()
                .filter(t -> t.getLoteOrigem().getUuid().equals(loteA.getUuid())).findFirst().orElseThrow().getUuid();
        String recebimento = """
                {"quantidadeRecebida": 5, "profissionalMatricula": "%s"}
                """;

        // Quem enviou (acesso só na origem) não confirma a chegada.
        mockMvc.perform(como(FARM_A, post("/api/v1/transferencia-farmacia/" + transferencia + "/recebimento"))
                        .contentType(MediaType.APPLICATION_JSON).content(recebimento.formatted("AUTZ-71428")))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(FARM_B, post("/api/v1/transferencia-farmacia/" + transferencia + "/recebimento"))
                        .contentType(MediaType.APPLICATION_JSON).content(recebimento.formatted("AUTZ-46959")))
                .andExpect(status().isOk());
    }

    @Test
    void farmaceuticoDeOutraUnidadeNaoDispensaNemVeOLote() throws Exception {
        mockMvc.perform(como(FARM_B, get("/api/v1/lote/" + loteA.getUuid()))).andExpect(status().isForbidden());
        // Sem lote na unidade do acesso, a listagem fica vazia — 404, convenção da listagem de lotes.
        mockMvc.perform(como(FARM_B, get("/api/v1/lote/"))).andExpect(status().isNotFound());
        mockMvc.perform(como(FARM_A, get("/api/v1/lote/")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].uuid", hasItem(loteA.getUuid().toString())));
        mockMvc.perform(como(FARM_A, get("/api/v1/lote/" + loteA.getUuid()))).andExpect(status().isOk());
    }

    // ── Concessão de acesso ────────────────────────────────────────────────────────────────────

    @Test
    void gestorConcedeSoNoSeuEscopoESemPapelDeAdministracao() throws Exception {
        UUID semPapel = usuarioRepository.findByCpf(SEM_PAPEL).orElseThrow().getUuid();
        String corpo = """
                {"usuarioId": "%s", "papelId": "%s", "unidadeId": "%s"}
                """;
        UUID enfermeiro = papelRepository.findByCodigo("ENFERMEIRO").orElseThrow().getUuid();
        UUID administrador = papelRepository.findByCodigo("ADMINISTRADOR_PLATAFORMA").orElseThrow().getUuid();

        mockMvc.perform(como(GESTOR_A, post("/api/v1/atribuicao-acesso/")).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo.formatted(semPapel, enfermeiro, ubsA.getUuid())))
                .andExpect(status().isCreated());
        mockMvc.perform(como(GESTOR_A, post("/api/v1/atribuicao-acesso/")).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo.formatted(semPapel, enfermeiro, ubsB.getUuid())))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR_A, post("/api/v1/atribuicao-acesso/")).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo.formatted(semPapel, administrador, ubsA.getUuid())))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR_A, post("/api/v1/papel/")).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo": "AUTZ_QUALQUER", "nome": "Qualquer", "permissoes": []}
                                """))
                .andExpect(status().isForbidden());

        // O acesso concedido já vale: agora ele registra na UBS A.
        mockMvc.perform(como(SEM_PAPEL, post("/api/v1/triagem/")).contentType(MediaType.APPLICATION_JSON)
                        .content(triagem(atendimentoA.getUuid(), "AUTZ-ENF-A", "")))
                .andExpect(status().isCreated());
    }
}
