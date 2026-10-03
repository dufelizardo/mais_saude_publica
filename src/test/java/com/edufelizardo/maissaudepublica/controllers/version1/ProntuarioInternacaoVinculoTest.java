package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlta;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prontuário por vínculo com a internação (ADR-0100): a equipe da unidade onde o paciente está internado e o médico
 * responsável abrem o prontuário; outra unidade, não; depois da janela da alta, o vínculo acaba. O prontuário traz as
 * internações com o sumário de alta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true",
        "app.security.prontuario-por-vinculo.enabled=true"})
class ProntuarioInternacaoVinculoTest {

    private static final String PREFIXO = "IntVinc Teste ";
    private static final String ENF_HOSPITAL = "93401857620";
    private static final String MED_RESPONSAVEL = "93401857621";
    private static final String ENF_OUTRA = "93401857622";
    private static final List<String> CPFS = List.of(ENF_HOSPITAL, MED_RESPONSAVEL, ENF_OUTRA);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private PapelRepository papelRepository;

    private Paciente paciente;
    private Internacao internacao;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude hospital = unidade("Hospital");
        UnidadeDeSaude outra = unidade("Outra unidade");
        atribuir(ENF_HOSPITAL, "ENFERMEIRO", hospital);
        atribuir(MED_RESPONSAVEL, "MEDICO", outra);
        atribuir(ENF_OUTRA, "ENFERMEIRO", outra);
        Profissional medico = new Profissional();
        medico.setMatricula("IV-MED");
        medico.setCpf(MED_RESPONSAVEL);
        medico.setNome(PREFIXO + "Médico");
        medico.setAtivo(true);
        medico = profissionalRepository.save(medico);

        Setor enfermaria = setorRepository.save(new Setor(hospital, PREFIXO + "Clínica", "IV-CL", TipoSetor.ASSISTENCIAL, true, null));
        Leito leito = new Leito();
        leito.setUnidade(hospital);
        leito.setSetor(enfermaria);
        leito.setIdentificacao("IV 01");
        leito.setTipo(TipoLeito.CLINICO);
        leito.setSexo(SexoLeito.MISTO);
        leito.setSituacao(SituacaoLeito.OCUPADO);
        leito.setAtivo(true);
        leito = leitoRepository.save(leito);

        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setSexo(Sexo.MASCULINO);
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);

        Internacao i = new Internacao();
        i.setPaciente(paciente);
        i.setUnidade(hospital);
        i.setLeito(leito);
        i.setMedicoResponsavel(medico);
        i.setCid("J189");
        i.setMotivo("Pneumonia com hipoxemia.");
        i.setCarater(CaraterInternacao.URGENCIA);
        i.setAdmitidaEm(Instant.now().minus(Duration.ofDays(3)));
        i.setStatus(StatusInternacao.INTERNADO);
        internacao = internacaoRepository.save(i);
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id = ?)", u);
            jdbc.update("delete from tb_internacao where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
        }
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        profissionalRepository.findByMatricula("IV-MED").ifPresent(profissionalRepository::delete);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
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

    private MockHttpServletRequestBuilder prontuario(String cpf) {
        return get("/api/v1/prontuario/" + paciente.getUuid())
                .header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    @Test
    void equipeDaUnidadeEMedicoResponsavelAbremOutraUnidadeNao() throws Exception {
        mockMvc.perform(prontuario(ENF_HOSPITAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acesso.base").value("VINCULO"))
                .andExpect(jsonPath("$.acesso.descricao", startsWith("Internação na unidade")))
                .andExpect(jsonPath("$.internacoes[0].leitoIdentificacao").value("IV 01"))
                .andExpect(jsonPath("$.internacoes[0].motivo").value("Pneumonia com hipoxemia."));
        mockMvc.perform(prontuario(MED_RESPONSAVEL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acesso.descricao", startsWith("Médico responsável pela internação")));
        mockMvc.perform(prontuario(ENF_OUTRA)).andExpect(status().isForbidden());
    }

    @Test
    void depoisDaJanelaDaAltaOVinculoAcaba() throws Exception {
        internacao.setStatus(StatusInternacao.ALTA);
        internacao.setTipoAlta(TipoAlta.MELHORADO);
        internacao.setAltaEm(Instant.now().minus(Duration.ofDays(2)));
        internacao.setSumarioAlta("Pneumonia tratada; alta com antibiótico oral.");
        internacaoRepository.save(internacao);
        mockMvc.perform(prontuario(ENF_HOSPITAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.internacoes[0].sumarioAlta").value("Pneumonia tratada; alta com antibiótico oral."));

        jdbc.update("update tb_internacao set admitida_em = ?, alta_em = ? where uuid = ?",
                java.sql.Timestamp.from(Instant.now().minus(Duration.ofDays(90))),
                java.sql.Timestamp.from(Instant.now().minus(Duration.ofDays(60))), internacao.getUuid());
        mockMvc.perform(prontuario(ENF_HOSPITAL)).andExpect(status().isForbidden());
    }
}
