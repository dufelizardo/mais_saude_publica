package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ParticipacaoPrograma;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroEquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Saúde Mental com login, autorização e prontuário por vínculo ligados (ADR-0120): o psicólogo da equipe de referência
 * abre o caso e lê o conteúdo, porque ser da referência conta como vínculo; o colega da mesma unidade com vínculo por
 * atendimento só vê o resumo e não registra, até declarar acesso justificado; o médico de outra unidade não tem vínculo;
 * o gestor não acessa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true",
        "app.security.prontuario-por-vinculo.enabled=true"})
class SaudeMentalAutorizacaoControllerTest {

    private static final String PREFIXO = "SMAut Teste ";
    private static final String MATRICULA = "SMA-";
    private static final String PSI_REF = "94601867360";
    private static final String PSI_OUTRO = "94601867361";
    private static final String MED_B = "94601867362";
    private static final String GESTOR = "94601867363";
    private static final List<String> CPFS = List.of(PSI_REF, PSI_OUTRO, MED_B, GESTOR);

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
    private ParticipacaoProgramaRepository participacaoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private MembroEquipeRepository membroRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    private UnidadeDeSaude caps;
    private Equipe equipe;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        caps = unidade("CAPS");
        UnidadeDeSaude ubsB = unidade("UBS B");
        UnidadeDeSaude ubsC = unidade("UBS C");
        participacaoRepository.save(new ParticipacaoPrograma(null, programaRepository.findByCodigo("SAUDE_MENTAL").orElseThrow(), caps,
                null, LocalDate.now().minusYears(1), null, null, null));
        atribuir(PSI_REF, "PSICOLOGO", caps);
        atribuir(PSI_OUTRO, "PSICOLOGO", caps);
        atribuir(MED_B, "MEDICO", ubsC);
        atribuir(GESTOR, "GESTOR", caps);
        equipe = new Equipe();
        equipe.setUnidade(caps);
        equipe.setTipo(TipoEquipe.EMULTI);
        equipe.setNome("Equipe CAPS");
        equipe.setAtiva(true);
        equipe = equipeRepository.save(equipe);
        Profissional ref = profissional("REF", PSI_REF);
        membroRepository.save(new MembroEquipe(null, equipe, ref, FuncaoEquipe.PSICOLOGO, null, LocalDate.now().minusMonths(6), null, null, null));
        Profissional outro = profissional("OUTRO", PSI_OUTRO);
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90055566677");
        p.setAtivo(true);
        paciente = pacienteRepository.save(p);
        // O colega tem vínculo por um atendimento seu, recente, em outra unidade; não é da equipe de referência.
        atendimentoRepository.save(new Atendimento(paciente, outro, ubsB, null, null, TipoAtendimento.CONSULTA,
                StatusAtendimento.CONCLUIDO, LocalDateTime.now().minusDays(2)));
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            jdbc.update("delete from tb_acesso_justificado where usuario_cpf = ?", cpf);
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        SaudeMentalControllerTest.limparDados(jdbc, PREFIXO, MATRICULA);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Profissional profissional(String sufixo, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(MATRICULA + sufixo);
        p.setCpf(cpf);
        p.setNome(PREFIXO + sufixo);
        p.setAtivo(true);
        return profissionalRepository.save(p);
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

    private String abertura() {
        return "{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"equipeId\": \"%s\", \"tecnicoMatricula\": \"%s\"}"
                .formatted(paciente.getUuid(), caps.getUuid(), equipe.getUuid(), MATRICULA + "REF");
    }

    @Test
    void sigiloPelaEquipeDeReferencia() throws Exception {
        mockMvc.perform(como(GESTOR, post("/api/v1/saude-mental/acompanhamento")).contentType(MediaType.APPLICATION_JSON).content(abertura()))
                .andExpect(status().isForbidden());
        String r = mockMvc.perform(como(PSI_REF, post("/api/v1/saude-mental/acompanhamento")).contentType(MediaType.APPLICATION_JSON)
                .content(abertura())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String caso = "/api/v1/saude-mental/acompanhamento/" + JsonPath.read(r, "$.uuid");
        String avaliacao = "{\"contextoHistoria\": \"Primeira escuta.\", \"necessidades\": \"Acompanhamento quinzenal.\"}";
        mockMvc.perform(como(PSI_REF, post(caso + "/avaliacao")).contentType(MediaType.APPLICATION_JSON).content(avaliacao))
                .andExpect(status().isCreated());

        // A referência lê tudo: ser da equipe de referência é vínculo.
        mockMvc.perform(como(PSI_REF, get(caso))).andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudoLiberado").value(true))
                .andExpect(jsonPath("$.acesso.descricao").value("Técnico de referência em saúde mental"))
                .andExpect(jsonPath("$.avaliacoes.length()").value(1));
        mockMvc.perform(como(PSI_REF, get("/api/v1/saude-mental/acompanhamentos").param("escopo", "TECNICO")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.emAcompanhamento").value(1));

        // O colega com vínculo por atendimento vê que o caso existe e quem é a referência, sem conteúdo, e não registra.
        mockMvc.perform(como(PSI_OUTRO, get(caso))).andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudoLiberado").value(false))
                .andExpect(jsonPath("$.tecnicoMatricula").value(MATRICULA + "REF"))
                .andExpect(jsonPath("$.avaliacoes.length()").value(0));
        mockMvc.perform(como(PSI_OUTRO, get("/api/v1/saude-mental/acompanhamentos").param("escopo", "TECNICO")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.emAcompanhamento").value(0));
        mockMvc.perform(como(PSI_OUTRO, post(caso + "/avaliacao")).contentType(MediaType.APPLICATION_JSON).content(avaliacao))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(PSI_OUTRO, post("/api/v1/prontuario/" + paciente.getUuid() + "/acesso-justificado"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"CONTINUIDADE_DO_CUIDADO\", \"justificativa\": \"Cobrindo as férias da técnica de referência.\"}"))
                .andExpect(status().is2xxSuccessful());
        mockMvc.perform(como(PSI_OUTRO, get(caso))).andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudoLiberado").value(true))
                .andExpect(jsonPath("$.avaliacoes.length()").value(1));

        // Outra unidade, sem vínculo; o gestor nem tem a permissão.
        mockMvc.perform(como(MED_B, get(caso))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.details").value("VINCULO_ASSISTENCIAL_AUSENTE"));
        mockMvc.perform(como(MED_B, get("/api/v1/saude-mental/acompanhamentos")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.emAcompanhamento").value(0));
        mockMvc.perform(como(GESTOR, get(caso))).andExpect(status().isForbidden());
    }

    @Test
    void ptsIntervencoesERedeSoComOConteudoLiberado() throws Exception {
        String r = mockMvc.perform(como(PSI_REF, post("/api/v1/saude-mental/acompanhamento")).contentType(MediaType.APPLICATION_JSON)
                .content(abertura())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String caso = "/api/v1/saude-mental/acompanhamento/" + JsonPath.read(r, "$.uuid");
        String pts = "{\"necessidades\": \"Vínculo e adesão.\", \"objetivos\": [{\"descricao\": \"Vínculo com o CAPS\"}]}";
        String intervencao = "{\"tipoId\": \"%s\", \"resumo\": \"Escuta.\"}".formatted(jdbc.queryForObject(
                "select uuid::text from tb_tipo_intervencao_saude_mental where codigo = 'ORIENTACAO'", String.class));

        // O colega com vínculo, fora da referência, não elabora, não registra e não lê o PTS.
        mockMvc.perform(como(PSI_OUTRO, post(caso + "/pts")).contentType(MediaType.APPLICATION_JSON).content(pts)).andExpect(status().isForbidden());
        mockMvc.perform(como(PSI_OUTRO, post(caso + "/intervencao")).contentType(MediaType.APPLICATION_JSON).content(intervencao))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(PSI_OUTRO, post(caso + "/rede-apoio")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Ana\", \"papel\": \"FAMILIAR\"}")).andExpect(status().isForbidden());

        mockMvc.perform(como(PSI_REF, post(caso + "/pts")).contentType(MediaType.APPLICATION_JSON).content(pts)).andExpect(status().isCreated());
        mockMvc.perform(como(PSI_REF, post(caso + "/intervencao")).contentType(MediaType.APPLICATION_JSON).content(intervencao))
                .andExpect(status().isCreated());
        mockMvc.perform(como(PSI_REF, get(caso + "/pts"))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(como(PSI_OUTRO, get(caso + "/pts"))).andExpect(status().isForbidden());
        // No detalhe, o colega vê que há PTS (a versão), sem o conteúdo.
        mockMvc.perform(como(PSI_OUTRO, get(caso))).andExpect(status().isOk())
                .andExpect(jsonPath("$.ptsVersao").value(1))
                .andExpect(jsonPath("$.pts.length()").value(0))
                .andExpect(jsonPath("$.intervencoes.length()").value(0));
        mockMvc.perform(como(GESTOR, get("/api/v1/saude-mental/tipos-intervencao"))).andExpect(status().isForbidden());
    }

    @Test
    void cuidadoCompartilhadoDaOAcessoETransferenciaPedeOAcessoNoDestino() throws Exception {
        UnidadeDeSaude ubsC = unidadeDeSaudeRepository.findAll().stream().filter(u -> (PREFIXO + "UBS C").equals(u.getNome())).findFirst().orElseThrow();
        participacaoRepository.save(new ParticipacaoPrograma(null, programaRepository.findByCodigo("SAUDE_MENTAL").orElseThrow(), ubsC,
                null, LocalDate.now().minusYears(1), null, null, null));
        Equipe esf = new Equipe();
        esf.setUnidade(ubsC);
        esf.setTipo(TipoEquipe.ESF);
        esf.setNome("eSF C");
        esf.setAtiva(true);
        esf = equipeRepository.save(esf);
        membroRepository.save(new MembroEquipe(null, esf, profissional("MEDB", MED_B), FuncaoEquipe.MEDICO, null, LocalDate.now().minusMonths(6),
                null, null, null));
        String r = mockMvc.perform(como(PSI_REF, post("/api/v1/saude-mental/acompanhamento")).contentType(MediaType.APPLICATION_JSON)
                .content(abertura())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String caso = "/api/v1/saude-mental/acompanhamento/" + JsonPath.read(r, "$.uuid");

        // Sem compartilhamento, o médico da eSF de outra unidade não tem vínculo.
        mockMvc.perform(como(MED_B, get(caso))).andExpect(status().isForbidden());
        String c = mockMvc.perform(como(PSI_REF, post(caso + "/compartilhamento")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equipeId\": \"%s\", \"objetivo\": \"Cuidado compartilhado com a eSF.\"}".formatted(esf.getUuid())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        mockMvc.perform(como(MED_B, get(caso))).andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudoLiberado").value(true))
                .andExpect(jsonPath("$.acesso.descricao").value("Cuidado compartilhado em saúde mental: eSF C"));
        mockMvc.perform(como(PSI_REF, post(caso + "/compartilhamento/" + JsonPath.read(c, "$.compartilhamentos[0].uuid") + "/encerramento"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Fim do compartilhamento.\"}")).andExpect(status().isOk());
        mockMvc.perform(como(MED_B, get(caso))).andExpect(status().isForbidden());

        // A transferência para a UBS C: quem não tem acesso lá não aceita; o médico de lá aceita.
        String t = mockMvc.perform(como(PSI_REF, post(caso + "/transferencia")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"unidadeDestinoId\": \"%s\", \"resumo\": \"Estável; segue na eSF.\"}".formatted(ubsC.getUuid())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String aceite = "/api/v1/saude-mental/transferencia/" + JsonPath.read(t, "$.transferencias[0].uuid") + "/aceite";
        String corpo = "{\"equipeId\": \"%s\", \"tecnicoMatricula\": \"%sMEDB\"}".formatted(esf.getUuid(), MATRICULA);
        mockMvc.perform(como(PSI_REF, post(aceite)).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isForbidden());
        mockMvc.perform(como(MED_B, get("/api/v1/saude-mental/transferencias"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.recebidas.length()").value(1));
        String novo = mockMvc.perform(como(MED_B, post(aceite)).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadeNome").value(PREFIXO + "UBS C"))
                .andReturn().getResponse().getContentAsString();
        // O caso anterior fica legível para a referência do caso novo.
        mockMvc.perform(como(MED_B, get(caso))).andExpect(status().isOk()).andExpect(jsonPath("$.conteudoLiberado").value(true))
                .andExpect(jsonPath("$.situacao").value("ENCERRADO"));
        mockMvc.perform(como(MED_B, get("/api/v1/saude-mental/acompanhamento/" + JsonPath.read(novo, "$.uuid")))).andExpect(status().isOk());
    }
}
