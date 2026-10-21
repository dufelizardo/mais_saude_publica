package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Saúde Bucal, fatia SB1 (ADR-0134): o catálogo de especialidades, a especialidade no atendimento, no bloco da agenda e
 * na marcação (herdada do bloco e passada ao atendimento), e a avaliação odontológica com a retificação e o prontuário.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SaudeBucalControllerTest {

    private static final String PREFIXO = "SaudeBucal Teste ";
    private static final String MATRICULA = "SB1-DENTISTA";
    private static final String URL = "/api/v1/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;

    private UnidadeDeSaude ubs;
    private Paciente paciente;
    private String odontologia;

    @BeforeEach
    void seed() throws Exception {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setCpf("90055500001");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        Profissional d = new Profissional();
        d.setMatricula(MATRICULA);
        d.setCpf("90055500009");
        d.setNome(PREFIXO + "Dentista");
        d.setAtivo(true);
        profissionalRepository.save(d);
        odontologia = ((List<String>) JsonPath.read(ler(URL + "especialidades"), "$[?(@.codigo == 'ODONTOLOGIA')].uuid")).get(0);
    }

    @AfterEach
    void limpar() {
        String atendimentos = "(select uuid from tb_atendimento where paciente_id in (select uuid from tb_paciente where nome like '" + PREFIXO + "%'))";
        jdbc.update("update tb_avaliacao_odontologica set retificacao_de_id = null where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_avaliacao_odontologica where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_atendimento where uuid in " + atendimentos);
        jdbc.update("delete from tb_agendamento where paciente_id in (select uuid from tb_paciente where nome like '" + PREFIXO + "%')");
        jdbc.update("delete from tb_bloco_agenda where profissional_id in (select uuid from tb_profissional where matricula = '" + MATRICULA + "')");
        jdbc.update("update tb_atendimento set especialidade_id = null where especialidade_id in (select uuid from tb_especialidade where codigo like 'TSTSB1%')");
        jdbc.update("delete from tb_especialidade where codigo like 'TSTSB1%'");
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

    /** Abre o atendimento e devolve o DTO dele (o POST responde só a mensagem de sucesso); com erro, a resposta. */
    private String atendimento(String especialidade, String agendamento, int esperado) throws Exception {
        Set<UUID> antes = atendimentoRepository.findByPacienteUuid(paciente.getUuid()).stream()
                .map(Atendimento::getUuid).collect(Collectors.toSet());
        String r = enviar(post(URL + "atendimento/"), ("{\"pacienteId\": \"%s\", \"profissionalMatricula\": \"%s\", \"unidadeId\": \"%s\", "
                + "\"tipo\": \"CONSULTA\", \"status\": \"EM_ANDAMENTO\", \"dataHora\": \"2026-01-01T08:00:00\"%s%s}").formatted(paciente.getUuid(),
                MATRICULA, ubs.getUuid(), especialidade == null ? "" : ", \"especialidadeId\": \"" + especialidade + "\"",
                agendamento == null ? "" : ", \"agendamentoId\": \"" + agendamento + "\""), esperado);
        if (esperado != 201) {
            return r;
        }
        UUID novo = atendimentoRepository.findByPacienteUuid(paciente.getUuid()).stream()
                .map(Atendimento::getUuid).filter(id -> !antes.contains(id)).findFirst().orElseThrow();
        return ler(URL + "atendimento/" + novo);
    }

    private String avaliacao(String atendimento, String extra) {
        return ("{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"%s\", \"queixaPrincipal\": \"Dor no dente\", \"diagnostico\": \"Cárie no 36\", "
                + "\"risco\": \"MEDIO\", \"necessidadeTratamento\": \"PRIORITARIA\"%s}").formatted(atendimento, MATRICULA, extra);
    }

    @Test
    void catalogoDeEspecialidades() throws Exception {
        assertThat((List<Boolean>) JsonPath.read(ler(URL + "especialidades"), "$[?(@.codigo == 'ODONTOLOGIA')].odontologica")).containsExactly(true);
        enviar(post(URL + "especialidade"), "{\"codigo\": \"ODONTOLOGIA\", \"nome\": \"Outra\"}", 409);
        enviar(post(URL + "especialidade"), "{\"codigo\": \"minusculo\", \"nome\": \"X\"}", 400);
        String r = enviar(post(URL + "especialidade"), "{\"codigo\": \"TSTSB1_ODONTOPEDIATRIA\", \"nome\": \"Odontopediatria\", \"odontologica\": true}", 201);
        String id = JsonPath.read(r, "$.uuid");
        enviar(put(URL + "especialidade/" + id), "{\"codigo\": \"TSTSB1_OUTRO\", \"nome\": \"Odontopediatria\"}", 422);
        r = enviar(put(URL + "especialidade/" + id), "{\"codigo\": \"TSTSB1_ODONTOPEDIATRIA\", \"nome\": \"Odontopediatria\", \"odontologica\": true, "
                + "\"ativa\": false}", 200);
        assertThat(JsonPath.<Boolean>read(r, "$.ativa")).isFalse();
        // Especialidade inativa não entra no atendimento novo.
        atendimento(id, null, 422);
    }

    @Test
    void avaliacaoOdontologicaComRetificacaoENoProntuario() throws Exception {
        String semEspecialidade = JsonPath.read(atendimento(null, null, 201), "$.uuid");
        enviar(post(URL + "avaliacao-odontologica"), avaliacao(semEspecialidade, ""), 422);
        String r = atendimento(odontologia, null, 201);
        assertThat(JsonPath.<String>read(r, "$.especialidadeNome")).isEqualTo("Odontologia");
        assertThat(JsonPath.<Boolean>read(r, "$.especialidadeOdontologica")).isTrue();
        String atendimento = JsonPath.read(r, "$.uuid");
        enviar(post(URL + "avaliacao-odontologica"), "{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"%s\"}".formatted(atendimento, MATRICULA), 400);
        r = enviar(post(URL + "avaliacao-odontologica"), avaliacao(atendimento, ""), 201);
        String original = JsonPath.read(r, "$.uuid");
        assertThat(JsonPath.<String>read(r, "$.risco")).isEqualTo("MEDIO");

        // Retificação (ADR-0062): pede o motivo e o mesmo atendimento; só a vigente se retifica.
        enviar(post(URL + "avaliacao-odontologica/" + original + "/retificacao"), avaliacao(atendimento, ""), 400);
        enviar(post(URL + "avaliacao-odontologica/" + original + "/retificacao"), avaliacao(semEspecialidade, ", \"motivoRetificacao\": \"Dente errado\""), 400);
        r = enviar(post(URL + "avaliacao-odontologica/" + original + "/retificacao"), avaliacao(atendimento,
                ", \"diagnostico\": \"Cárie no 46\", \"motivoRetificacao\": \"Dente errado\"").replace("\"diagnostico\": \"Cárie no 36\", ", ""), 201);
        String nova = JsonPath.read(r, "$.uuid");
        assertThat(JsonPath.<String>read(r, "$.retificacaoDeUuid")).isEqualTo(original);
        enviar(post(URL + "avaliacao-odontologica/" + original + "/retificacao"), avaliacao(atendimento, ", \"motivoRetificacao\": \"De novo\""), 422);
        r = ler(URL + "avaliacao-odontologica/" + original);
        assertThat(JsonPath.<Boolean>read(r, "$.retificado")).isTrue();
        assertThat(JsonPath.<String>read(r, "$.retificadoPorUuid")).isEqualTo(nova);

        // As duas versões no prontuário, no atendimento de odontologia.
        String prontuario = ler(URL + "prontuario/" + paciente.getUuid());
        assertThat((List<String>) JsonPath.read(prontuario, "$.atendimentos[*].avaliacoesOdontologicas[*].diagnostico"))
                .containsExactlyInAnyOrder("Cárie no 36", "Cárie no 46");
    }

    @Test
    void especialidadeDoBlocoNaMarcacaoENoAtendimento() throws Exception {
        LocalDate segunda = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        enviar(post(URL + "agenda/bloco"), ("{\"profissionalMatricula\": \"%s\", \"unidadeId\": \"%s\", \"diaSemana\": \"MONDAY\", "
                + "\"horaInicio\": \"08:00\", \"horaFim\": \"10:00\", \"duracaoMinutos\": 30, \"tipo\": \"CONSULTA\", \"especialidadeId\": \"%s\"}")
                .formatted(MATRICULA, ubs.getUuid(), odontologia), 201);
        assertThat((List<String>) JsonPath.read(ler(URL + "agenda/bloco?profissionalMatricula=" + MATRICULA), "$[*].especialidadeNome"))
                .containsExactly("Odontologia");
        String agenda = ler(URL + "agenda/?profissionalMatricula=" + MATRICULA + "&unidadeId=" + ubs.getUuid() + "&de=" + segunda + "&ate=" + segunda);
        assertThat((List<String>) JsonPath.read(agenda, "$.dias[0].itens[?(@.tipo == 'VAGA')].especialidadeNome")).hasSize(4).containsOnly("Odontologia");

        // A marcação na vaga herda a especialidade do bloco; o atendimento dela, da marcação.
        enviar(post(URL + "agendamento/"), ("{\"pacienteId\": \"%s\", \"profissionalMatricula\": \"%s\", \"dataHora\": \"%sT08:30:00\", "
                + "\"status\": \"AGENDADO\", \"tipo\": \"CONSULTA\", \"unidadeId\": \"%s\"}").formatted(paciente.getUuid(), MATRICULA, segunda, ubs.getUuid()), 201);
        agenda = ler(URL + "agenda/?profissionalMatricula=" + MATRICULA + "&unidadeId=" + ubs.getUuid() + "&de=" + segunda + "&ate=" + segunda);
        assertThat((List<String>) JsonPath.read(agenda, "$.dias[0].itens[?(@.tipo == 'MARCACAO')].especialidadeNome")).containsExactly("Odontologia");
        String agendamento = ((List<String>) JsonPath.read(agenda, "$.dias[0].itens[?(@.tipo == 'MARCACAO')].agendamentoId")).get(0);
        String r = atendimento(null, agendamento, 201);
        assertThat(JsonPath.<String>read(r, "$.especialidadeNome")).isEqualTo("Odontologia");
    }
}
