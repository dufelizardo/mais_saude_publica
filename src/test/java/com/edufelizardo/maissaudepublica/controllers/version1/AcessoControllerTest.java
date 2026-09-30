package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Papel;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import com.edufelizardo.maissaudepublica.services.version1.AutorizacaoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Catálogo de permissões, papéis, atribuições de acesso e o cálculo de autorização por escopo
 * (ADR-0066). Nesta fatia nada é exigido nas rotas; os testes cobrem o modelo e as regras.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcessoControllerTest {

    private static final String PAPEL_URL = "/api/v1/papel/";
    private static final String ATRIBUICAO_URL = "/api/v1/atribuicao-acesso/";
    private static final String CPF_A = "11144477735";
    private static final String CPF_B = "39053344705";
    private static final String CPF_NOVO = "93541134780";
    private static final String PREFIXO_PAPEL = "TESTE_ACESSO_";
    private static final String PREFIXO_UNIDADE = "Teste Acesso ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private AutorizacaoService autorizacaoService;

    private Usuario usuarioA;
    private Usuario usuarioB;
    private UnidadeDeSaude regional;
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outraUbs;

    @BeforeEach
    void seed() {
        limpar();
        usuarioA = usuarioRepository.save(new Usuario(CPF_A, "Usuario Acesso A", "hash-irrelevante"));
        usuarioB = usuarioRepository.save(new Usuario(CPF_B, "Usuario Acesso B", "hash-irrelevante"));
        regional = unidade("Regional", null);
        ubs = unidade("UBS", regional);
        outraUbs = unidade("Outra UBS", null);
    }

    @AfterEach
    void limpar() {
        for (String cpf : new String[]{CPF_A, CPF_B, CPF_NOVO}) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
        papelRepository.findAll().stream()
                .filter(p -> p.getCodigo().startsWith(PREFIXO_PAPEL))
                .forEach(papelRepository::delete);
        // Filhas antes da superior.
        unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome().startsWith(PREFIXO_UNIDADE) && u.getUnidadeSuperior() != null)
                .forEach(unidadeDeSaudeRepository::delete);
        unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome().startsWith(PREFIXO_UNIDADE))
                .forEach(unidadeDeSaudeRepository::delete);
    }

    private UnidadeDeSaude unidade(String nome, UnidadeDeSaude superior) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO_UNIDADE + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        u.setUnidadeSuperior(superior);
        return unidadeDeSaudeRepository.save(u);
    }

    private UUID papelId(String codigo) {
        return papelRepository.findByCodigo(codigo).orElseThrow().getUuid();
    }

    private String corpoAtribuicao(UUID usuarioId, UUID papelId, UUID unidadeId, String inicio, String fim) {
        return """
                {"usuarioId": "%s", "papelId": "%s", "unidadeId": %s, "inicio": %s, "fim": %s}
                """.formatted(usuarioId, papelId,
                unidadeId == null ? "null" : "\"" + unidadeId + "\"",
                inicio == null ? "null" : "\"" + inicio + "\"",
                fim == null ? "null" : "\"" + fim + "\"");
    }

    /** Concede pela API e devolve o id da atribuição vigente criada. */
    private String conceder(UUID usuarioId, String papel, UUID unidadeId) throws Exception {
        UUID papelId = papelId(papel);
        mockMvc.perform(post(ATRIBUICAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtribuicao(usuarioId, papelId, unidadeId, null, null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Acesso concedido com sucesso!"));
        return atribuicaoRepository.findByUsuarioUuid(usuarioId).stream()
                .filter(a -> a.getRevogadoEm() == null && a.getPapel().getUuid().equals(papelId)
                        && java.util.Objects.equals(a.getUnidade() != null ? a.getUnidade().getUuid() : null, unidadeId))
                .findFirst().orElseThrow().getUuid().toString();
    }

    // ── Catálogo ───────────────────────────────────────────────────────────────────────────────

    @Test
    void deveSemearOCatalogoDePermissoes() throws Exception {
        mockMvc.perform(get("/api/v1/permissao/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", hasItems("ACESSO.GERENCIAR", "TRIAGEM.REGISTRAR",
                        "FARMACIA.TRANSFERIR", "REGISTRO_CLINICO.RETIFICAR_DE_OUTROS")));
    }

    @Test
    void tecnicoDeEnfermagemNaoRecebeClassificacaoDeRiscoNemEvolucao() throws Exception {
        mockMvc.perform(get(PAPEL_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo=='TECNICO_DE_ENFERMAGEM')].permissoes[*]",
                        hasItem("MEDICACAO.ADMINISTRAR")))
                .andExpect(jsonPath("$[?(@.codigo=='TECNICO_DE_ENFERMAGEM')].permissoes[*]",
                        not(hasItem("TRIAGEM.REGISTRAR"))))
                .andExpect(jsonPath("$[?(@.codigo=='TECNICO_DE_ENFERMAGEM')].permissoes[*]",
                        not(hasItem("EVOLUCAO.REGISTRAR"))));
    }

    @Test
    void administradorDaPlataformaNaoLeDadoDeSaude() throws Exception {
        mockMvc.perform(get(PAPEL_URL + papelId("ADMINISTRADOR_PLATAFORMA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.padrao").value(true))
                .andExpect(jsonPath("$.permissoes", not(hasItem("PRONTUARIO.CONSULTAR"))))
                .andExpect(jsonPath("$.permissoes", hasItem("ACESSO.GERENCIAR")));
    }

    // ── Papéis ─────────────────────────────────────────────────────────────────────────────────

    @Test
    void deveCriarEAtualizarPapel() throws Exception {
        mockMvc.perform(post(PAPEL_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo": "teste_acesso_agente", "nome": "Agente comunitário",
                                 "permissoes": ["PACIENTE.CONSULTAR", "agendamento.gerenciar"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Papel criado com sucesso!"));
        UUID uuid = papelId("TESTE_ACESSO_AGENTE");

        mockMvc.perform(get(PAPEL_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.padrao").value(false))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.permissoes", hasItems("PACIENTE.CONSULTAR", "AGENDAMENTO.GERENCIAR")));

        mockMvc.perform(patch(PAPEL_URL + uuid).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Agente comunitário de saúde", "ativo": false, "permissoes": ["PACIENTE.CONSULTAR"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Papel atualizado com sucesso!"));

        mockMvc.perform(get(PAPEL_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("TESTE_ACESSO_AGENTE"))
                .andExpect(jsonPath("$.nome").value("Agente comunitário de saúde"))
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.permissoes.length()").value(1));
    }

    @Test
    void deveRecusarPapelComCodigoRepetido() throws Exception {
        mockMvc.perform(post(PAPEL_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo": "ENFERMEIRO", "nome": "Outro", "permissoes": []}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRecusarPermissaoForaDoCatalogo() throws Exception {
        mockMvc.perform(post(PAPEL_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo": "TESTE_ACESSO_X", "nome": "X", "permissoes": ["NAO.EXISTE"]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void naoDeveDesativarOAdministradorDaPlataforma() throws Exception {
        mockMvc.perform(patch(PAPEL_URL + papelId("ADMINISTRADOR_PLATAFORMA")).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Administrador da plataforma", "ativo": false,
                                 "permissoes": ["USUARIO.GERENCIAR", "ACESSO.GERENCIAR", "ORGANIZACAO.GERENCIAR"]}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRetornarNotFoundParaPapelInexistente() throws Exception {
        mockMvc.perform(get(PAPEL_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ── Atribuições ────────────────────────────────────────────────────────────────────────────

    @Test
    void deveConcederAcessoNumaUnidadeEListarPorUsuario() throws Exception {
        conceder(usuarioA.getUuid(), "ENFERMEIRO", ubs.getUuid());

        mockMvc.perform(get(ATRIBUICAO_URL).param("usuarioId", usuarioA.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].papelCodigo").value("ENFERMEIRO"))
                .andExpect(jsonPath("$[0].unidadeUuid").value(ubs.getUuid().toString()))
                .andExpect(jsonPath("$[0].usuarioCpf").value(CPF_A))
                .andExpect(jsonPath("$[0].vigente").value(true));
    }

    @Test
    void deveRecusarAcessoRepetidoNoMesmoEscopo() throws Exception {
        conceder(usuarioA.getUuid(), "ENFERMEIRO", ubs.getUuid());
        mockMvc.perform(post(ATRIBUICAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtribuicao(usuarioA.getUuid(), papelId("ENFERMEIRO"), ubs.getUuid(), null, null)))
                .andExpect(status().isConflict());
        // Outro escopo é outra atribuição.
        conceder(usuarioA.getUuid(), "ENFERMEIRO", outraUbs.getUuid());
    }

    @Test
    void deveRecusarPeriodoInvertido() throws Exception {
        mockMvc.perform(post(ATRIBUICAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtribuicao(usuarioA.getUuid(), papelId("MEDICO"), null, "2026-10-10", "2026-10-01")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRecusarPapelInativo() throws Exception {
        Papel inativo = new Papel(PREFIXO_PAPEL + "INATIVO", "Inativo", null, new java.util.HashSet<>());
        inativo.setAtivo(false);
        inativo = papelRepository.save(inativo);
        mockMvc.perform(post(ATRIBUICAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtribuicao(usuarioA.getUuid(), inativo.getUuid(), null, null, null)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRetornarNotFoundParaUsuarioOuUnidadeInexistente() throws Exception {
        mockMvc.perform(post(ATRIBUICAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtribuicao(UUID.randomUUID(), papelId("MEDICO"), null, null, null)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post(ATRIBUICAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtribuicao(usuarioA.getUuid(), papelId("MEDICO"), UUID.randomUUID(), null, null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRevogarSemApagarERecusarSegundaRevogacao() throws Exception {
        String id = conceder(usuarioA.getUuid(), "FARMACEUTICO", ubs.getUuid());
        String motivo = """
                {"motivo": "Transferido para outra unidade"}
                """;

        mockMvc.perform(post(ATRIBUICAO_URL + id + "/revogacao").contentType(MediaType.APPLICATION_JSON).content(motivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Acesso revogado com sucesso!"));
        mockMvc.perform(get(ATRIBUICAO_URL).param("usuarioId", usuarioA.getUuid().toString()))
                .andExpect(jsonPath("$[0].vigente").value(false))
                .andExpect(jsonPath("$[0].revogadoEm").isNotEmpty())
                .andExpect(jsonPath("$[0].motivoRevogacao").value("Transferido para outra unidade"));

        mockMvc.perform(post(ATRIBUICAO_URL + id + "/revogacao").contentType(MediaType.APPLICATION_JSON).content(motivo))
                .andExpect(status().isUnprocessableEntity());

        // Continua no histórico.
        mockMvc.perform(get(ATRIBUICAO_URL).param("usuarioId", usuarioA.getUuid().toString()))
                .andExpect(jsonPath("$.length()").value(1));
        // E, revogada, pode ser concedida de novo.
        conceder(usuarioA.getUuid(), "FARMACEUTICO", ubs.getUuid());
    }

    @Test
    void deveRecusarRevogacaoSemMotivo() throws Exception {
        String id = conceder(usuarioA.getUuid(), "RECEPCAO", null);
        mockMvc.perform(post(ATRIBUICAO_URL + id + "/revogacao").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarEscoposDeTodosOsNiveis() throws Exception {
        mockMvc.perform(get(ATRIBUICAO_URL + "escopos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome=='Teste Acesso UBS')].unidadeSuperiorUuid").value(regional.getUuid().toString()))
                .andExpect(jsonPath("$[*].uuid", hasItems(regional.getUuid().toString(), outraUbs.getUuid().toString())));
    }

    // ── Cálculo de autorização ─────────────────────────────────────────────────────────────────

    @Test
    void acessoNaUnidadeSuperiorValeParaAsDeBaixoEnaoParaAsOutras() throws Exception {
        conceder(usuarioA.getUuid(), "FARMACEUTICO", regional.getUuid());

        assertThat(autorizacaoService.tem(CPF_A, "FARMACIA.TRANSFERIR", regional.getUuid())).isTrue();
        assertThat(autorizacaoService.tem(CPF_A, "FARMACIA.TRANSFERIR", ubs.getUuid())).isTrue();
        assertThat(autorizacaoService.tem(CPF_A, "FARMACIA.TRANSFERIR", outraUbs.getUuid())).isFalse();
        assertThat(autorizacaoService.tem(CPF_A, "TRIAGEM.REGISTRAR", ubs.getUuid())).isFalse();
    }

    @Test
    void acessoNaUnidadeDeBaixoNaoSobeParaASuperior() throws Exception {
        conceder(usuarioA.getUuid(), "ENFERMEIRO", ubs.getUuid());

        assertThat(autorizacaoService.tem(CPF_A, "TRIAGEM.REGISTRAR", ubs.getUuid())).isTrue();
        assertThat(autorizacaoService.tem(CPF_A, "TRIAGEM.REGISTRAR", regional.getUuid())).isFalse();
        assertThat(autorizacaoService.tem(CPF_A, "TRIAGEM.REGISTRAR")).isTrue();
    }

    @Test
    void acessoSemUnidadeValeParaARedeInteira() throws Exception {
        conceder(usuarioB.getUuid(), "MEDICO", null);

        assertThat(autorizacaoService.tem(CPF_B, "CONSULTA.REGISTRAR", ubs.getUuid())).isTrue();
        assertThat(autorizacaoService.tem(CPF_B, "CONSULTA.REGISTRAR", outraUbs.getUuid())).isTrue();
    }

    @Test
    void naoContaAcessoRevogadoForaDoPeriodoOuDePapelInativo() {
        Papel enfermeiro = papelRepository.findByCodigo("ENFERMEIRO").orElseThrow();
        salvarAtribuicao(usuarioA, enfermeiro, LocalDate.now().plusDays(1), null, false);
        salvarAtribuicao(usuarioA, enfermeiro, LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), false);
        salvarAtribuicao(usuarioA, enfermeiro, null, null, true);
        assertThat(autorizacaoService.tem(CPF_A, "TRIAGEM.REGISTRAR")).isFalse();
        assertThat(autorizacaoService.permissoes(CPF_A)).isEmpty();

        Papel inativo = new Papel(PREFIXO_PAPEL + "DESLIGADO", "Desligado", null,
                new java.util.HashSet<>(enfermeiro.getPermissoes()));
        inativo.setAtivo(false);
        salvarAtribuicao(usuarioB, papelRepository.save(inativo), null, null, false);
        assertThat(autorizacaoService.tem(CPF_B, "TRIAGEM.REGISTRAR")).isFalse();
    }

    private void salvarAtribuicao(Usuario usuario, Papel papel, LocalDate inicio, LocalDate fim, boolean revogada) {
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papel);
        a.setInicio(inicio);
        a.setFim(fim);
        a.setConcedidoEm(Instant.now());
        if (revogada) {
            a.setRevogadoEm(Instant.now());
            a.setMotivoRevogacao("teste");
        }
        atribuicaoRepository.save(a);
    }

    // ── Usuários ───────────────────────────────────────────────────────────────────────────────

    @Test
    void deveCadastrarUsuarioComCpfValidoEUnico() throws Exception {
        mockMvc.perform(post("/api/v1/usuario/").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf": "935.411.347-80", "nome": "Usuario Novo", "senha": "SenhaInicial1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Usuário cadastrado com sucesso!"));
        Usuario novo = usuarioRepository.findByCpf(CPF_NOVO).orElseThrow();
        assertThat(novo.isAtivo()).isTrue();
        assertThat(novo.getSenhaHash()).isNotEqualTo("SenhaInicial1");

        mockMvc.perform(post("/api/v1/usuario/").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf": "93541134780", "nome": "Outro", "senha": "SenhaInicial1"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRecusarCpfInvalidoOuSenhaCurta() throws Exception {
        mockMvc.perform(post("/api/v1/usuario/").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf": "93541134781", "nome": "Digito errado", "senha": "SenhaInicial1"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/usuario/").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf": "11111111111", "nome": "Repetido", "senha": "SenhaInicial1"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/usuario/").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf": "93541134780", "nome": "Senha curta", "senha": "curta"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveDesativarEReativarUsuario() throws Exception {
        mockMvc.perform(patch("/api/v1/usuario/" + usuarioA.getUuid()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Usuario Acesso A Renomeado", "ativo": false}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/usuario/" + usuarioA.getUuid()))
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.nome").value("Usuario Acesso A Renomeado"));
    }

    @Test
    void deveDesbloquearSoQuemEstaBloqueado() throws Exception {
        mockMvc.perform(post("/api/v1/usuario/" + usuarioA.getUuid() + "/desbloqueio"))
                .andExpect(status().isUnprocessableEntity());

        usuarioA.setBloqueadoAte(Instant.now().plusSeconds(600));
        usuarioA.setTentativasFalhas(5);
        usuarioRepository.save(usuarioA);
        mockMvc.perform(get("/api/v1/usuario/" + usuarioA.getUuid())).andExpect(jsonPath("$.bloqueado").value(true));

        mockMvc.perform(post("/api/v1/usuario/" + usuarioA.getUuid() + "/desbloqueio"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/usuario/" + usuarioA.getUuid())).andExpect(jsonPath("$.bloqueado").value(false));
    }

    @Test
    void deveListarUsuariosSemOHashDaSenha() throws Exception {
        mockMvc.perform(get("/api/v1/usuario/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].cpf", hasItems(CPF_A, CPF_B)))
                .andExpect(jsonPath("$[0].senhaHash").doesNotExist());
        mockMvc.perform(get("/api/v1/usuario/" + usuarioA.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Usuario Acesso A"));
        mockMvc.perform(get("/api/v1/usuario/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
