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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Procedimento odontológico, fatia SB3a (ADR-0136): o catálogo geral de procedimentos, o procedimento direto no
 * atendimento, o complemento odontológico (dente e faces) com as regras do odontograma e os registros que o procedimento
 * realizado grava, anulados na retificação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProcedimentoOdontologicoControllerTest {

    private static final String PREFIXO = "ProcOdonto Teste ";
    private static final String MATRICULA = "SB3-DENTISTA";
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
        paciente.setCpf("90088800001");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        Profissional d = new Profissional();
        d.setMatricula(MATRICULA);
        d.setCpf("90088800009");
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
        String pacientes = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        String atendimentos = "(select uuid from tb_atendimento where paciente_id in " + pacientes + ")";
        String procedimentos = "(select uuid from tb_procedimento where atendimento_id in " + atendimentos + ")";
        jdbc.update("delete from tb_registro_odontograma where paciente_id in " + pacientes);
        jdbc.update("delete from tb_complemento_odontologico_face where complemento_id in (select uuid from tb_complemento_odontologico "
                + "where procedimento_id in " + procedimentos + ")");
        jdbc.update("delete from tb_complemento_odontologico where procedimento_id in " + procedimentos);
        jdbc.update("update tb_procedimento set retificacao_de_id = null where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_procedimento where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_atendimento where uuid in " + atendimentos);
        jdbc.update("delete from tb_procedimento_catalogo where codigo like 'TSTSB3%'");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = '" + MATRICULA + "'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private String item(String codigo) throws Exception {
        return ((List<String>) JsonPath.read(ler(URL + "procedimentos-catalogo"), "$[?(@.codigo == '" + codigo + "')].uuid")).get(0);
    }

    private String uuidDe(String tabela, String codigo) {
        return jdbc.queryForObject("select cast(uuid as varchar) from " + tabela + " where codigo = ?", String.class, codigo);
    }

    /** Corpo do procedimento direto no atendimento; {@code extra} completa (dente, faces, motivo). */
    private String procedimento(Object atendimentoId, String catalogo, String status, String extra) throws Exception {
        return ("{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"%s\", %s\"dataRealizacao\": \"%s\", \"status\": \"%s\"%s}")
                .formatted(atendimentoId, MATRICULA, catalogo == null ? "" : "\"procedimentoCatalogoId\": \"" + item(catalogo) + "\", ",
                        LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.SECONDS), status, extra.isEmpty() ? "" : ", " + extra);
    }

    private String registrar(String catalogo, String status, String extra) throws Exception {
        enviar(post(URL + "procedimento/"), procedimento(atendimento.getUuid(), catalogo, status, extra), 201);
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_procedimento where atendimento_id = ? order by registrado_em desc limit 1",
                String.class, atendimento.getUuid());
    }

    @Test
    void catalogoGeralComComplementoOdontologico() throws Exception {
        String todos = ler(URL + "procedimentos-catalogo");
        assertThat((List<String>) JsonPath.read(todos, "$[?(@.codigo == 'RESTAURACAO_RESINA')].condicaoResultanteNome")).containsExactly("Restaurado");
        assertThat((List<Boolean>) JsonPath.read(todos, "$[?(@.codigo == 'EXODONTIA')].exigeDentista")).containsExactly(true);
        assertThat((List<Boolean>) JsonPath.read(todos, "$[?(@.codigo == 'APLICACAO_FLUOR')].exigeDentista")).containsExactly(false);
        // Pela especialidade: os itens dela e os de uso geral.
        String odonto = ler(URL + "procedimentos-catalogo?especialidadeId=" + atendimento.getEspecialidade().getUuid());
        assertThat((List<String>) JsonPath.read(odonto, "$[*].codigo")).contains("CURATIVO", "EXODONTIA");

        String odontologia = atendimento.getEspecialidade().getUuid().toString();
        enviar(post(URL + "procedimento-catalogo"), "{\"codigo\": \"CURATIVO\", \"nome\": \"X\"}", 409);
        enviar(post(URL + "procedimento-catalogo"), "{\"codigo\": \"TSTSB3_X\", \"nome\": \"X\", \"sigtap\": \"123\"}", 400);
        // Dente e face só em procedimento odontológico; a condição precisa caber no alcance.
        enviar(post(URL + "procedimento-catalogo"), "{\"codigo\": \"TSTSB3_X\", \"nome\": \"X\", \"alcance\": \"DENTE\"}", 422);
        enviar(post(URL + "procedimento-catalogo"), ("{\"codigo\": \"TSTSB3_X\", \"nome\": \"X\", \"especialidadeId\": \"%s\", \"alcance\": \"DENTE\", "
                + "\"condicaoResultanteId\": \"%s\"}").formatted(odontologia, uuidDe("tb_condicao_odontologica", "CARIE")), 422);
        enviar(post(URL + "procedimento-catalogo"), ("{\"codigo\": \"TSTSB3_X\", \"nome\": \"X\", \"especialidadeId\": \"%s\", "
                + "\"condicaoResultanteId\": \"%s\"}").formatted(odontologia, uuidDe("tb_condicao_odontologica", "SELANTE")), 422);
        String r = enviar(post(URL + "procedimento-catalogo"), ("{\"codigo\": \"TSTSB3_COROA\", \"nome\": \"Coroa provisória\", \"especialidadeId\": \"%s\", "
                + "\"alcance\": \"DENTE\", \"condicaoResultanteId\": \"%s\", \"exigeDentista\": true, \"sigtap\": \"0307040089\"}")
                .formatted(odontologia, uuidDe("tb_condicao_odontologica", "PROTESE")), 201);
        assertThat(JsonPath.<Boolean>read(r, "$.odontologico")).isTrue();
        assertThat(JsonPath.<String>read(r, "$.sigtap")).isEqualTo("0307040089");
        enviar(put(URL + "procedimento-catalogo/" + JsonPath.read(r, "$.uuid")), "{\"codigo\": \"TSTSB3_OUTRO\", \"nome\": \"X\"}", 422);
        String inativo = enviar(put(URL + "procedimento-catalogo/" + JsonPath.read(r, "$.uuid")), ("{\"codigo\": \"TSTSB3_COROA\", \"nome\": \"Coroa\", "
                + "\"especialidadeId\": \"%s\", \"alcance\": \"DENTE\", \"ativo\": false}").formatted(odontologia), 200);
        assertThat(JsonPath.<Boolean>read(inativo, "$.ativo")).isFalse();
        enviar(post(URL + "procedimento/"), procedimento(atendimento.getUuid(), "TSTSB3_COROA", "REALIZADO", "\"dente\": 11"), 422);
    }

    @Test
    void complementoSegueOCatalogoEOAtendimento() throws Exception {
        String url = URL + "procedimento/";
        enviar(post(url), procedimento(null, "APLICACAO_FLUOR", "REALIZADO", "").replace("\"atendimentoId\": \"null\", ", ""), 400);
        enviar(post(url), procedimento(atendimento.getUuid(), null, "REALIZADO", ""), 400);
        enviar(post(url), procedimento(atendimento.getUuid(), "RESTAURACAO_RESINA", "REALIZADO", "\"dente\": 36"), 400);
        enviar(post(url), procedimento(atendimento.getUuid(), "RESTAURACAO_RESINA", "REALIZADO", "\"dente\": 39, \"faces\": [\"OCLUSAL\"]"), 400);
        enviar(post(url), procedimento(atendimento.getUuid(), "RESTAURACAO_RESINA", "REALIZADO", "\"dente\": 36, \"faces\": [\"INCISAL\"]"), 422);
        enviar(post(url), procedimento(atendimento.getUuid(), "EXODONTIA", "REALIZADO", "\"dente\": 48, \"faces\": [\"MESIAL\"]"), 422);
        enviar(post(url), procedimento(atendimento.getUuid(), "APLICACAO_FLUOR", "REALIZADO", "\"dente\": 11"), 422);
        enviar(post(url), procedimento(atendimento.getUuid(), null, "REALIZADO", "\"tipo\": \"Outro\", \"dente\": 11"), 422);
        // Odontológico só em atendimento odontológico; o geral entra em qualquer um.
        enviar(post(url), procedimento(atendimentoGeral.getUuid(), "APLICACAO_FLUOR", "REALIZADO", ""), 422);
        enviar(post(url), procedimento(atendimentoGeral.getUuid(), "CURATIVO", "REALIZADO", ""), 201);
        enviar(post(url), procedimento(atendimento.getUuid(), "APLICACAO_FLUOR", "REALIZADO", ""), 201);

        String prontuario = ler(URL + "prontuario/" + paciente.getUuid());
        assertThat((List<String>) JsonPath.read(prontuario, "$.atendimentos[*].procedimentos[*].tipo"))
                .containsExactlyInAnyOrder("Curativo", "Aplicação tópica de flúor (individual)");
        assertThat((List<Boolean>) JsonPath.read(prontuario, "$.atendimentos[*].procedimentos[?(@.tipo == 'Curativo')].odontologico")).containsExactly(false);
    }

    @Test
    void realizadoGravaOOdontogramaERetificacaoAnula() throws Exception {
        String resina = registrar("RESTAURACAO_RESINA", "REALIZADO", "\"dente\": 36, \"faces\": [\"DISTAL\", \"OCLUSAL\"], \"tipo\": \"ignorado\"");
        String odontograma = ler(URL + "paciente/" + paciente.getUuid() + "/odontograma");
        assertThat((List<String>) JsonPath.read(odontograma, "$.dentes[?(@.dente == 36)].faces[*].face")).containsExactly("DISTAL", "OCLUSAL");
        assertThat((List<String>) JsonPath.read(odontograma, "$.dentes[?(@.dente == 36)].faces[*].registro.procedimentoNome"))
                .containsExactly("Restauração em resina composta", "Restauração em resina composta");
        assertThat(JsonPath.<String>read(ler(URL + "procedimento/" + resina), "$.tipo")).isEqualTo("Restauração em resina composta");
        assertThat((List<String>) JsonPath.read(ler(URL + "procedimento/" + resina), "$.faces")).containsExactly("DISTAL", "OCLUSAL");

        // O registro vem do procedimento: corrige-se retificando o procedimento, não anulando o registro.
        String registro = ((List<String>) JsonPath.read(odontograma, "$.dentes[?(@.dente == 36)].faces[0].registro.uuid")).get(0);
        enviar(post(URL + "registro-odontograma/" + registro + "/anulacao"), "{\"motivo\": \"Errado\"}", 422);

        // Agendado não grava; o desfecho realizado grava.
        String exodontia = registrar("EXODONTIA", "AGENDADO", "\"dente\": 48");
        assertThat((List<Integer>) JsonPath.read(ler(URL + "paciente/" + paciente.getUuid() + "/odontograma"), "$.dentes[*].dente")).containsExactly(36);
        enviar(post(URL + "procedimento/" + exodontia + "/status"), ("{\"status\": \"REALIZADO\", \"dataRealizacao\": \"%s\", "
                + "\"profissionalMatricula\": \"%s\"}").formatted(LocalDateTime.now().minusMinutes(5).truncatedTo(ChronoUnit.SECONDS), MATRICULA), 200);
        assertThat((List<String>) JsonPath.read(ler(URL + "paciente/" + paciente.getUuid() + "/odontograma"),
                "$.dentes[?(@.dente == 48)].condicaoDente.condicaoCodigo")).containsExactly("AUSENTE");

        // Retificar o dente da restauração: os registros do 36 são anulados e o 37 recebe os novos.
        enviar(post(URL + "procedimento/" + resina + "/retificacao"), procedimento(atendimento.getUuid(), "RESTAURACAO_RESINA", "REALIZADO",
                "\"dente\": 37, \"faces\": [\"OCLUSAL\"], \"motivoRetificacao\": \"Dente errado\""), 201);
        String depois = ler(URL + "paciente/" + paciente.getUuid() + "/odontograma");
        assertThat((List<Integer>) JsonPath.read(depois, "$.dentes[*].dente")).containsExactly(37, 48);
        String historico = ler(URL + "paciente/" + paciente.getUuid() + "/odontograma/dente/36");
        assertThat((List<Boolean>) JsonPath.read(historico, "$[*].anulado")).containsExactly(true, true);
        assertThat((List<String>) JsonPath.read(historico, "$[*].motivoAnulacao")).allMatch(m -> m.startsWith("Procedimento retificado"));

        String prontuario = ler(URL + "prontuario/" + paciente.getUuid());
        assertThat((List<Boolean>) JsonPath.read(prontuario, "$.atendimentos[?(@.atendimento.uuid == '" + atendimento.getUuid() + "')].procedimentos[*].retificado"))
                .containsExactly(true, false, false);
    }
}
