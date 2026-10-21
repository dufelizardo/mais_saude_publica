package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.EspecialidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Saúde Bucal com login e autorização ligados (ADR-0134): a avaliação odontológica é do cirurgião-dentista; o técnico em
 * saúde bucal e o enfermeiro não a registram; o catálogo de especialidades é lido por qualquer logado e mantido por
 * quem cuida da organização.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class SaudeBucalAutorizacaoControllerTest {

    private static final String PREFIXO = "SbAut Teste ";
    private static final String DENTISTA = "94801867360";
    private static final String TSB = "94801867361";
    private static final String ENF = "94801867362";
    private static final List<String> CPFS = List.of(DENTISTA, TSB, ENF);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;
    @Autowired private EspecialidadeRepository especialidadeRepository;

    private UnidadeDeSaude ubs;
    private Atendimento atendimento;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        atribuir(DENTISTA, "CIRURGIAO_DENTISTA");
        atribuir(TSB, "TECNICO_SAUDE_BUCAL");
        atribuir(ENF, "ENFERMEIRO");
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90066600001");
        p.setAtivo(true);
        p = pacienteRepository.save(p);
        Profissional d = new Profissional();
        d.setMatricula("SBAUT-DENTISTA");
        d.setCpf(DENTISTA);
        d.setNome(PREFIXO + "Dentista");
        d.setAtivo(true);
        d = profissionalRepository.save(d);
        atendimento = new Atendimento(p, d, ubs, null, null, TipoAtendimento.CONSULTA, StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now());
        atendimento.setEspecialidade(especialidadeRepository.findAll().stream().filter(e -> e.getCodigo().equals("ODONTOLOGIA")).findFirst().orElseThrow());
        atendimento = atendimentoRepository.save(atendimento);
    }

    @AfterEach
    void limpar() {
        String atendimentos = "(select uuid from tb_atendimento where unidade_id in (select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%'))";
        jdbc.update("delete from tb_avaliacao_odontologica where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_atendimento where uuid in " + atendimentos);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_especialidade where codigo = 'TSTSBAUT'");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = 'SBAUT-DENTISTA'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private void atribuir(String cpf, String papel) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(ubs);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    @Test
    void avaliacaoOdontologicaSoDoCirurgiaoDentista() throws Exception {
        String corpo = "{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"SBAUT-DENTISTA\", \"risco\": \"BAIXO\", \"necessidadeTratamento\": \"ELETIVA\"}"
                .formatted(atendimento.getUuid());
        mockMvc.perform(como(TSB, post("/api/v1/avaliacao-odontologica")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(ENF, post("/api/v1/avaliacao-odontologica")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(DENTISTA, post("/api/v1/avaliacao-odontologica")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());
    }

    @Test
    void catalogoDeEspecialidadesLidoPorLogadoEMantidoPelaOrganizacao() throws Exception {
        mockMvc.perform(get("/api/v1/especialidades")).andExpect(status().isUnauthorized());
        mockMvc.perform(como(TSB, get("/api/v1/especialidades"))).andExpect(status().isOk());
        mockMvc.perform(como(DENTISTA, post("/api/v1/especialidade")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\": \"TSTSBAUT\", \"nome\": \"Teste\"}")).andExpect(status().isForbidden());
    }
}
