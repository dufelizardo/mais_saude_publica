package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * A I6 com login e autorização ligados (ADR-0206): a coordenação da origem envia, a do destino não, nem o técnico; o técnico do destino
 * recebe, o da origem não; a coordenação de uma unidade vê os alertas, mas só a da rede suspende; o médico revoga a contraindicação, o
 * técnico não.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class ImunizacaoRedeAutorizacaoControllerTest {

    private static final String PREFIXO = "ImuRedeAut Teste ";
    private static final String COORD_ORIGEM = "94501867390";
    private static final String COORD_DESTINO = "94501867391";
    private static final String TEC_ORIGEM = "94501867392";
    private static final String TEC_DESTINO = "94501867393";
    private static final String GESTOR_REDE = "94501867394";
    private static final String MEDICO = "94501867395";
    private static final List<String> CPFS = List.of(COORD_ORIGEM, COORD_DESTINO, TEC_ORIGEM, TEC_DESTINO, GESTOR_REDE, MEDICO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private PacienteRepository pacienteRepository;

    private UnidadeDeSaude origem;
    private UnidadeDeSaude destino;

    @BeforeEach
    void seed() {
        limpar();
        origem = unidade("Origem");
        destino = unidade("Destino");
        for (UnidadeDeSaude u : List.of(origem, destino)) {
            jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                    + "from tb_capacidade_assistencial where codigo = 'SALA_VACINA'", u.getUuid(), LocalDate.now().minusDays(1));
        }
        atribuir(COORD_ORIGEM, "COORDENADOR_DE_ENFERMAGEM", origem);
        atribuir(COORD_DESTINO, "COORDENADOR_DE_ENFERMAGEM", destino);
        atribuir(TEC_ORIGEM, "TECNICO_DE_ENFERMAGEM", origem);
        atribuir(TEC_DESTINO, "TECNICO_DE_ENFERMAGEM", destino);
        atribuir(GESTOR_REDE, "GESTOR", null);
        atribuir(MEDICO, "MEDICO", origem);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        ImunizacaoRedeControllerTest.limparDados(jdbc, PREFIXO, "-");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
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

    private MvcResult como(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false))).andReturn();
    }

    private int status(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return como(cpf, r).getResponse().getStatus();
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private String imuno() {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_imunobiologico where codigo = 'PENTA'", String.class);
    }

    @Test
    void remanejamentoSuspensaoECrie() throws Exception {
        UUID lote = UUID.randomUUID();
        jdbc.update("insert into tb_lote_imunobiologico (uuid, unidade_id, imunobiologico_id, numero_lote, validade, doses, situacao) "
                + "values (?, ?, cast(? as uuid), 'I6AUT1', ?, 10, 'DISPONIVEL')", lote, origem.getUuid(), imuno(), LocalDate.now().plusDays(100));
        String envio = "{\"loteId\": \"" + lote + "\", \"unidadeDestinoId\": \"" + destino.getUuid() + "\", \"doses\": 3, \"motivo\": \"EXCESSO\"}";
        assertThat(status(TEC_ORIGEM, json(post("/api/v1/sala-de-vacina/remanejamento"), envio))).isEqualTo(403);
        assertThat(status(COORD_DESTINO, json(post("/api/v1/sala-de-vacina/remanejamento"), envio))).isEqualTo(403);
        MvcResult r = como(COORD_ORIGEM, json(post("/api/v1/sala-de-vacina/remanejamento"), envio));
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        String id = JsonPath.read(r.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.uuid");

        // O recebimento é do destino.
        String chegada = "{\"temperaturaChegada\": 4.0}";
        assertThat(status(TEC_ORIGEM, json(post("/api/v1/remanejamento-imunobiologico/" + id + "/recebimento"), chegada))).isEqualTo(403);
        assertThat(status(TEC_DESTINO, get("/api/v1/sala-de-vacina/remanejamentos?unidadeId=" + destino.getUuid()))).isEqualTo(200);
        assertThat(status(TEC_DESTINO, json(post("/api/v1/remanejamento-imunobiologico/" + id + "/recebimento"), chegada))).isEqualTo(200);

        // Os alertas para quem gerencia; a suspensão só para a rede.
        assertThat(status(TEC_ORIGEM, get("/api/v1/imunizacao/alertas-lote"))).isEqualTo(403);
        assertThat(status(COORD_ORIGEM, get("/api/v1/imunizacao/alertas-lote"))).isEqualTo(200);
        String suspensao = "{\"imunobiologicoId\": \"" + imuno() + "\", \"numeroLote\": \"I6AUT1\", \"motivo\": \"Comunicado do fabricante.\"}";
        assertThat(status(COORD_ORIGEM, json(post("/api/v1/imunizacao/suspensao-lote"), suspensao))).isEqualTo(403);
        assertThat(status(GESTOR_REDE, json(post("/api/v1/imunizacao/suspensao-lote"), suspensao))).isEqualTo(201);

        // A revogação da contraindicação: o médico sim, o técnico não.
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90055500999");
        p.setSexo(Sexo.FEMININO);
        p.setDataNascimento(LocalDate.now().minusYears(2));
        p.setAtivo(true);
        p = pacienteRepository.save(p);
        UUID contra = UUID.randomUUID();
        jdbc.update("insert into tb_contraindicacao_vacinal (uuid, paciente_id, imunobiologico_id, desde, motivo, registrada_em) "
                + "values (?, ?, cast(? as uuid), ?, 'ESAVI grave.', now())", contra, p.getUuid(), imuno(), LocalDate.now().minusDays(30));
        String parecer = "{\"parecer\": \"Reavaliada no CRIE.\"}";
        assertThat(status(TEC_ORIGEM, json(post("/api/v1/contraindicacao-vacinal/" + contra + "/revogacao"), parecer))).isEqualTo(403);
        assertThat(status(MEDICO, json(post("/api/v1/contraindicacao-vacinal/" + contra + "/revogacao"), parecer))).isEqualTo(200);
        assertThat(mockMvc.perform(get("/api/v1/imunizacao/alertas-lote")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
