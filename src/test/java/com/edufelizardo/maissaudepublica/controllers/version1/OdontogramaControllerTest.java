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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Odontograma, fatia SB2 (ADR-0135): o catálogo de condições, as marcações na avaliação com a notação FDI, a face e o
 * alcance da condição, a projeção por data (o dente inteiro por cima das faces), a anulação e o histórico do dente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OdontogramaControllerTest {

    private static final String PREFIXO = "Odontograma Teste ";
    private static final String MATRICULA = "SB2-DENTISTA";
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
        paciente.setCpf("90077700001");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        Profissional d = new Profissional();
        d.setMatricula(MATRICULA);
        d.setCpf("90077700009");
        d.setNome(PREFIXO + "Dentista");
        d.setAtivo(true);
        d = profissionalRepository.save(d);
        atendimento = new Atendimento(paciente, d, ubs, null, null, TipoAtendimento.CONSULTA, StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now());
        atendimento.setEspecialidade(especialidadeRepository.findAll().stream().filter(e -> e.getCodigo().equals("ODONTOLOGIA")).findFirst().orElseThrow());
        atendimento = atendimentoRepository.save(atendimento);
    }

    @AfterEach
    void limpar() {
        String pacientes = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        String atendimentos = "(select uuid from tb_atendimento where paciente_id in " + pacientes + ")";
        jdbc.update("delete from tb_registro_odontograma where paciente_id in " + pacientes);
        jdbc.update("update tb_avaliacao_odontologica set retificacao_de_id = null where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_avaliacao_odontologica where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_atendimento where uuid in " + atendimentos);
        jdbc.update("delete from tb_condicao_odontologica where codigo like 'TSTSB2%'");
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

    private String condicao(String codigo) throws Exception {
        return ((List<String>) JsonPath.read(ler(URL + "condicoes-odontologicas"), "$[?(@.codigo == '" + codigo + "')].uuid")).get(0);
    }

    private String avaliacao() throws Exception {
        return JsonPath.read(enviar(post(URL + "avaliacao-odontologica"), ("{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"%s\", "
                + "\"risco\": \"MEDIO\", \"necessidadeTratamento\": \"PRIORITARIA\"}").formatted(atendimento.getUuid(), MATRICULA), 201), "$.uuid");
    }

    private String marcacao(int dente, String face, String condicao) {
        return "{\"dente\": %d, %s\"condicaoId\": \"%s\"}".formatted(dente, face == null ? "" : "\"face\": \"" + face + "\", ", condicao);
    }

    @Test
    void catalogoDeCondicoes() throws Exception {
        assertThat((List<String>) JsonPath.read(ler(URL + "condicoes-odontologicas"), "$[?(@.codigo == 'CARIE')].alcance")).containsExactly("FACE");
        enviar(post(URL + "condicao-odontologica"), "{\"codigo\": \"CARIE\", \"nome\": \"X\", \"alcance\": \"FACE\", \"cor\": \"#000000\", \"sigla\": \"X\"}", 409);
        enviar(post(URL + "condicao-odontologica"), "{\"codigo\": \"TSTSB2_X\", \"nome\": \"X\", \"alcance\": \"FACE\", \"cor\": \"vermelho\", \"sigla\": \"X\"}", 400);
        String r = enviar(post(URL + "condicao-odontologica"), "{\"codigo\": \"TSTSB2_MANCHA\", \"nome\": \"Mancha branca\", \"alcance\": \"FACE\", "
                + "\"cor\": \"#fef3c7\", \"sigla\": \"mb\"}", 201);
        assertThat(JsonPath.<String>read(r, "$.cor")).isEqualTo("#FEF3C7");
        assertThat(JsonPath.<String>read(r, "$.sigla")).isEqualTo("MB");
        enviar(put(URL + "condicao-odontologica/" + JsonPath.read(r, "$.uuid")), "{\"codigo\": \"TSTSB2_OUTRA\", \"nome\": \"X\", \"alcance\": \"FACE\", "
                + "\"cor\": \"#000000\", \"sigla\": \"X\"}", 422);
    }

    @Test
    void marcacoesComFdiFaceEAlcance() throws Exception {
        String av = avaliacao();
        String carie = condicao("CARIE");
        String ausente = condicao("AUSENTE");
        String fratura = condicao("FRATURA");
        String url = URL + "avaliacao-odontologica/" + av + "/odontograma";
        enviar(post(url), "{\"marcacoes\": [%s]}".formatted(marcacao(19, "OCLUSAL", carie)), 400);
        enviar(post(url), "{\"marcacoes\": [%s]}".formatted(marcacao(56, null, ausente)), 400);
        enviar(post(url), "{\"marcacoes\": [%s]}".formatted(marcacao(36, null, carie)), 422);
        enviar(post(url), "{\"marcacoes\": [%s]}".formatted(marcacao(36, "MESIAL", ausente)), 422);
        enviar(post(url), "{\"marcacoes\": [%s]}".formatted(marcacao(11, "OCLUSAL", carie)), 422);
        enviar(post(url), "{\"marcacoes\": [%s]}".formatted(marcacao(36, "INCISAL", carie)), 422);
        enviar(post(url), "{\"data\": \"%s\", \"marcacoes\": [%s]}".formatted(LocalDate.now().plusDays(1), marcacao(36, "OCLUSAL", carie)), 400);
        String r = enviar(post(url), "{\"marcacoes\": [%s, %s, %s, %s]}".formatted(marcacao(36, "OCLUSAL", carie), marcacao(11, "INCISAL", fratura),
                marcacao(85, "DISTAL", carie), marcacao(48, null, ausente)), 201);
        assertThat((List<Integer>) JsonPath.read(r, "$.dentes[*].dente")).containsExactly(11, 36, 48, 85);
        assertThat((List<String>) JsonPath.read(r, "$.dentes[?(@.dente == 36)].faces[*].registro.condicaoCodigo")).containsExactly("CARIE");
        assertThat((List<String>) JsonPath.read(r, "$.dentes[?(@.dente == 48)].condicaoDente.condicaoCodigo")).containsExactly("AUSENTE");
    }

    @Test
    void projecaoPorDataAnulacaoEHistorico() throws Exception {
        String av = avaliacao();
        String url = URL + "avaliacao-odontologica/" + av + "/odontograma";
        LocalDate hoje = LocalDate.now();
        enviar(post(url), "{\"data\": \"%s\", \"marcacoes\": [%s]}".formatted(hoje.minusDays(14), marcacao(36, "OCLUSAL", condicao("CARIE"))), 201);
        enviar(post(url), "{\"data\": \"%s\", \"marcacoes\": [%s]}".formatted(hoje.minusDays(7), marcacao(36, "OCLUSAL", condicao("RESTAURADO"))), 201);
        String r = enviar(post(url), "{\"marcacoes\": [%s]}".formatted(marcacao(36, null, condicao("EXTRACAO_INDICADA"))), 201);

        // Hoje: a condição do dente inteiro vale por cima das faces registradas antes.
        assertThat((List<String>) JsonPath.read(r, "$.dentes[?(@.dente == 36)].condicaoDente.condicaoCodigo")).containsExactly("EXTRACAO_INDICADA");
        assertThat((List<List<?>>) JsonPath.read(r, "$.dentes[?(@.dente == 36)].faces")).containsExactly(List.of());
        // Como estava antes: há 10 dias, a cárie; há 3, a restauração.
        String antes = ler(URL + "paciente/" + paciente.getUuid() + "/odontograma?data=" + hoje.minusDays(10));
        assertThat((List<String>) JsonPath.read(antes, "$.dentes[?(@.dente == 36)].faces[*].registro.condicaoCodigo")).containsExactly("CARIE");
        String depois = ler(URL + "paciente/" + paciente.getUuid() + "/odontograma?data=" + hoje.minusDays(3));
        assertThat((List<String>) JsonPath.read(depois, "$.dentes[?(@.dente == 36)].faces[*].registro.condicaoCodigo")).containsExactly("RESTAURADO");

        // Anular a extração indicada: sai da projeção, fica no histórico; anular de novo não pode.
        String historico = ler(URL + "paciente/" + paciente.getUuid() + "/odontograma/dente/36");
        assertThat((List<?>) JsonPath.read(historico, "$")).hasSize(3);
        String extracao = ((List<String>) JsonPath.read(historico, "$[?(@.condicaoCodigo == 'EXTRACAO_INDICADA')].uuid")).get(0);
        enviar(post(URL + "registro-odontograma/" + extracao + "/anulacao"), "{}", 400);
        enviar(post(URL + "registro-odontograma/" + extracao + "/anulacao"), "{\"motivo\": \"Dente errado\"}", 200);
        enviar(post(URL + "registro-odontograma/" + extracao + "/anulacao"), "{\"motivo\": \"De novo\"}", 422);
        String atual = ler(URL + "paciente/" + paciente.getUuid() + "/odontograma");
        assertThat((List<String>) JsonPath.read(atual, "$.dentes[?(@.dente == 36)].faces[*].registro.condicaoCodigo")).containsExactly("RESTAURADO");
        assertThat((List<Boolean>) JsonPath.read(ler(URL + "paciente/" + paciente.getUuid() + "/odontograma/dente/36"), "$[*].anulado"))
                .containsExactly(false, false, true);
        mockMvc.perform(get(URL + "paciente/" + paciente.getUuid() + "/odontograma/dente/99")).andExpect(status().isBadRequest());

        // A avaliação retificada não recebe marcação: marca-se na vigente.
        enviar(post(URL + "avaliacao-odontologica/" + av + "/retificacao"), ("{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"%s\", "
                + "\"risco\": \"ALTO\", \"necessidadeTratamento\": \"URGENCIA\", \"motivoRetificacao\": \"Risco\"}").formatted(atendimento.getUuid(), MATRICULA), 201);
        enviar(post(url), "{\"marcacoes\": [%s]}".formatted(marcacao(11, null, condicao("HIGIDO"))), 422);
    }
}
