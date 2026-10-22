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
 * Saúde bucal coletiva, fatia SB5 (ADR-0140): o detalhe odontológico da ação coletiva dos programas Saúde Bucal e PSE,
 * a avaliação por participante na ação realizada e a lista a convocar sem quem já tem plano aberto.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SaudeBucalColetivaControllerTest {

    private static final String PREFIXO = "SbColetiva Teste ";
    private static final String MATRICULA = "SB5-DENTISTA";
    private static final String URL = "/api/v1/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;
    @Autowired private EspecialidadeRepository especialidadeRepository;

    private UnidadeDeSaude ubs;
    private Paciente ana;
    private Paciente bruno;
    private Paciente caio;
    private Profissional dentista;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        jdbc.update("insert into tb_participacao_programa (uuid, programa_id, unidade_id, inicio) select gen_random_uuid(), uuid, ?, ? from tb_programa "
                + "where codigo in ('SAUDE_BUCAL', 'SAUDE_NA_ESCOLA', 'HIPERDIA')", ubs.getUuid(), LocalDate.now().minusYears(1));
        ana = paciente("Ana", "90033300001");
        bruno = paciente("Bruno", "90033300002");
        caio = paciente("Caio", "90033300003");
        dentista = new Profissional();
        dentista.setMatricula(MATRICULA);
        dentista.setCpf("90033300009");
        dentista.setNome(PREFIXO + "Dentista");
        dentista.setAtivo(true);
        dentista = profissionalRepository.save(dentista);
    }

    @AfterEach
    void limpar() {
        limparPorPrefixo(jdbc, PREFIXO, MATRICULA);
    }

    static void limparPorPrefixo(JdbcTemplate jdbc, String prefixo, String matricula) {
        String pacientes = "(select uuid from tb_paciente where nome like '" + prefixo + "%')";
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + prefixo + "%')";
        String acoes = "(select uuid from tb_acao_programatica where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_avaliacao_bucal_coletiva where acao_id in " + acoes);
        jdbc.update("delete from tb_acao_saude_bucal_atividade where detalhe_id in (select uuid from tb_acao_saude_bucal where acao_id in " + acoes + ")");
        jdbc.update("delete from tb_acao_saude_bucal where acao_id in " + acoes);
        jdbc.update("delete from tb_acao_programatica_participante where acao_id in " + acoes);
        jdbc.update("delete from tb_acao_programatica where uuid in " + acoes);
        jdbc.update("delete from tb_item_plano_tratamento where plano_id in (select uuid from tb_plano_tratamento where paciente_id in " + pacientes + ")");
        jdbc.update("delete from tb_plano_tratamento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_evento_acompanhamento where acompanhamento_id in (select uuid from tb_acompanhamento_programatico where paciente_id in "
                + pacientes + ")");
        jdbc.update("delete from tb_acompanhamento_programatico where paciente_id in " + pacientes);
        jdbc.update("delete from tb_participacao_programa where unidade_id in " + unidades);
        jdbc.update("delete from tb_atendimento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_paciente where nome like '" + prefixo + "%'");
        jdbc.update("delete from tb_profissional where matricula = '" + matricula + "'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + prefixo + "%'");
    }

    private Paciente paciente(String nome, String cpf) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private String programa(String codigo) {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_programa where codigo = ?", String.class, codigo);
    }

    private void inscrever(String programa, Paciente p) throws Exception {
        enviar(post(URL + "programa/" + programa + "/acompanhamento"), "{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\"}".formatted(p.getUuid(), ubs.getUuid()), 201);
    }

    /** Ação coletiva prevista para ontem, com os participantes; devolve o id. */
    private String acaoColetiva(String programa, String participantes) throws Exception {
        String r = enviar(post(URL + "acao-programatica"), ("{\"programaId\": \"%s\", \"tipo\": \"GRUPO\", \"unidadeId\": \"%s\", \"local\": \"Escola Municipal\", "
                + "\"dataPrevista\": \"%s\", \"participantesIds\": [%s]}").formatted(programa, ubs.getUuid(), LocalDate.now().minusDays(1), participantes), 201);
        return JsonPath.read(r, "$.uuid");
    }

    private static final String DETALHE = "{\"atividades\": [\"ESCOVACAO_SUPERVISIONADA\", \"AVALIACAO_COLETIVA\"], \"localTipo\": \"ESCOLA\", "
            + "\"instituicao\": \"Escola Municipal Rui Barbosa\", \"publicoAlvo\": \"1º ao 5º ano\", \"kitsDistribuidos\": 40}";

    private String resultado(Paciente p, String risco, String necessidade) {
        return "{\"pacienteId\": \"%s\", \"risco\": \"%s\", \"necessidadeTratamento\": \"%s\"}".formatted(p.getUuid(), risco, necessidade);
    }

    @Test
    void detalheSoNaAcaoColetivaDoProgramaOdontologico() throws Exception {
        String hiperdia = programa("HIPERDIA");
        String doHiperdia = acaoColetiva(hiperdia, "");
        enviar(put(URL + "acao-programatica/" + doHiperdia + "/saude-bucal"), DETALHE, 422);

        String pse = programa("SAUDE_NA_ESCOLA");
        String acao = acaoColetiva(pse, "");
        assertThat(JsonPath.<Boolean>read(ler(URL + "acao-programatica/" + acao + "/saude-bucal"), "$.preenchido")).isFalse();
        enviar(put(URL + "acao-programatica/" + acao + "/saude-bucal"), "{\"atividades\": [], \"localTipo\": \"ESCOLA\"}", 400);
        enviar(put(URL + "acao-programatica/" + acao + "/saude-bucal"), "{\"atividades\": [\"APLICACAO_TOPICA_FLUOR\"], \"localTipo\": \"ESCOLA\", "
                + "\"kitsDistribuidos\": -1}", 400);
        String r = enviar(put(URL + "acao-programatica/" + acao + "/saude-bucal"), DETALHE, 200);
        assertThat((List<String>) JsonPath.read(r, "$.atividades")).containsExactly("ESCOVACAO_SUPERVISIONADA", "AVALIACAO_COLETIVA");
        assertThat(JsonPath.<Integer>read(r, "$.kitsDistribuidos")).isEqualTo(40);
        // Corrigir o detalhe substitui.
        r = enviar(put(URL + "acao-programatica/" + acao + "/saude-bucal"), "{\"atividades\": [\"EDUCACAO_EM_SAUDE\"], \"localTipo\": \"CRECHE\"}", 200);
        assertThat((List<String>) JsonPath.read(r, "$.atividades")).containsExactly("EDUCACAO_EM_SAUDE");

        // A ação cancelada não recebe o detalhe.
        enviar(post(URL + "acao-programatica/" + acao + "/situacao"), "{\"situacao\": \"CANCELADA\", \"motivo\": \"Chuva\"}", 200);
        enviar(put(URL + "acao-programatica/" + acao + "/saude-bucal"), DETALHE, 422);
    }

    @Test
    void avaliacaoPorParticipanteEListaAConvocar() throws Exception {
        String bucal = programa("SAUDE_BUCAL");
        inscrever(bucal, ana);
        inscrever(bucal, bruno);
        inscrever(bucal, caio);
        String semLista = acaoColetiva(bucal, "");
        enviar(post(URL + "acao-programatica/" + semLista + "/realizacao"), "{\"quantidadeAtendida\": 30}", 200);
        enviar(post(URL + "acao-programatica/" + semLista + "/avaliacao-bucal"), "{\"resultados\": [%s]}".formatted(resultado(ana, "ALTO", "URGENCIA")), 422);

        String acao = acaoColetiva(bucal, "\"%s\", \"%s\", \"%s\"".formatted(ana.getUuid(), bruno.getUuid(), caio.getUuid()));
        // Antes da realização, não.
        enviar(post(URL + "acao-programatica/" + acao + "/avaliacao-bucal"), "{\"resultados\": [%s]}".formatted(resultado(ana, "ALTO", "URGENCIA")), 422);
        enviar(post(URL + "acao-programatica/" + acao + "/realizacao"), "{}", 200);
        Paciente fora = paciente("Fora", "90033300004");
        enviar(post(URL + "acao-programatica/" + acao + "/avaliacao-bucal"), "{\"resultados\": [%s]}".formatted(resultado(fora, "ALTO", "URGENCIA")), 422);
        String r = enviar(post(URL + "acao-programatica/" + acao + "/avaliacao-bucal"), "{\"resultados\": [%s, %s, %s]}".formatted(
                resultado(ana, "MEDIO", "ELETIVA"), resultado(bruno, "BAIXO", "SEM_NECESSIDADE"), resultado(caio, "ALTO", "URGENCIA")), 200);
        assertThat((List<?>) JsonPath.read(r, "$.avaliacoes")).hasSize(3);

        // A convocar: quem precisa de tratamento, a urgência primeiro; o Bruno não precisa.
        String lista = ler(URL + "saude-bucal/a-convocar?unidadeId=" + ubs.getUuid());
        assertThat((List<String>) JsonPath.read(lista, "$[*].pacienteNome")).containsExactly(PREFIXO + "Caio", PREFIXO + "Ana");

        // Corrigir o resultado substitui; com o plano aberto, a Ana sai da lista.
        r = enviar(post(URL + "acao-programatica/" + acao + "/avaliacao-bucal"), "{\"resultados\": [%s]}".formatted(resultado(ana, "ALTO", "PRIORITARIA")), 200);
        assertThat((List<String>) JsonPath.read(r, "$.avaliacoes[?(@.pacienteNome == '" + PREFIXO + "Ana')].necessidadeTratamento"))
                .containsExactly("PRIORITARIA");
        Atendimento at = new Atendimento(ana, dentista, ubs, null, null, TipoAtendimento.CONSULTA, StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now());
        at.setEspecialidade(especialidadeRepository.findAll().stream().filter(e -> e.getCodigo().equals("ODONTOLOGIA")).findFirst().orElseThrow());
        at = atendimentoRepository.save(at);
        enviar(post(URL + "plano-tratamento"), "{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"%s\", \"prioridade\": \"PRIORITARIA\"}"
                .formatted(at.getUuid(), MATRICULA), 201);
        lista = ler(URL + "saude-bucal/a-convocar?unidadeId=" + ubs.getUuid());
        assertThat((List<String>) JsonPath.read(lista, "$[*].pacienteNome")).containsExactly(PREFIXO + "Caio");
    }
}
