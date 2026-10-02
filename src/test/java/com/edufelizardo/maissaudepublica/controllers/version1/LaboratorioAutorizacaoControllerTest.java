package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.PedidoExame;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.ExameLaboratorialRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemPedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.PedidoExameRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Laboratório com login e autorização ligados (ADR-0093): o médico pede na própria unidade; o técnico do laboratório
 * escolhido na coleta analisa, e o de outro laboratório não; só o responsável técnico libera; a recepção não lê.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class LaboratorioAutorizacaoControllerTest {

    private static final String URL = "/api/v1/pedido-exame/";
    private static final String PREFIXO = "LabAut Teste ";
    // CPFs com dígito verificador válido, exclusivos desta suíte.
    private static final String MEDICO = "48291736005";
    private static final String TECNICO = "59302847106";
    private static final String TECNICO_OUTRO = "60413958207";
    private static final String RT = "71524069308";
    private static final String RECEPCAO = "82635170409";
    private static final List<String> CPFS = List.of(MEDICO, TECNICO, TECNICO_OUTRO, RT, RECEPCAO);

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
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ExameLaboratorialRepository exameRepository;

    @Autowired
    private PedidoExameRepository pedidoRepository;

    @Autowired
    private ItemPedidoExameRepository itemRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude laboratorio;
    private Paciente paciente;
    private ExameLaboratorial hemoglobina;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        laboratorio = unidade("Laboratório");
        UnidadeDeSaude outroLaboratorio = unidade("Outro laboratório");
        usuario(MEDICO, "MEDICO", ubs);
        usuario(TECNICO, "TECNICO_DE_LABORATORIO", laboratorio);
        usuario(TECNICO_OUTRO, "TECNICO_DE_LABORATORIO", outroLaboratorio);
        usuario(RT, "RESPONSAVEL_TECNICO_LABORATORIO", laboratorio);
        usuario(RECEPCAO, "RECEPCAO", ubs);
        // O técnico coleta na UBS (posto de coleta) e o laboratório analisa.
        usuario(TECNICO, "TECNICO_DE_LABORATORIO", ubs);
        for (String cpf : List.of(MEDICO, TECNICO, TECNICO_OUTRO, RT)) {
            Profissional p = new Profissional();
            p.setMatricula("LABAUT-" + cpf.substring(0, 4));
            p.setCpf(cpf);
            p.setNome(PREFIXO + cpf);
            p.setAtivo(true);
            profissionalRepository.save(p);
        }
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        hemoglobina = new ExameLaboratorial();
        hemoglobina.setNome(PREFIXO + "Hemoglobina");
        hemoglobina.setMaterial(MaterialExame.SANGUE);
        hemoglobina.setTipoResultado(TipoResultadoExame.NUMERICO);
        hemoglobina.setUnidadeMedida("g/dL");
        hemoglobina.setReferenciaMinima(new BigDecimal("12"));
        hemoglobina.setReferenciaMaxima(new BigDecimal("16"));
        hemoglobina.setAtivo(true);
        hemoglobina = exameRepository.save(hemoglobina);
    }

    @AfterEach
    void limpar() {
        List<UUID> pacientes = pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).map(Paciente::getUuid).toList();
        for (UUID p : pacientes) {
            for (PedidoExame pedido : pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(p)) {
                UUID id = pedido.getUuid();
                jdbc.update("update tb_item_pedido_exame set resultado_atual_id = null, amostra_id = null where pedido_id = ?", id);
                jdbc.update("delete from tb_resultado_exame where item_id in (select uuid from tb_item_pedido_exame where pedido_id = ?)", id);
                jdbc.update("delete from tb_evento_exame where pedido_id = ?", id);
                jdbc.update("delete from tb_item_pedido_exame where pedido_id = ?", id);
                jdbc.update("delete from tb_amostra_exame where pedido_id = ?", id);
                jdbc.update("delete from tb_pedido_exame where uuid = ?", id);
            }
        }
        pacienteRepository.deleteAllById(pacientes);
        exameRepository.deleteAll(exameRepository.findAll().stream().filter(e -> e.getNome().startsWith(PREFIXO)).toList());
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("LABAUT-")).toList());
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList());
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(nome.contains("aborat") ? TipoUnidadeDeSaude.LABORATORIO : TipoUnidadeDeSaude.UBS);
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

    private UUID pedidoColetado() throws Exception {
        mockMvc.perform(como(MEDICO, json(post(URL), """
                {"pacienteId": "%s", "unidadeSolicitanteId": "%s", "profissionalMatricula": "LABAUT-%s", "exameIds": ["%s"],
                 "indicacaoClinica": "Astenia e palidez.", "prioridade": "ROTINA"}
                """.formatted(paciente.getUuid(), ubs.getUuid(), MEDICO.substring(0, 4), hemoglobina.getUuid()))))
                .andExpect(status().isCreated());
        UUID pedido = pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(paciente.getUuid()).get(0).getUuid();
        mockMvc.perform(como(TECNICO, json(post(URL + pedido + "/coleta"), """
                {"profissionalMatricula": "LABAUT-%s", "unidadeColetaId": "%s", "laboratorioId": "%s"}
                """.formatted(TECNICO.substring(0, 4), ubs.getUuid(), laboratorio.getUuid())))).andExpect(status().isOk());
        return pedido;
    }

    @Test
    void tecnicoDoLaboratorioAnalisaOdeOutroNaoESoORtLibera() throws Exception {
        UUID pedido = pedidoColetado();
        UUID item = itemRepository.findByPedido_Uuid(pedido).get(0).getUuid();
        String resultado = "{\"profissionalMatricula\": \"LABAUT-%s\", \"valorNumerico\": 10.5}";

        mockMvc.perform(como(TECNICO_OUTRO, json(post(URL + "item/" + item + "/resultado"), resultado.formatted(TECNICO_OUTRO.substring(0, 4)))))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(TECNICO_OUTRO, get(URL + "trabalho?etapa=EM_ANALISE")))
                .andExpect(jsonPath("$[?(@.pedidoId == '%s')]".formatted(pedido)).isEmpty());
        mockMvc.perform(como(TECNICO, get(URL + "trabalho?etapa=EM_ANALISE")))
                .andExpect(jsonPath("$[?(@.pedidoId == '%s')]".formatted(pedido)).isNotEmpty());
        mockMvc.perform(como(TECNICO, json(post(URL + "item/" + item + "/resultado"), resultado.formatted(TECNICO.substring(0, 4)))))
                .andExpect(status().isOk());

        String liberar = "{\"profissionalMatricula\": \"LABAUT-%s\"}";
        mockMvc.perform(como(TECNICO, json(post(URL + "item/" + item + "/liberacao"), liberar.formatted(TECNICO.substring(0, 4)))))
                .andExpect(status().isForbidden());
        mockMvc.perform(como(RT, json(post(URL + "item/" + item + "/liberacao"), liberar.formatted(RT.substring(0, 4)))))
                .andExpect(status().isOk());
        mockMvc.perform(como(MEDICO, get(URL + pedido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].resultado.interpretacao").value("ABAIXO"));
    }

    @Test
    void recepcaoNaoLeOPedidoEMedicoNaoColeta() throws Exception {
        UUID pedido = pedidoColetado();
        mockMvc.perform(como(RECEPCAO, get(URL + pedido))).andExpect(status().isForbidden());
        mockMvc.perform(como(RECEPCAO, get(URL))).andExpect(status().isForbidden());
        mockMvc.perform(como(MEDICO, get(URL + "trabalho?etapa=PARA_COLETAR"))).andExpect(status().isForbidden());
    }
}
