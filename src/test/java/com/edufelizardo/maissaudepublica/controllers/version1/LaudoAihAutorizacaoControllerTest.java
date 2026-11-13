package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ProcedimentoCatalogo;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.AlcanceProcedimento;
import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoCatalogoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
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
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escopo do laudo de AIH (ADR-0186), com a segurança ligada: o médico do hospital solicita; o médico autorizador do hospital
 * autoriza, mas não o laudo que ele mesmo registrou; o autorizador de outro hospital e a recepção recebem 403; a faixa é da gestão
 * da rede (o gestor da unidade recebe 403).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class LaudoAihAutorizacaoControllerTest {

    private static final String PREFIXO = "LaudoAihAut Teste ";
    private static final String MEDICO = "74685960705";
    private static final String AUTORIZADOR = "85796071816";
    private static final String AUTORIZADOR_OUTRO = "96807182927";
    private static final String RECEPCAO = "07918293038";
    private static final String GESTOR_REDE = "18029304149";
    private static final String GESTOR_UNIDADE = "29130415250";
    private static final List<String> CPFS = List.of(MEDICO, AUTORIZADOR, AUTORIZADOR_OUTRO, RECEPCAO, GESTOR_REDE, GESTOR_UNIDADE);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private ProcedimentoCatalogoRepository procedimentoRepository;

    private Internacao internacao;
    private UUID procedimento;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude hospital = unidade("Hospital");
        UnidadeDeSaude outro = unidade("Outro hospital");
        usuario(MEDICO, "MEDICO", hospital);
        usuario(AUTORIZADOR, "MEDICO_AUTORIZADOR", hospital);
        usuario(AUTORIZADOR, "MEDICO", hospital);
        usuario(AUTORIZADOR_OUTRO, "MEDICO_AUTORIZADOR", outro);
        usuario(RECEPCAO, "RECEPCAO", hospital);
        usuario(GESTOR_REDE, "GESTOR", null);
        usuario(GESTOR_UNIDADE, "GESTOR", hospital);
        Profissional medico = profissional("LAA-MED", MEDICO);
        profissional("LAA-AUT", AUTORIZADOR);
        Paciente paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setSexo(Sexo.MASCULINO);
        paciente.setDataNascimento(LocalDate.of(1960, 2, 2));
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        Setor setor = setorRepository.save(new Setor(hospital, PREFIXO + "Clínica", "LAA-CM", TipoSetor.ASSISTENCIAL, true, null));
        Leito l = new Leito();
        l.setUnidade(hospital);
        l.setSetor(setor);
        l.setIdentificacao("LAA-01");
        l.setTipo(TipoLeito.CLINICO);
        l.setSexo(SexoLeito.MISTO);
        l.setFaixaEtaria(FaixaEtariaLeito.ADULTO);
        l.setSituacao(SituacaoLeito.OCUPADO);
        l.setAtivo(true);
        l = leitoRepository.save(l);
        Internacao i = new Internacao();
        i.setPaciente(paciente);
        i.setUnidade(hospital);
        i.setLeito(l);
        i.setMedicoResponsavel(medico);
        i.setCid("J189");
        i.setMotivo("Pneumonia.");
        i.setCarater(CaraterInternacao.URGENCIA);
        i.setAdmitidaEm(Instant.now());
        i.setStatus(StatusInternacao.INTERNADO);
        internacao = internacaoRepository.save(i);
        ProcedimentoCatalogo p = new ProcedimentoCatalogo();
        p.setCodigo("LAA-PROC");
        p.setNome(PREFIXO + "Tratamento de pneumonia");
        p.setAlcance(AlcanceProcedimento.NENHUM);
        p.setAtivo(true);
        procedimento = procedimentoRepository.save(p).getUuid();
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_faixa_aih");
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_evento_laudo_aih where laudo_id in (select uuid from tb_laudo_aih where unidade_id = ?)", u);
            jdbc.update("delete from tb_laudo_aih where unidade_id = ?", u);
            jdbc.update("delete from tb_internacao where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
        }
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
        for (String m : List.of("LAA-MED", "LAA-AUT")) {
            profissionalRepository.findByMatricula(m).ifPresent(profissionalRepository::delete);
        }
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        procedimentoRepository.findAll().stream().filter(p -> "LAA-PROC".equals(p.getCodigo())).forEach(procedimentoRepository::delete);
        unidadeDeSaudeRepository.deleteAllById(unidades);
    }

    @Test
    void medicoSolicitaAutorizadorDoHospitalAutorizaMasNaoOProprio() throws Exception {
        String laudo = """
                {"internacaoId": "%s", "profissionalMatricula": "%s", "sinaisSintomas": "Febre e dispneia.",
                 "condicoesJustificativas": "Hipoxemia.", "diagnosticoInicial": "Pneumonia.", "cidPrincipal": "J18.9",
                 "procedimentoId": "%s", "clinica": "CLINICA_MEDICA"}
                """;
        mockMvc.perform(json(post("/api/v1/laudo-aih/"), laudo.formatted(internacao.getUuid(), "LAA-MED", procedimento))).andExpect(status().isUnauthorized());
        mockMvc.perform(como(RECEPCAO, json(post("/api/v1/laudo-aih/"), laudo.formatted(internacao.getUuid(), "LAA-MED", procedimento))))
                .andExpect(status().isForbidden());
        String r = mockMvc.perform(como(MEDICO, json(post("/api/v1/laudo-aih/"), laudo.formatted(internacao.getUuid(), "LAA-MED", procedimento))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID doMedico = UUID.fromString(JsonPath.read(r, "$.uuid"));

        String faixa = "{\"ano\": %d, \"inicio\": 7000, \"fim\": 7010}".formatted(LocalDate.now().getYear());
        mockMvc.perform(como(GESTOR_UNIDADE, json(post("/api/v1/laudo-aih/faixas"), faixa))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR_REDE, json(post("/api/v1/laudo-aih/faixas"), faixa))).andExpect(status().isCreated());

        String decisao = "{\"profissionalMatricula\": \"LAA-AUT\"}";
        mockMvc.perform(como(MEDICO, json(post("/api/v1/laudo-aih/" + doMedico + "/autorizacao"), decisao))).andExpect(status().isForbidden());
        mockMvc.perform(como(AUTORIZADOR_OUTRO, json(post("/api/v1/laudo-aih/" + doMedico + "/autorizacao"), decisao))).andExpect(status().isForbidden());
        mockMvc.perform(como(AUTORIZADOR_OUTRO, get("/api/v1/laudo-aih/" + doMedico))).andExpect(status().isForbidden());
        mockMvc.perform(como(AUTORIZADOR, json(post("/api/v1/laudo-aih/" + doMedico + "/autorizacao"), decisao))).andExpect(status().isOk());

        // O autorizador que também é médico registra um laudo: não pode autorizá-lo, mesmo com a matrícula de outro.
        Internacao outra = new Internacao();
        outra.setPaciente(internacao.getPaciente());
        outra.setUnidade(internacao.getUnidade());
        outra.setLeito(internacao.getLeito());
        outra.setMedicoResponsavel(internacao.getMedicoResponsavel());
        outra.setCid("J189");
        outra.setMotivo("Outra.");
        outra.setCarater(CaraterInternacao.URGENCIA);
        outra.setAdmitidaEm(Instant.now());
        outra.setStatus(StatusInternacao.ALTA);
        outra.setAltaEm(Instant.now());
        outra = internacaoRepository.save(outra);
        String r2 = mockMvc.perform(como(AUTORIZADOR, json(post("/api/v1/laudo-aih/"), laudo.formatted(outra.getUuid(), "LAA-MED", procedimento))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID doAutorizador = UUID.fromString(JsonPath.read(r2, "$.uuid"));
        mockMvc.perform(como(AUTORIZADOR, json(post("/api/v1/laudo-aih/" + doAutorizador + "/autorizacao"), decisao)))
                .andExpect(status().isUnprocessableEntity());
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Profissional profissional(String matricula, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private void usuario(String cpf, String papel, UnidadeDeSaude unidade) {
        Usuario usuario = usuarioRepository.findByCpf(cpf).orElseGet(() -> usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante")));
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

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }
}
