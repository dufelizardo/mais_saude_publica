package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Emergências, fatia E4b (ADR-0194): onde tem o medicamento e o material na rede (a afetada marcada, a reserva descontada); a mobilização
 * com o medicamento do catálogo; os compromissos nas unidades afetadas fora de operação; a prioridade da emergência na Regulação (fila,
 * 409, 422, retirada, fim com o encerramento) e o relatório.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RedeEmergenciaControllerTest {

    private static final String PREFIXO = "RedeEmerg Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude hospital;
    private Paciente deslocado;
    private Paciente outro;
    private Profissional medico;
    private String procedimento;
    private String medicamento;
    private String material;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS Ribeirinha");
        hospital = unidade("Hospital");
        deslocado = paciente("Deslocado", "90188800001");
        outro = paciente("Outro", "90188800002");
        Profissional p = new Profissional();
        p.setMatricula("RDE-MED");
        p.setCpf("90188800099");
        p.setNome(PREFIXO + "Médico");
        p.setAtivo(true);
        medico = profissionalRepository.save(p);
        procedimento = jdbc.queryForObject("insert into tb_procedimento_regulado (uuid, nome, tipo, ativo) values (gen_random_uuid(), ?, "
                + "'CONSULTA_ESPECIALIZADA', true) returning cast(uuid as varchar)", String.class, PREFIXO + "Cardiologia");
        medicamento = jdbc.queryForObject("insert into tb_medicamento (uuid, nome, ativo) values (gen_random_uuid(), ?, true) returning cast(uuid as varchar)",
                String.class, PREFIXO + "Soro fisiológico");
        jdbc.update("insert into tb_lote (uuid, medicamento_id, unidade_id, numero_lote, validade, quantidade) values (gen_random_uuid(), cast(? as uuid), ?, 'S1', ?, 40)",
                medicamento, hospital.getUuid(), LocalDate.now().plusDays(100));
        jdbc.update("insert into tb_lote (uuid, medicamento_id, unidade_id, numero_lote, validade, quantidade) values (gen_random_uuid(), cast(? as uuid), ?, 'S2', ?, 10)",
                medicamento, ubs.getUuid(), LocalDate.now().plusDays(50));
        jdbc.update("insert into tb_lote (uuid, medicamento_id, unidade_id, numero_lote, validade, quantidade) values (gen_random_uuid(), cast(? as uuid), ?, 'VENC', ?, 99)",
                medicamento, ubs.getUuid(), LocalDate.now().minusDays(1));
        material = jdbc.queryForObject("insert into tb_material_estoque (uuid, codigo, nome, categoria, unidade_medida, controla_lote, ativo) values "
                + "(gen_random_uuid(), 'RDE-KIT', ?, 'MEDICO_HOSPITALAR', 'UNIDADE', false, true) returning cast(uuid as varchar)", String.class, PREFIXO + "Kit");
        jdbc.update("insert into tb_lote_material (uuid, material_id, unidade_id, quantidade, criado_em) values (gen_random_uuid(), cast(? as uuid), ?, 30, now())",
                material, hospital.getUuid());
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        String pacientes = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String relatorios = "(select uuid from tb_relatorio_pos_evento where emergencia_id in " + emergencias + ")";
        jdbc.update("delete from tb_solicitacao_regulacao where paciente_id in " + pacientes);
        jdbc.update("delete from tb_agendamento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_paciente_deslocado where paciente_id in " + pacientes);
        jdbc.update("delete from tb_reserva_estoque where unidade_id in " + unidades);
        jdbc.update("delete from tb_recomendacao_relatorio where relatorio_id in " + relatorios);
        jdbc.update("delete from tb_relatorio_pos_evento where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_desmobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_avaliacao_impacto_unidade where avaliacao_id in (select uuid from tb_avaliacao_impacto where emergencia_id in " + emergencias + ")");
        jdbc.update("delete from tb_avaliacao_impacto where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_lote where unidade_id in " + unidades);
        jdbc.update("delete from tb_medicamento where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_lote_material where unidade_id in " + unidades);
        jdbc.update("delete from tb_material_estoque where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_procedimento_regulado where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = 'RDE-MED'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private Paciente paciente(String nome, String cpf) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setDataNascimento(LocalDate.now().minusYears(50));
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private String regulacao(Paciente p, String prioridade, String status, UnidadeDeSaude executante, LocalDateTime quando, int minutosAtras) {
        return jdbc.queryForObject("insert into tb_solicitacao_regulacao (uuid, cid, justificativa, prioridade, solicitado_em, status, paciente_id, procedimento_id, "
                + "profissional_solicitante_id, unidade_solicitante_id, unidade_executante_id, data_hora_prevista) values (gen_random_uuid(), 'I20', 'Dor', ?, ?, ?, ?, "
                + "cast(? as uuid), ?, ?, ?, ?) returning cast(uuid as varchar)", String.class, prioridade,
                java.sql.Timestamp.from(Instant.now().minusSeconds(60L * minutosAtras)), status, p.getUuid(), procedimento, medico.getUuid(), hospital.getUuid(),
                executante != null ? executante.getUuid() : null, quando);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        if (corpo != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        var resposta = mockMvc.perform(r).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString(StandardCharsets.UTF_8)).isEqualTo(esperado);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    private String emergencia() throws Exception {
        String tipo = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'INUNDACAO'", String.class);
        String nivel = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        String evento = JsonPath.read(enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + tipo + "\", \"fonte\": \"DEFESA_CIVIL\", "
                + "\"descricao\": \"" + PREFIXO + "Cheia\"}", 201), "$.uuid");
        String em = JsonPath.read(enviar(post("/api/v1/evento-emergencial/" + evento + "/emergencia"), "{\"nome\": \"Cheia Rede\", \"nivelId\": \"" + nivel
                + "\", \"justificativa\": \"Rio acima da cota\"}", 201), "$.uuid");
        // A UBS ribeirinha é afetada e fica inoperante.
        enviar(post("/api/v1/emergencia/" + em + "/avaliacao-impacto"), "{\"nivelImpacto\": \"ALTO\", \"unidadeIds\": [\"" + ubs.getUuid() + "\"]}", 201);
        jdbc.update("update tb_unidade_de_saude set situacao_operacional = 'INOPERANTE' where uuid = ?", ubs.getUuid());
        return em;
    }

    @Test
    void ondeTemNaRede() throws Exception {
        String em = emergencia();
        enviar(get("/api/v1/emergencia/" + em + "/disponibilidade"), null, 400);
        enviar(get("/api/v1/emergencia/" + em + "/disponibilidade?medicamentoId=" + medicamento + "&materialId=" + material), null, 400);
        // O vencido fica de fora; a afetada vai para o fim.
        String r = enviar(get("/api/v1/emergencia/" + em + "/disponibilidade?medicamentoId=" + medicamento), null, 200);
        assertThat((List<String>) JsonPath.read(r, "$[*].unidadeNome")).containsExactly(PREFIXO + "Hospital", PREFIXO + "UBS Ribeirinha");
        assertThat((List<Integer>) JsonPath.read(r, "$[*].saldo")).containsExactly(40, 10);
        assertThat((List<Boolean>) JsonPath.read(r, "$[*].afetada")).containsExactly(false, true);
        // O material desconta a reserva.
        jdbc.update("insert into tb_reserva_estoque (uuid, emergencia_id, unidade_id, material_id, quantidade, consumida, situacao, justificativa, criada_em) "
                + "values (gen_random_uuid(), cast(? as uuid), ?, cast(? as uuid), 12, 0, 'ATIVA', 'Kits', now())", em, hospital.getUuid(), material);
        r = enviar(get("/api/v1/emergencia/" + em + "/disponibilidade?materialId=" + material), null, 200);
        assertThat((Integer) JsonPath.read(r, "$[0].reservado")).isEqualTo(12);
        assertThat((Integer) JsonPath.read(r, "$[0].livre")).isEqualTo(18);

        // A mobilização de medicamento leva o do catálogo; noutro tipo, 422.
        enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"INSUMO\", \"destinoId\": \"%s\", \"recurso\": \"X\", \"medicamentoId\": \"%s\"}"
                .formatted(hospital.getUuid(), medicamento), 422);
        r = enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"MEDICAMENTO\", \"destinoId\": \"%s\", \"medicamentoId\": \"%s\", \"quantidade\": 20}"
                .formatted(hospital.getUuid(), medicamento), 201);
        assertThat(JsonPath.parse(r).read("$.mobilizacoes[?(@.medicamentoId == '" + medicamento + "')].recurso", List.class))
                .containsExactly(PREFIXO + "Soro fisiológico");
    }

    @Test
    void compromissosEPrioridadeNaRegulacao() throws Exception {
        String em = emergencia();
        LocalDateTime amanha = LocalDateTime.now().plusDays(1).withNano(0);
        // Compromissos: agendamento e regulação agendada na UBS inoperante; os do hospital e os passados ficam de fora.
        jdbc.update("insert into tb_agendamento (uuid, data_hora, status, tipo, paciente_id, profissional_id, unidade_id, encaixe) values (gen_random_uuid(), ?, "
                + "'AGENDADO', 'CONSULTA', ?, ?, ?, false)", amanha, outro.getUuid(), medico.getUuid(), ubs.getUuid());
        jdbc.update("insert into tb_agendamento (uuid, data_hora, status, tipo, paciente_id, profissional_id, unidade_id, encaixe) values (gen_random_uuid(), ?, "
                + "'AGENDADO', 'CONSULTA', ?, ?, ?, false)", amanha, outro.getUuid(), medico.getUuid(), hospital.getUuid());
        jdbc.update("insert into tb_agendamento (uuid, data_hora, status, tipo, paciente_id, profissional_id, unidade_id, encaixe) values (gen_random_uuid(), ?, "
                + "'AGENDADO', 'CONSULTA', ?, ?, ?, false)", amanha.minusDays(3), outro.getUuid(), medico.getUuid(), ubs.getUuid());
        regulacao(outro, "VERDE", "AUTORIZADA", ubs, amanha.plusDays(2), 100);
        String r = enviar(get("/api/v1/emergencia/" + em + "/rede"), null, 200);
        assertThat((List<String>) JsonPath.read(r, "$.compromissos[*].tipo")).containsExactly("AGENDAMENTO", "REGULACAO");
        assertThat((List<String>) JsonPath.read(r, "$.compromissos[*].unidadeNome")).containsOnly(PREFIXO + "UBS Ribeirinha");

        // A prioridade: só de quem é da emergência (422), em aberto (422), uma vez (409).
        String antes = regulacao(outro, "AMARELO", "SOLICITADA", null, null, 60);
        String dele = regulacao(deslocado, "AMARELO", "SOLICITADA", null, null, 10);
        String realizada = regulacao(deslocado, "AMARELO", "REALIZADA", null, null, 30);
        enviar(post("/api/v1/emergencia/" + em + "/regulacao/" + dele + "/prioridade"), null, 422);
        enviar(post("/api/v1/emergencia/" + em + "/deslocado"), "{\"pacienteId\": \"" + deslocado.getUuid() + "\", \"destinoTipo\": \"OUTRO\", "
                + "\"destinoDescricao\": \"Escola\"}", 201);
        enviar(post("/api/v1/emergencia/" + em + "/regulacao/" + realizada + "/prioridade"), null, 422);
        enviar(post("/api/v1/emergencia/" + em + "/regulacao/" + antes + "/prioridade"), null, 422);
        assertThat((List<String>) JsonPath.read(enviar(get("/api/v1/emergencia/" + em + "/regulacoes?pacienteId=" + deslocado.getUuid()), null, 200), "$[*].id"))
                .containsExactly(dele);
        r = enviar(post("/api/v1/emergencia/" + em + "/regulacao/" + dele + "/prioridade"), null, 200);
        assertThat((List<String>) JsonPath.read(r, "$.priorizadas[*].id")).containsExactly(dele);
        enviar(post("/api/v1/emergencia/" + em + "/regulacao/" + dele + "/prioridade"), null, 409);

        // Na fila do regulador, a da emergência sobe dentro da mesma cor e vem marcada.
        String fila = enviar(get("/api/v1/solicitacao-regulacao/fila?procedimentoId=" + procedimento), null, 200);
        assertThat((List<String>) JsonPath.read(fila, "$[*].uuid")).containsExactly(dele, antes);
        assertThat((String) JsonPath.read(fila, "$[0].emergenciaNome")).isEqualTo("Cheia Rede");

        // Retirar e pôr de novo; encerrada a emergência, a marca deixa de valer.
        enviar(delete("/api/v1/emergencia/" + em + "/regulacao/" + antes + "/prioridade"), null, 422);
        enviar(delete("/api/v1/emergencia/" + em + "/regulacao/" + dele + "/prioridade"), null, 200);
        enviar(post("/api/v1/emergencia/" + em + "/regulacao/" + dele + "/prioridade"), null, 200);
        enviar(post("/api/v1/emergencia/" + em + "/desmobilizacao"), "{\"responsavel\": \"Secretário\", \"justificativa\": \"Águas baixando\"}", 200);
        String rel = enviar(get("/api/v1/emergencia/" + em + "/relatorio"), null, 200);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.regulacoesPriorizadas")).isEqualTo(1);
        enviar(post("/api/v1/emergencia/" + em + "/encerramento"), "{\"motivo\": \"Rede normalizada\"}", 200);
        fila = enviar(get("/api/v1/solicitacao-regulacao/fila?procedimentoId=" + procedimento), null, 200);
        assertThat((List<String>) JsonPath.read(fila, "$[*].uuid")).containsExactly(antes, dele);
        assertThat((List<Object>) JsonPath.read(fila, "$[*].emergenciaNome")).containsOnlyNulls();
        assertThat(UUID.fromString(dele)).isNotNull();
    }
}
