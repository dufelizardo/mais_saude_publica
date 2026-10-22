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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Plano de tratamento odontológico, fatia SB3b (ADR-0137): abrir (um aberto por paciente), os itens com o complemento,
 * a execução pelo procedimento (agendado, desfecho, cancelado, retificação), a situação mudada pelo dentista, a
 * conclusão (tratamento concluído) e o abandono.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlanoTratamentoControllerTest {

    private static final String PREFIXO = "PlanoTrat Teste ";
    private static final String MATRICULA = "SB3B-DENTISTA";
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
    private Atendimento atendimentoGeral;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setCpf("90099900001");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        Profissional d = new Profissional();
        d.setMatricula(MATRICULA);
        d.setCpf("90099900009");
        d.setNome(PREFIXO + "Dentista");
        d.setAtivo(true);
        d = profissionalRepository.save(d);
        atendimento = new Atendimento(paciente, d, ubs, null, null, TipoAtendimento.CONSULTA, StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now());
        atendimento.setEspecialidade(especialidadeRepository.findAll().stream().filter(e -> e.getCodigo().equals("ODONTOLOGIA")).findFirst().orElseThrow());
        atendimento = atendimentoRepository.save(atendimento);
        atendimentoGeral = atendimentoRepository.save(new Atendimento(paciente, d, ubs, null, null, TipoAtendimento.CONSULTA,
                StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now()));
    }

    @AfterEach
    void limpar() {
        limparPorPrefixo(jdbc, PREFIXO, MATRICULA);
    }

    /** Apaga o plano, os itens, os procedimentos e o resto do paciente de teste (também usado pelo teste de autorização). */
    static void limparPorPrefixo(JdbcTemplate jdbc, String prefixo, String matricula) {
        String pacientes = "(select uuid from tb_paciente where nome like '" + prefixo + "%')";
        String atendimentos = "(select uuid from tb_atendimento where paciente_id in " + pacientes + ")";
        String procedimentos = "(select uuid from tb_procedimento where atendimento_id in " + atendimentos + ")";
        String planos = "(select uuid from tb_plano_tratamento where paciente_id in " + pacientes + ")";
        jdbc.update("delete from tb_item_plano_tratamento_face where item_id in (select uuid from tb_item_plano_tratamento where plano_id in " + planos + ")");
        jdbc.update("delete from tb_item_plano_tratamento where plano_id in " + planos);
        jdbc.update("delete from tb_plano_tratamento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_registro_odontograma where paciente_id in " + pacientes);
        jdbc.update("delete from tb_complemento_odontologico_face where complemento_id in (select uuid from tb_complemento_odontologico "
                + "where procedimento_id in " + procedimentos + ")");
        jdbc.update("delete from tb_complemento_odontologico where procedimento_id in " + procedimentos);
        jdbc.update("update tb_procedimento set retificacao_de_id = null where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_procedimento where atendimento_id in " + atendimentos);
        jdbc.update("update tb_avaliacao_odontologica set retificacao_de_id = null where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_avaliacao_odontologica where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_atendimento where uuid in " + atendimentos);
        jdbc.update("delete from tb_paciente where nome like '" + prefixo + "%'");
        jdbc.update("delete from tb_profissional where matricula = '" + matricula + "'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + prefixo + "%'");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String item(String codigo) {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_procedimento_catalogo where codigo = ?", String.class, codigo);
    }

    private String itemDoPlano(String codigo, String extra) {
        return "{\"procedimentoCatalogoId\": \"%s\"%s}".formatted(item(codigo), extra);
    }

    private String plano(Object atendimentoId, String prioridade, String... itens) {
        return "{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"%s\", \"diagnostico\": \"Cárie no 36\", %s\"itens\": [%s]}"
                .formatted(atendimentoId, MATRICULA, prioridade == null ? "" : "\"prioridade\": \"" + prioridade + "\", ", String.join(", ", itens));
    }

    private String procedimento(String itemPlano, String codigo, String status, String extra) {
        return ("{\"atendimentoId\": \"%s\", \"procedimentoCatalogoId\": \"%s\", \"profissionalMatricula\": \"%s\", \"dataRealizacao\": \"%s\", "
                + "\"status\": \"%s\", \"itemPlanoId\": \"%s\"%s}").formatted(atendimento.getUuid(), item(codigo), MATRICULA,
                LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.SECONDS), status, itemPlano, extra);
    }

    private String ultimoProcedimento() {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_procedimento where atendimento_id = ? order by registrado_em desc limit 1",
                String.class, atendimento.getUuid());
    }

    private static String idDoItem(String plano, String codigo) {
        return ((List<String>) JsonPath.read(plano, "$.itens[?(@.procedimentoNome =~ /.*" + codigo + ".*/)].uuid")).get(0);
    }

    @Test
    void abrirUmPorPacienteComItens() throws Exception {
        enviar(post(URL + "plano-tratamento"), plano(atendimentoGeral.getUuid(), "ELETIVA"), 422);
        enviar(post(URL + "plano-tratamento"), plano(atendimento.getUuid(), null), 400);
        // Os itens seguem o complemento: resina pede a face; curativo não é odontológico.
        enviar(post(URL + "plano-tratamento"), plano(atendimento.getUuid(), "ELETIVA", itemDoPlano("RESTAURACAO_RESINA", ", \"dente\": 36")), 400);
        enviar(post(URL + "plano-tratamento"), plano(atendimento.getUuid(), "ELETIVA", itemDoPlano("CURATIVO", "")), 422);
        String r = enviar(post(URL + "plano-tratamento"), plano(atendimento.getUuid(), "PRIORITARIA",
                itemDoPlano("RESTAURACAO_RESINA", ", \"dente\": 36, \"faces\": [\"OCLUSAL\"]"), itemDoPlano("EXODONTIA", ", \"dente\": 48")), 201);
        assertThat(JsonPath.<String>read(r, "$.situacao")).isEqualTo("ABERTO");
        assertThat((List<String>) JsonPath.read(r, "$.itens[*].situacao")).containsExactly("PLANEJADO", "PLANEJADO");
        assertThat((List<Integer>) JsonPath.read(r, "$.itens[*].ordem")).containsExactly(1, 2);
        enviar(post(URL + "plano-tratamento"), plano(atendimento.getUuid(), "ELETIVA"), 409);

        String plano = JsonPath.read(r, "$.uuid");
        String comItem = enviar(post(URL + "plano-tratamento/" + plano + "/item"), itemDoPlano("APLICACAO_FLUOR", ""), 201);
        assertThat((List<Integer>) JsonPath.read(comItem, "$.itens[*].ordem")).containsExactly(1, 2, 3);

        String planos = mockMvc.perform(get(URL + "paciente/" + paciente.getUuid() + "/planos-tratamento")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat((List<String>) JsonPath.read(planos, "$[*].prioridade")).containsExactly("PRIORITARIA");
    }

    @Test
    void execucaoPeloProcedimentoEConclusao() throws Exception {
        String r = enviar(post(URL + "plano-tratamento"), plano(atendimento.getUuid(), "ELETIVA",
                itemDoPlano("RESTAURACAO_RESINA", ", \"dente\": 36, \"faces\": [\"OCLUSAL\"]"), itemDoPlano("EXODONTIA", ", \"dente\": 48"),
                itemDoPlano("PULPOTOMIA", ", \"dente\": 46")), 201);
        String plano = JsonPath.read(r, "$.uuid");
        String resina = idDoItem(r, "resina");
        String exodontia = idDoItem(r, "Exodontia");
        String pulpotomia = idDoItem(r, "Pulpotomia");
        String url = URL + "procedimento/";

        // O procedimento precisa corresponder ao item: outro dente, outro procedimento.
        enviar(post(url), procedimento(resina, "RESTAURACAO_RESINA", "REALIZADO", ", \"dente\": 37, \"faces\": [\"OCLUSAL\"]"), 422);
        enviar(post(url), procedimento(resina, "SELANTE", "REALIZADO", ", \"dente\": 36, \"faces\": [\"OCLUSAL\"]"), 422);
        // Realizado: conclui.
        enviar(post(url), procedimento(resina, "RESTAURACAO_RESINA", "REALIZADO", ", \"dente\": 36, \"faces\": [\"OCLUSAL\"]"), 201);
        // Agendado: em andamento; o desfecho cancelado devolve a planejado; o novo agendado e o desfecho realizado concluem.
        enviar(post(url), procedimento(exodontia, "EXODONTIA", "AGENDADO", ", \"dente\": 48"), 201);
        String agendado = ultimoProcedimento();
        assertThat(situacoes()).containsExactly("CONCLUIDO", "EM_ANDAMENTO", "PLANEJADO");
        enviar(post(url + agendado + "/status"), "{\"status\": \"CANCELADO\", \"justificativa\": \"Faltou\", \"profissionalMatricula\": \"" + MATRICULA + "\"}", 200);
        assertThat(situacoes()).containsExactly("CONCLUIDO", "PLANEJADO", "PLANEJADO");
        enviar(post(url), procedimento(exodontia, "EXODONTIA", "AGENDADO", ", \"dente\": 48"), 201);
        enviar(post(url + ultimoProcedimento() + "/status"), ("{\"status\": \"REALIZADO\", \"dataRealizacao\": \"%s\", \"profissionalMatricula\": "
                + "\"%s\"}").formatted(LocalDateTime.now().minusMinutes(5).truncatedTo(ChronoUnit.SECONDS), MATRICULA), 200);
        assertThat(situacoes()).containsExactly("CONCLUIDO", "CONCLUIDO", "PLANEJADO");
        // Executar o concluído não pode; a retificação precisa manter o item.
        enviar(post(url), procedimento(resina, "RESTAURACAO_RESINA", "REALIZADO", ", \"dente\": 36, \"faces\": [\"OCLUSAL\"]"), 422);
        String daResina = jdbc.queryForObject("select cast(procedimento_id as varchar) from tb_item_plano_tratamento where uuid = cast(? as uuid)",
                String.class, resina);
        enviar(post(url + daResina + "/retificacao"), procedimento(resina, "RESTAURACAO_RESINA", "REALIZADO",
                ", \"dente\": 37, \"faces\": [\"OCLUSAL\"], \"motivoRetificacao\": \"Dente\"").replace("\"itemPlanoId\": \"" + resina + "\", ", ""), 422);

        // Concluir com item pendente não pode; o dentista encaminha (ou cancela com motivo) e conclui.
        enviar(post(URL + "plano-tratamento/" + plano + "/conclusao"), "", 422);
        enviar(post(URL + "item-plano-tratamento/" + pulpotomia + "/situacao"), "{\"situacao\": \"CONCLUIDO\"}", 400);
        enviar(post(URL + "item-plano-tratamento/" + pulpotomia + "/situacao"), "{\"situacao\": \"CANCELADO\"}", 400);
        enviar(post(URL + "item-plano-tratamento/" + pulpotomia + "/situacao"), "{\"situacao\": \"EM_ANDAMENTO\"}", 200);
        enviar(post(URL + "item-plano-tratamento/" + pulpotomia + "/situacao"), "{\"situacao\": \"ENCAMINHADO\", \"motivo\": \"CEO\"}", 200);
        String concluido = enviar(post(URL + "plano-tratamento/" + plano + "/conclusao"), "", 200);
        assertThat(JsonPath.<String>read(concluido, "$.situacao")).isEqualTo("CONCLUIDO");
        assertThat(JsonPath.<String>read(concluido, "$.encerradoEm")).isNotNull();
        enviar(post(URL + "item-plano-tratamento/" + pulpotomia + "/situacao"), "{\"situacao\": \"CANCELADO\", \"motivo\": \"X\"}", 422);
        // Encerrado, um novo plano pode ser aberto.
        enviar(post(URL + "plano-tratamento"), plano(atendimento.getUuid(), "ELETIVA"), 201);
    }

    @Test
    void conclusaoSemItemConcluidoEAbandono() throws Exception {
        String r = enviar(post(URL + "plano-tratamento"), plano(atendimento.getUuid(), "ELETIVA", itemDoPlano("APLICACAO_FLUOR", "")), 201);
        String plano = JsonPath.read(r, "$.uuid");
        enviar(post(URL + "item-plano-tratamento/" + idDoItem(r, "flúor") + "/situacao"), "{\"situacao\": \"CANCELADO\", \"motivo\": \"Recusou\"}", 200);
        enviar(post(URL + "plano-tratamento/" + plano + "/conclusao"), "", 422);
        enviar(post(URL + "plano-tratamento/" + plano + "/abandono"), "{}", 400);
        String a = enviar(post(URL + "plano-tratamento/" + plano + "/abandono"), "{\"motivo\": \"Não voltou\"}", 200);
        assertThat(JsonPath.<String>read(a, "$.situacao")).isEqualTo("ABANDONADO");
        enviar(post(URL + "plano-tratamento/" + plano + "/item"), itemDoPlano("APLICACAO_FLUOR", ""), 422);
    }

    private List<String> situacoes() {
        return jdbc.queryForList("select i.situacao from tb_item_plano_tratamento i join tb_plano_tratamento p on p.uuid = i.plano_id "
                + "where p.paciente_id = ? and p.situacao = 'ABERTO' order by i.ordem", String.class, paciente.getUuid());
    }
}
