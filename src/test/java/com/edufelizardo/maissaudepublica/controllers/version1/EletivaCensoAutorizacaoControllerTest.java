package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escopo da internação eletiva e do censo diário (ADR-0185), com a segurança ligada: o NIR do hospital agenda e fecha o censo; o
 * médico agenda mas não fecha o censo; o NIR de outro hospital não mexe neste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class EletivaCensoAutorizacaoControllerTest {

    private static final String PREFIXO = "EletCensoAut Teste ";
    private static final String NIR = "41352637472";
    private static final String NIR_OUTRO = "52463748583";
    private static final String MEDICO = "63574859694";
    private static final List<String> CPFS = List.of(NIR, NIR_OUTRO, MEDICO);

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

    private UnidadeDeSaude hospital;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        hospital = unidade("Hospital");
        UnidadeDeSaude outro = unidade("Outro hospital");
        usuario(NIR, "GESTOR_DE_LEITOS", hospital);
        usuario(NIR_OUTRO, "GESTOR_DE_LEITOS", outro);
        usuario(MEDICO, "MEDICO", hospital);
        Profissional p = new Profissional();
        p.setMatricula("ECA-MED");
        p.setCpf(MEDICO);
        p.setNome(PREFIXO + "Médico");
        p.setAtivo(true);
        profissionalRepository.save(p);
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setSexo(Sexo.MASCULINO);
        paciente.setDataNascimento(LocalDate.of(1960, 2, 2));
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        Setor setor = setorRepository.save(new Setor(hospital, PREFIXO + "Cirurgia", "ECA-CC", TipoSetor.ASSISTENCIAL, true, null));
        Leito l = new Leito();
        l.setUnidade(hospital);
        l.setSetor(setor);
        l.setIdentificacao("ECA-01");
        l.setTipo(TipoLeito.CIRURGICO);
        l.setSexo(SexoLeito.MISTO);
        l.setFaixaEtaria(FaixaEtariaLeito.ADULTO);
        l.setSituacao(SituacaoLeito.LIVRE);
        l.setAtivo(true);
        leitoRepository.save(l);
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_censo_diario_paciente where censo_id in (select uuid from tb_censo_diario where unidade_id = ?)", u);
            jdbc.update("delete from tb_censo_diario where unidade_id = ?", u);
            jdbc.update("delete from tb_evento_internacao_agendada where agendamento_id in (select uuid from tb_internacao_agendada where unidade_id = ?)", u);
            jdbc.update("delete from tb_internacao_agendada where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
        }
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
        profissionalRepository.findByMatricula("ECA-MED").ifPresent(profissionalRepository::delete);
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        unidadeDeSaudeRepository.deleteAllById(unidades);
    }

    @Test
    void nirDoHospitalAgendaEFechaOCensoOMedicoSoAgenda() throws Exception {
        String agendamento = """
                {"pacienteId": "%s", "unidadeId": "%s", "dataPrevista": "%s", "tipoLeito": "CIRURGICO", "medicoMatricula": "ECA-MED",
                 "cid": "K40.9", "procedimento": "Herniorrafia inguinal."}
                """.formatted(paciente.getUuid(), hospital.getUuid(), LocalDate.now().plusDays(10));
        mockMvc.perform(json(post("/api/v1/internacao-eletiva/"), agendamento)).andExpect(status().isUnauthorized());
        mockMvc.perform(como(NIR_OUTRO, json(post("/api/v1/internacao-eletiva/"), agendamento))).andExpect(status().isForbidden());
        mockMvc.perform(como(MEDICO, json(post("/api/v1/internacao-eletiva/"), agendamento))).andExpect(status().isCreated());

        String censo = "{\"unidadeId\": \"%s\", \"data\": \"%s\"}".formatted(hospital.getUuid(), LocalDate.now().minusDays(1));
        mockMvc.perform(como(MEDICO, json(post("/api/v1/censo-diario/fechamento"), censo))).andExpect(status().isForbidden());
        mockMvc.perform(como(NIR_OUTRO, json(post("/api/v1/censo-diario/fechamento"), censo))).andExpect(status().isForbidden());
        mockMvc.perform(como(NIR, json(post("/api/v1/censo-diario/fechamento"), censo))).andExpect(status().isCreated());
        mockMvc.perform(como(NIR_OUTRO, get("/api/v1/censo-diario/?unidadeId=%s&data=%s".formatted(hospital.getUuid(), LocalDate.now().minusDays(1)))))
                .andExpect(status().isForbidden());
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
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
