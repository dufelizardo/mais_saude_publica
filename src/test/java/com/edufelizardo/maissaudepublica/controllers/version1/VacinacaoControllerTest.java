package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Imunização, fatia I1 (ADR-0147): o catálogo e o calendário da carga inicial, o registro da dose com as regras (lote,
 * quem aplicou, futuro, repetição, fora do calendário com justificativa), a transcrição da caderneta, a situação vacinal
 * calculada, a retificação e a anulação, a sala de vacina do dia e os atrasados adscritos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VacinacaoControllerTest {

    private static final String PREFIXO = "Vacina Teste ";
    private static final String MATRICULA = "P-IMU-TEC";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude ubs;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        Profissional tec = new Profissional();
        tec.setMatricula(MATRICULA);
        tec.setCpf("90088800001");
        tec.setNome(PREFIXO + "Técnica");
        tec.setConselhoClasse("COREN");
        tec.setAtivo(true);
        profissionalRepository.save(tec);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String pacientesSql = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        jdbc.update("update tb_registro_vacinacao set retificacao_de_id = null where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_registro_vacinacao where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_adscricao where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_equipe where unidade_id in " + unidades);
        jdbc.update("delete from tb_esquema_vacinal where observacao = 'IMU-TESTE'");
        jdbc.update("delete from tb_imunobiologico where codigo = 'IMU_TESTE'");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = '" + MATRICULA + "'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private Paciente paciente(LocalDate nascimento) {
        pacientes++;
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Criança " + pacientes);
        p.setCpf(String.format("900888%05d", pacientes));
        p.setSexo(Sexo.FEMININO);
        p.setDataNascimento(nascimento);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private String imuno(String codigo) {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_imunobiologico where codigo = ?", String.class, codigo);
    }

    private String corpo(Paciente p, String codigo, String dose, String extra) {
        return ("{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"APLICADA\", "
                + "\"profissionalMatricula\": \"%s\", \"lote\": \"L123\"%s}").formatted(p.getUuid(), imuno(codigo), dose, ubs.getUuid(), MATRICULA, extra);
    }

    private String transcrita(Paciente p, String codigo, String dose, LocalDate data) {
        return ("{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"TRANSCRITA\", "
                + "\"aplicadaEm\": \"%s\", \"localOrigem\": \"Maternidade Municipal\"}").formatted(p.getUuid(), imuno(codigo), dose, ubs.getUuid(),
                data.atTime(LocalTime.of(9, 0)));
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static String situacao(String caderneta, String sigla, String dose) {
        List<String> s = JsonPath.read(caderneta, "$.calendario[?(@.sigla == '" + sigla + "' && @.dose == '" + dose + "')].situacao");
        return s.isEmpty() ? null : s.get(0);
    }

    @Test
    void catalogoECalendarioDaCargaInicial() throws Exception {
        String imunos = ler("/api/v1/imunobiologicos");
        assertThat((List<String>) JsonPath.read(imunos, "$[*].codigo")).contains("BCG", "PENTA", "VIP", "HPV4", "DT").hasSizeGreaterThanOrEqualTo(20);
        String cal = ler("/api/v1/calendario-vacinal");
        assertThat((List<Integer>) JsonPath.read(cal, "$[?(@.sigla == 'Penta' && @.dose == 'TERCEIRA')].idadeRecomendadaMeses")).containsExactly(6);
        assertThat((List<Boolean>) JsonPath.read(cal, "$[?(@.sigla == 'dT')].contaNaSituacao")).containsExactly(true);

        // Gerência: novo imunobiológico, código repetido 409, dose do calendário com idades fora de ordem 400.
        String novo = "{\"codigo\": \"imu teste\", \"nome\": \"Vacina teste\", \"sigla\": \"VT\", \"tipo\": \"VACINA\", \"via\": \"ORAL\", \"dosesPorFrasco\": 1}";
        String criado = enviar(post("/api/v1/imunobiologico"), novo, 201);
        assertThat((String) JsonPath.read(criado, "$.codigo")).isEqualTo("IMU_TESTE");
        enviar(post("/api/v1/imunobiologico"), novo, 409);
        String esquema = "{\"imunobiologicoId\": \"%s\", \"dose\": \"UNICA\", \"publico\": \"CRIANCA\", \"idadeMinimaMeses\": 6, \"idadeRecomendadaMeses\": 3, "
                + "\"vigenteDesde\": \"2025-01-01\", \"observacao\": \"IMU-TESTE\"}";
        enviar(post("/api/v1/esquema-vacinal"), esquema.formatted(JsonPath.read(criado, "$.uuid").toString()), 400);
        String ok = enviar(post("/api/v1/esquema-vacinal"), esquema.replace("\"idadeRecomendadaMeses\": 3", "\"idadeRecomendadaMeses\": 6")
                .formatted(JsonPath.read(criado, "$.uuid").toString()), 201);
        enviar(put("/api/v1/imunobiologico/" + JsonPath.read(criado, "$.uuid")), novo.replace("\"dosesPorFrasco\": 1", "\"dosesPorFrasco\": 1, \"ativo\": false"), 200);
        assertThat((Boolean) JsonPath.read(ok, "$.contaNaSituacao")).isTrue();
    }

    @Test
    void registroComAsRegrasDoCalendario() throws Exception {
        Paciente bebe = paciente(hoje.minusMonths(2).minusDays(3));
        String r = enviar(post("/api/v1/vacinacao"), corpo(bebe, "PENTA", "PRIMEIRA", ""), 201);
        assertThat((String) JsonPath.read(r, "$.via")).isEqualTo("INTRAMUSCULAR");
        assertThat((String) JsonPath.read(r, "$.estrategia")).isEqualTo("ROTINA");
        // Repetida, sem lote, sem quem aplicou, no futuro.
        enviar(post("/api/v1/vacinacao"), corpo(bebe, "PENTA", "PRIMEIRA", ""), 409);
        enviar(post("/api/v1/vacinacao"), corpo(bebe, "VIP", "PRIMEIRA", "").replace("\"lote\": \"L123\"", "\"lote\": \"\""), 400);
        enviar(post("/api/v1/vacinacao"), corpo(bebe, "VIP", "PRIMEIRA", "").replace(MATRICULA, ""), 400);
        enviar(post("/api/v1/vacinacao"), corpo(bebe, "VIP", "PRIMEIRA", ", \"aplicadaEm\": \"" + hoje.plusDays(2).atTime(9, 0) + "\""), 400);
        // Tríplice viral aos 2 meses: fora da idade mínima (12), só com justificativa.
        String fora = enviar(post("/api/v1/vacinacao"), corpo(bebe, "TRIPLICE_VIRAL", "PRIMEIRA", ""), 422);
        assertThat(fora).contains("abaixo da mínima de 12");
        String justificada = enviar(post("/api/v1/vacinacao"), corpo(bebe, "TRIPLICE_VIRAL", "PRIMEIRA", ", \"justificativa\": \"Bloqueio de surto de sarampo\", "
                + "\"estrategia\": \"BLOQUEIO\""), 201);
        assertThat((String) JsonPath.read(justificada, "$.justificativa")).isEqualTo("Bloqueio de surto de sarampo");
        // A transcrita pede a data e não exige lote.
        enviar(post("/api/v1/vacinacao"), transcrita(bebe, "BCG", "UNICA", hoje.minusMonths(2)).replace(", \"aplicadaEm\": \"" + hoje.minusMonths(2).atTime(9, 0) + "\"", ""), 400);
        String bcg = enviar(post("/api/v1/vacinacao"), transcrita(bebe, "BCG", "UNICA", hoje.minusMonths(2)), 201);
        assertThat((String) JsonPath.read(bcg, "$.origem")).isEqualTo("TRANSCRITA");
        assertThat((Object) JsonPath.read(bcg, "$.lote")).isNull();

        // A mesma vacina e dose em mais de um público: vale o esquema da idade. No adulto, a hepatite B e a influenza não são
        // "fora do calendário"; a BCG (só da criança) é.
        Paciente adulto = paciente(hoje.minusYears(36));
        enviar(post("/api/v1/vacinacao"), corpo(adulto, "HEPATITE_B", "PRIMEIRA", ""), 201);
        enviar(post("/api/v1/vacinacao"), corpo(adulto, "INFLUENZA", "ANUAL", ""), 201);
        enviar(post("/api/v1/vacinacao"), corpo(adulto, "INFLUENZA", "ANUAL", ""), 409);
        assertThat(enviar(post("/api/v1/vacinacao"), corpo(adulto, "BCG", "UNICA", ""), 422)).contains("acima da máxima de 59");
    }

    @Test
    void cadernetaComASituacaoPeloCalendario() throws Exception {
        LocalDate nascimento = hoje.minusMonths(7);
        Paciente bebe = paciente(nascimento);
        enviar(post("/api/v1/vacinacao"), transcrita(bebe, "BCG", "UNICA", nascimento), 201);
        enviar(post("/api/v1/vacinacao"), transcrita(bebe, "HEPATITE_B", "PRIMEIRA", nascimento), 201);
        enviar(post("/api/v1/vacinacao"), transcrita(bebe, "PENTA", "PRIMEIRA", nascimento.plusMonths(2)), 201);
        String c = ler("/api/v1/paciente/" + bebe.getUuid() + "/vacinacao");
        assertThat((Boolean) JsonPath.read(c, "$.situacaoDisponivel")).isTrue();
        assertThat((Integer) JsonPath.read(c, "$.idadeMeses")).isEqualTo(7);
        assertThat(situacao(c, "BCG", "UNICA")).isEqualTo("APLICADA");
        assertThat(situacao(c, "Penta", "PRIMEIRA")).isEqualTo("APLICADA");
        assertThat(situacao(c, "Penta", "SEGUNDA")).isEqualTo("ATRASADA");
        // O rotavírus passou da idade máxima; a febre amarela é aos 9 meses.
        assertThat(situacao(c, "VRH", "PRIMEIRA")).isEqualTo("FORA_DA_IDADE");
        assertThat(situacao(c, "FA", "PRIMEIRA")).isEqualTo("FUTURA");
        assertThat((Boolean) JsonPath.read(c, "$.emDia")).isFalse();
        assertThat((Integer) JsonPath.read(c, "$.atrasadas")).isGreaterThan(0);
        assertThat((List<?>) JsonPath.read(c, "$.doses")).hasSize(3);
        // A influenza anual não entra nos atrasos: é recomendação.
        assertThat((List<String>) JsonPath.read(c, "$.recomendacoes[*].sigla")).contains("Influenza");

        // Sem a data de nascimento, sem situação.
        Paciente semData = paciente(null);
        String s = ler("/api/v1/paciente/" + semData.getUuid() + "/vacinacao");
        assertThat((Boolean) JsonPath.read(s, "$.situacaoDisponivel")).isFalse();
    }

    @Test
    void retificacaoEAnulacao() throws Exception {
        Paciente bebe = paciente(hoje.minusMonths(3));
        String r = enviar(post("/api/v1/vacinacao"), corpo(bebe, "VIP", "PRIMEIRA", ""), 201);
        String id = JsonPath.read(r, "$.uuid");
        enviar(post("/api/v1/vacinacao/" + id + "/retificacao"), corpo(bebe, "VIP", "PRIMEIRA", ""), 400);
        String nova = enviar(post("/api/v1/vacinacao/" + id + "/retificacao"),
                corpo(bebe, "VIP", "PRIMEIRA", ", \"motivoRetificacao\": \"Lote digitado errado\"").replace("L123", "L999"), 201);
        assertThat((String) JsonPath.read(nova, "$.retificacaoDeId")).isEqualTo(id);
        enviar(post("/api/v1/vacinacao/" + id + "/retificacao"), corpo(bebe, "VIP", "PRIMEIRA", ", \"motivoRetificacao\": \"De novo\""), 422);
        String c = ler("/api/v1/paciente/" + bebe.getUuid() + "/vacinacao");
        assertThat((List<String>) JsonPath.read(c, "$.doses[*].lote")).containsExactly("L999");

        String novaId = JsonPath.read(nova, "$.uuid");
        enviar(post("/api/v1/vacinacao/" + novaId + "/anulacao"), "{\"motivo\": \"Paciente errado\"}", 200);
        enviar(post("/api/v1/vacinacao/" + novaId + "/anulacao"), "{\"motivo\": \"De novo\"}", 422);
        assertThat((List<?>) JsonPath.read(ler("/api/v1/paciente/" + bebe.getUuid() + "/vacinacao"), "$.doses")).isEmpty();
        // Anulada, a mesma dose pode ser registrada de novo.
        enviar(post("/api/v1/vacinacao"), corpo(bebe, "VIP", "PRIMEIRA", ""), 201);
    }

    @Test
    void salaDeVacinaDoDiaEAtrasadosAdscritos() throws Exception {
        Paciente emDia = paciente(hoje.minusDays(10));
        Paciente atrasado = paciente(hoje.minusMonths(5));
        enviar(post("/api/v1/vacinacao"), corpo(emDia, "BCG", "UNICA", ""), 201);
        enviar(post("/api/v1/vacinacao"), corpo(emDia, "HEPATITE_B", "PRIMEIRA", ""), 201);
        String dia = ler("/api/v1/vacinacao/dia?unidadeId=" + ubs.getUuid());
        assertThat((Integer) JsonPath.read(dia, "$.total")).isEqualTo(2);
        assertThat((List<String>) JsonPath.read(dia, "$.porImunobiologico[*].chave")).containsExactlyInAnyOrder("BCG", "HepB");

        String equipe = jdbc.queryForObject("insert into tb_equipe (uuid, unidade_id, nome, tipo, ativa) values (gen_random_uuid(), ?, ?, 'ESF', true) "
                + "returning cast(uuid as varchar)", String.class, ubs.getUuid(), PREFIXO + "ESF");
        for (Paciente p : List.of(emDia, atrasado)) {
            jdbc.update("insert into tb_adscricao (uuid, paciente_id, equipe_id, origem, inicio) values (gen_random_uuid(), ?, cast(? as uuid), 'MANUAL', ?)",
                    p.getUuid(), equipe, hoje.minusDays(5));
        }
        String lista = ler("/api/v1/vacinacao/atrasados?unidadeId=" + ubs.getUuid());
        List<Map<String, Object>> linhas = JsonPath.read(lista, "$[?(@.pacienteId == '" + atrasado.getUuid() + "')]");
        assertThat(linhas).hasSize(1);
        assertThat((List<String>) linhas.get(0).get("atrasadas")).contains("BCG, dose única", "Penta, 1ª dose");
        assertThat(linhas.get(0)).containsEntry("equipeNome", PREFIXO + "ESF");
        // O recém-nascido com BCG e hepatite B está em dia.
        assertThat((List<?>) JsonPath.read(lista, "$[?(@.pacienteId == '" + emDia.getUuid() + "')]")).isEmpty();
    }
}
