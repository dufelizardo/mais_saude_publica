package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EspecialidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tratamento protético, fatia SB4b (ADR-0139): a abertura pela arcada ou pelo dente, o laboratório (LRPD ou contratado),
 * a ordem das etapas, a entrega que grava a prótese unitária no odontograma, a conclusão e o cancelamento.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TratamentoProteticoControllerTest {

    static final String PREFIXO = "Protese Teste ";
    private static final String MATRICULA = "SB4B-DENTISTA";
    private static final String URL = "/api/v1/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;
    @Autowired private EspecialidadeRepository especialidadeRepository;

    private Paciente paciente;
    private Atendimento atendimento;
    private UnidadeDeSaude lrpd;
    private UnidadeDeSaude semLrpd;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude ubs = unidade("UBS");
        lrpd = unidade("LRPD");
        semLrpd = unidade("Policlínica");
        jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, "
                + "uuid, ? from tb_capacidade_assistencial where codigo = 'LRPD'", lrpd.getUuid(), LocalDate.now().minusDays(1));
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setCpf("90022200001");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        Profissional d = new Profissional();
        d.setMatricula(MATRICULA);
        d.setCpf("90022200009");
        d.setNome(PREFIXO + "Dentista");
        d.setAtivo(true);
        d = profissionalRepository.save(d);
        atendimento = new Atendimento(paciente, d, ubs, null, null, TipoAtendimento.CONSULTA, StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now());
        atendimento.setEspecialidade(especialidadeRepository.findAll().stream().filter(e -> e.getCodigo().equals("ODONTOLOGIA")).findFirst().orElseThrow());
        atendimento = atendimentoRepository.save(atendimento);
    }

    @AfterEach
    void limpar() {
        limparPorPrefixo(jdbc, PREFIXO, MATRICULA);
    }

    static void limparPorPrefixo(JdbcTemplate jdbc, String prefixo, String matricula) {
        String pacientes = "(select uuid from tb_paciente where nome like '" + prefixo + "%')";
        String tratamentos = "(select uuid from tb_tratamento_protetico where paciente_id in " + pacientes + ")";
        jdbc.update("delete from tb_registro_odontograma where paciente_id in " + pacientes);
        jdbc.update("delete from tb_etapa_tratamento_protetico where tratamento_id in " + tratamentos);
        jdbc.update("delete from tb_tratamento_protetico where paciente_id in " + pacientes);
        jdbc.update("delete from tb_item_plano_tratamento_face where item_id in (select uuid from tb_item_plano_tratamento where plano_id in "
                + "(select uuid from tb_plano_tratamento where paciente_id in " + pacientes + "))");
        jdbc.update("delete from tb_item_plano_tratamento where plano_id in (select uuid from tb_plano_tratamento where paciente_id in " + pacientes + ")");
        jdbc.update("delete from tb_plano_tratamento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_atendimento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in (select uuid from tb_unidade_de_saude where nome like '" + prefixo + "%')");
        jdbc.update("delete from tb_paciente where nome like '" + prefixo + "%'");
        jdbc.update("delete from tb_profissional where matricula = '" + matricula + "'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + prefixo + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(nome.equals("UBS") ? TipoUnidadeDeSaude.UBS : TipoUnidadeDeSaude.CENTRO_ESPECIALIDADES);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String abrir(String extra, int esperado) throws Exception {
        return enviar(post(URL + "tratamento-protetico"), "{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"%s\"%s}"
                .formatted(atendimento.getUuid(), MATRICULA, extra), esperado);
    }

    private String etapa(String id, String etapa, String extra, int esperado) throws Exception {
        return enviar(post(URL + "tratamento-protetico/" + id + "/etapa"), "{\"etapa\": \"%s\", \"profissionalMatricula\": \"%s\"%s}"
                .formatted(etapa, MATRICULA, extra), esperado);
    }

    @Test
    void abrirPelaArcadaOuPeloDenteComOLaboratorio() throws Exception {
        abrir(", \"tipo\": \"TOTAL\"", 400);
        abrir(", \"tipo\": \"TOTAL\", \"arcada\": \"SUPERIOR\", \"dente\": 11", 422);
        abrir(", \"tipo\": \"UNITARIA\"", 400);
        abrir(", \"tipo\": \"UNITARIA\", \"dente\": 19", 400);
        abrir(", \"tipo\": \"TOTAL\", \"arcada\": \"SUPERIOR\", \"laboratorioUnidadeId\": \"" + semLrpd.getUuid() + "\"", 422);
        abrir(", \"tipo\": \"TOTAL\", \"arcada\": \"SUPERIOR\", \"laboratorioUnidadeId\": \"" + lrpd.getUuid() + "\", \"laboratorioExterno\": \"X\"", 400);
        String total = abrir(", \"tipo\": \"TOTAL\", \"arcada\": \"INFERIOR\", \"laboratorioUnidadeId\": \"" + lrpd.getUuid() + "\"", 201);
        assertThat(JsonPath.<String>read(total, "$.laboratorioNome")).isEqualTo(PREFIXO + "LRPD");
        assertThat((List<String>) JsonPath.read(total, "$.etapas[*].etapa")).containsExactly("PLANEJAMENTO");
        assertThat((List<String>) JsonPath.read(total, "$.proximasEtapas")).containsExactly("MOLDAGEM");
        String unitaria = abrir(", \"tipo\": \"UNITARIA\", \"dente\": 46, \"laboratorioExterno\": \"Laboratório Sorriso\"", 201);
        assertThat(JsonPath.<String>read(unitaria, "$.arcada")).isEqualTo("INFERIOR");
        assertThat(JsonPath.<Boolean>read(unitaria, "$.laboratorioContratado")).isTrue();
        // Fora da odontologia, não.
        enviar(post(URL + "tratamento-protetico"), "{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"%s\", \"tipo\": \"TOTAL\", \"arcada\": \"SUPERIOR\"}"
                .formatted(atendimentoRepository.save(new Atendimento(paciente, atendimento.getProfissional(), atendimento.getUnidade(), null, null,
                        TipoAtendimento.CONSULTA, StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now())).getUuid(), MATRICULA), 422);
        String lista = mockMvc.perform(get(URL + "paciente/" + paciente.getUuid() + "/tratamentos-proteticos")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat((List<String>) JsonPath.read(lista, "$[*].tipo")).containsExactly("UNITARIA", "TOTAL");
    }

    @Test
    void etapasNaOrdemEntregaNoOdontogramaEConclusao() throws Exception {
        String id = JsonPath.read(abrir(", \"tipo\": \"UNITARIA\", \"dente\": 46", 201), "$.uuid");
        etapa(id, "PROVA", "", 422);
        etapa(id, "ENTREGA", "", 422);
        etapa(id, "PLANEJAMENTO", "", 422);
        etapa(id, "MOLDAGEM", ", \"data\": \"" + LocalDate.now().plusDays(1) + "\"", 400);
        etapa(id, "MOLDAGEM", ", \"data\": \"" + LocalDate.now().minusDays(20) + "\"", 201);
        // Sem laboratório definido, o envio não pode; com o laboratório informado no envio, pode.
        etapa(id, "LABORATORIO", "", 422);
        etapa(id, "LABORATORIO", ", \"laboratorioUnidadeId\": \"" + semLrpd.getUuid() + "\"", 422);
        etapa(id, "LABORATORIO", ", \"data\": \"" + LocalDate.now().minusDays(15) + "\", \"laboratorioUnidadeId\": \"" + lrpd.getUuid() + "\"", 201);
        // Prova e laboratório se alternam: volta ao laboratório depois da prova.
        etapa(id, "PROVA", ", \"data\": \"" + LocalDate.now().minusDays(8) + "\", \"observacao\": \"Ajustar a cor\"", 201);
        etapa(id, "LABORATORIO", ", \"data\": \"" + LocalDate.now().minusDays(7) + "\"", 201);
        enviar(post(URL + "tratamento-protetico/" + id + "/conclusao"), "", 422);
        String entregue = etapa(id, "ENTREGA", "", 201);
        assertThat(JsonPath.<String>read(entregue, "$.dataEntrega")).isEqualTo(LocalDate.now().toString());
        assertThat((List<String>) JsonPath.read(entregue, "$.proximasEtapas")).containsExactly("ACOMPANHAMENTO");
        etapa(id, "PROVA", "", 422);
        etapa(id, "ACOMPANHAMENTO", "", 201);

        // A unitária entregue vira "Prótese" no dente, com a origem.
        String odontograma = mockMvc.perform(get(URL + "paciente/" + paciente.getUuid() + "/odontograma")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat((List<String>) JsonPath.read(odontograma, "$.dentes[?(@.dente == 46)].condicaoDente.condicaoCodigo")).containsExactly("PROTESE");
        assertThat((List<String>) JsonPath.read(odontograma, "$.dentes[?(@.dente == 46)].condicaoDente.tratamentoProteticoId")).containsExactly(id);

        String concluido = enviar(post(URL + "tratamento-protetico/" + id + "/conclusao"), "", 200);
        assertThat(JsonPath.<String>read(concluido, "$.situacao")).isEqualTo("CONCLUIDO");
        assertThat((List<String>) JsonPath.read(concluido, "$.proximasEtapas")).isEmpty();
        etapa(id, "ACOMPANHAMENTO", "", 422);
    }

    @Test
    void cancelamentoComMotivoEPlanoAberto() throws Exception {
        String item = jdbc.queryForObject("select cast(uuid as varchar) from tb_procedimento_catalogo where codigo = 'EXODONTIA'", String.class);
        String plano = JsonPath.read(enviar(post(URL + "plano-tratamento"), ("{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"%s\", "
                + "\"prioridade\": \"ELETIVA\", \"itens\": [{\"procedimentoCatalogoId\": \"%s\", \"dente\": 48}]}").formatted(atendimento.getUuid(), MATRICULA, item), 201), "$.uuid");
        String r = abrir(", \"tipo\": \"PARCIAL_REMOVIVEL\", \"arcada\": \"SUPERIOR\"", 201);
        assertThat(JsonPath.<String>read(r, "$.planoId")).isEqualTo(plano);
        String id = JsonPath.read(r, "$.uuid");
        enviar(post(URL + "tratamento-protetico/" + id + "/cancelamento"), "{}", 400);
        String c = enviar(post(URL + "tratamento-protetico/" + id + "/cancelamento"), "{\"motivo\": \"Paciente desistiu\"}", 200);
        assertThat(JsonPath.<String>read(c, "$.situacao")).isEqualTo("CANCELADO");
        etapa(id, "MOLDAGEM", "", 422);
    }
}
