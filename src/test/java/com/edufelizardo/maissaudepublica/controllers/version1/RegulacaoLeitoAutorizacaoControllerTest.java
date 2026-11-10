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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escopo da central de leitos (ADR-0184), com a segurança ligada: o médico da origem pede, o regulador da rede reserva, o NIR do
 * hospital de destino admite; a recepção e o NIR de outro hospital não.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class RegulacaoLeitoAutorizacaoControllerTest {

    private static final String URL = "/api/v1/regulacao-leito/";
    private static final String PREFIXO = "RegLeitoAut Teste ";
    private static final String MEDICO = "51627384928";
    private static final String REGULADOR = "62738495010";
    private static final String NIR = "73849506193";
    private static final String NIR_OUTRO = "84950617249";
    private static final String RECEPCAO = "95061728386";
    private static final List<String> CPFS = List.of(MEDICO, REGULADOR, NIR, NIR_OUTRO, RECEPCAO);

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

    private UnidadeDeSaude upa;
    private UnidadeDeSaude hospital;
    private Paciente paciente;
    private UUID leito;

    @BeforeEach
    void seed() {
        limpar();
        upa = unidade("UPA", TipoUnidadeDeSaude.UPA);
        hospital = unidade("Hospital", TipoUnidadeDeSaude.HOSPITAL);
        UnidadeDeSaude outro = unidade("Outro hospital", TipoUnidadeDeSaude.HOSPITAL);
        usuario(MEDICO, "MEDICO", upa);
        usuario(REGULADOR, "REGULADOR_DE_LEITOS", null);
        usuario(NIR, "GESTOR_DE_LEITOS", hospital);
        usuario(NIR_OUTRO, "GESTOR_DE_LEITOS", outro);
        usuario(RECEPCAO, "RECEPCAO", upa);
        for (String cpf : List.of(MEDICO, REGULADOR, NIR)) {
            Profissional p = new Profissional();
            p.setMatricula("RLA-" + cpf.substring(0, 4));
            p.setCpf(cpf);
            p.setNome(PREFIXO + cpf);
            p.setAtivo(true);
            profissionalRepository.save(p);
        }
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setSexo(Sexo.FEMININO);
        paciente.setDataNascimento(LocalDate.of(1975, 5, 5));
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        Setor setor = setorRepository.save(new Setor(hospital, PREFIXO + "Clínica", "RLA-CM", TipoSetor.ASSISTENCIAL, true, null));
        Leito l = new Leito();
        l.setUnidade(hospital);
        l.setSetor(setor);
        l.setIdentificacao("RLA-01");
        l.setTipo(TipoLeito.CLINICO);
        l.setSexo(SexoLeito.MISTO);
        l.setFaixaEtaria(FaixaEtariaLeito.ADULTO);
        l.setSituacao(SituacaoLeito.LIVRE);
        l.setAtivo(true);
        leito = leitoRepository.save(l).getUuid();
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_evento_solicitacao_leito where solicitacao_id in (select uuid from tb_solicitacao_leito where unidade_origem_id = ?)", u);
            jdbc.update("delete from tb_solicitacao_leito where unidade_origem_id = ?", u);
        }
        for (UUID u : unidades) {
            jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id = ?)", u);
            jdbc.update("delete from tb_internacao where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
        }
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("RLA-")).toList());
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        unidadeDeSaudeRepository.deleteAllById(unidades);
    }

    @Test
    void medicoPedeReguladorReservaEODestinoAdmite() throws Exception {
        String pedido = """
                {"pacienteId": "%s", "unidadeOrigemId": "%s", "profissionalMatricula": "RLA-%s", "cid": "J18.9",
                 "quadroClinico": "Pneumonia com hipoxemia.", "tipoLeito": "CLINICO", "prioridade": "AMARELO"}
                """.formatted(paciente.getUuid(), upa.getUuid(), MEDICO.substring(0, 4));
        mockMvc.perform(como(RECEPCAO, json(post(URL), pedido))).andExpect(status().isForbidden());
        mockMvc.perform(json(post(URL), pedido)).andExpect(status().isUnauthorized());
        String id = JsonPath.read(mockMvc.perform(como(MEDICO, json(post(URL), pedido))).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.uuid");

        mockMvc.perform(como(MEDICO, get(URL + "fila"))).andExpect(status().isForbidden());
        mockMvc.perform(como(REGULADOR, get(URL + "fila"))).andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(id)).isNotEmpty());
        String reserva = "{\"profissionalMatricula\": \"RLA-%s\", \"leitoId\": \"%s\"}";
        mockMvc.perform(como(MEDICO, json(post(URL + id + "/reserva"), reserva.formatted(MEDICO.substring(0, 4), leito))))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(REGULADOR, json(post(URL + id + "/reserva"), reserva.formatted(REGULADOR.substring(0, 4), leito))))
                .andExpect(status().isOk());

        mockMvc.perform(como(NIR_OUTRO, get(URL + "a-receber"))).andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(id)).isEmpty());
        mockMvc.perform(como(NIR, get(URL + "a-receber"))).andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(id)).isNotEmpty());
        String admissao = "{\"medicoMatricula\": \"RLA-%s\"}".formatted(NIR.substring(0, 4));
        mockMvc.perform(como(NIR_OUTRO, json(post(URL + id + "/admissao"), admissao))).andExpect(status().isForbidden());
        mockMvc.perform(como(NIR, json(post(URL + id + "/admissao"), admissao))).andExpect(status().isOk());

        mockMvc.perform(como(MEDICO, get(URL + id))).andExpect(status().isOk());
        mockMvc.perform(como(RECEPCAO, get(URL + id))).andExpect(status().isForbidden());
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
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
