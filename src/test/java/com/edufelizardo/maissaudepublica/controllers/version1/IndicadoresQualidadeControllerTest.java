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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Indicadores de qualidade com metas e auditoria de protocolos, fatia Q3 (ADR-0205): os cálculos sobre dados conhecidos (pacientes-dia,
 * quedas, eventos graves, reinternação, adesão), o "sem base", as metas nos dois sentidos e o histórico, as regras da auditoria e do
 * checklist, a origem indicador na Q2 com a sugestão do fora da meta e o relatório no #19.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IndicadoresQualidadeControllerTest {

    static final String PREFIXO = "IndQual Teste ";
    private static final String API = "/api/v1/qualidade/";
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final YearMonth mes = YearMonth.now(ZONA).minusMonths(1);
    private UnidadeDeSaude hospital;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "Hospital");
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        u.setAtivo(true);
        hospital = unidadeDeSaudeRepository.save(u);
        Profissional p = new Profissional();
        p.setMatricula("IQT-MED");
        p.setCpf("90099900001");
        p.setNome(PREFIXO + "Médico");
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    @AfterEach
    void limpar() {
        limparDados(jdbc, PREFIXO, "IQT-%");
    }

    static void limparDados(JdbcTemplate jdbc, String prefixo, String matriculas) {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + prefixo + "%')";
        jdbc.update("delete from tb_meta_qualidade where unidade_id in " + unidades);
        jdbc.update("delete from tb_auditoria_protocolo where unidade_id in " + unidades);
        jdbc.update("delete from tb_versao_checklist_protocolo where checklist_id in (select uuid from tb_checklist_protocolo where codigo like 'TSTQ3%')");
        jdbc.update("delete from tb_checklist_protocolo where codigo like 'TSTQ3%'");
        jdbc.update("delete from tb_internacao where unidade_id in " + unidades);
        jdbc.update("delete from tb_leito where unidade_id in " + unidades);
        NaoConformidadeControllerTest.limparDados(jdbc, prefixo, matriculas);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        String texto = resposta.getContentAsString(StandardCharsets.UTF_8);
        assertThat(resposta.getStatus()).as(texto).isEqualTo(esperado);
        return texto;
    }

    private String ler(String url) throws Exception {
        var resposta = mockMvc.perform(get(url)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(200);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    private Timestamp dia(int d) {
        return Timestamp.from(mes.atDay(d).atTime(10, 0).atZone(ZONA).toInstant());
    }

    private UUID paciente(String nome, String cpf) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setAtivo(true);
        return pacienteRepository.save(p).getUuid();
    }

    private void internacao(UUID paciente, int admissao, int alta) {
        UUID setor = jdbc.queryForList("select uuid from tb_setor where unidade_id = ?", UUID.class, hospital.getUuid()).stream().findFirst().orElseGet(() -> {
            UUID s = UUID.randomUUID();
            jdbc.update("insert into tb_setor (uuid, unidade_id, nome, codigo, tipo, ativo) values (?, ?, ?, 'IQT-S', 'ASSISTENCIAL', true)", s, hospital.getUuid(),
                    PREFIXO + "Clínica");
            return s;
        });
        UUID leito = UUID.randomUUID();
        jdbc.update("insert into tb_leito (uuid, unidade_id, setor_id, identificacao, tipo, sexo, situacao, ativo, faixa_etaria) values (?, ?, ?, ?, 'CLINICO', 'MISTO', 'LIVRE', true, 'ADULTO')",
                leito, hospital.getUuid(), setor, PREFIXO + "L" + admissao + "-" + alta);
        UUID medico = profissionalRepository.findByMatricula("IQT-MED").orElseThrow().getUuid();
        jdbc.update("insert into tb_internacao (uuid, paciente_id, unidade_id, leito_id, medico_responsavel_id, cid, motivo, carater, admitida_em, alta_em, status) "
                + "values (gen_random_uuid(), ?, ?, ?, ?, 'R50', 'Febre', 'URGENCIA', ?, ?, 'ALTA')", paciente, hospital.getUuid(), leito, medico, dia(admissao), dia(alta));
    }

    private String incidente(String tipo, int d) throws Exception {
        return JsonPath.read(enviar(post(API + "incidente"), "{\"unidadeId\": \"" + hospital.getUuid() + "\", \"ocorridoEm\": \"" + dia(d).toInstant()
                + "\", \"local\": \"Leito\", \"tipo\": \"" + tipo + "\", \"descricao\": \"Ocorrência.\"}", 201), "$.uuid");
    }

    private String higiene() throws Exception {
        List<String> ids = JsonPath.read(ler(API + "checklists"), "$[?(@.codigo == 'HIGIENE_DAS_MAOS')].uuid");
        return ids.get(0);
    }

    private String versaoHigiene() throws Exception {
        List<String> ids = JsonPath.read(ler(API + "checklists"), "$[?(@.codigo == 'HIGIENE_DAS_MAOS')].versaoId");
        return ids.get(0);
    }

    private List<Object> linha(String painel, String indicador, String campo) {
        return JsonPath.read(painel, "$.linhas[?(@.indicador == '" + indicador + "')]." + campo);
    }

    @Test
    void indicadoresMetasESugestao() throws Exception {
        // 17 pacientes-dia (10 + 2 + 5); três altas, uma reinternada em até 30 dias.
        UUID ana = paciente("Ana", "90099900011");
        UUID bia = paciente("Bia", "90099900012");
        internacao(ana, 1, 11);
        internacao(bia, 5, 7);
        internacao(bia, 20, 25);
        // Duas quedas valem; a descartada não. Um evento adverso grave.
        incidente("QUEDA", 3);
        incidente("QUEDA", 4);
        String dup = incidente("QUEDA", 4);
        enviar(post(API + "incidente/" + dup + "/descarte"), "{\"motivo\": \"Duplicada.\"}", 200);
        String grave = incidente("MEDICACAO", 6);
        enviar(post(API + "incidente/" + grave + "/classificacao"), "{\"natureza\": \"EVENTO_ADVERSO\", \"dano\": \"GRAVE\"}", 200);
        // Adesão: 4 de 5 aplicáveis (um não se aplica).
        String versao = versaoHigiene();
        String auditoria = "{\"unidadeId\": \"" + hospital.getUuid() + "\", \"versaoId\": \"" + versao + "\", \"dataAuditoria\": \"" + mes.atDay(8) + "\", ";
        enviar(post(API + "auditoria-protocolo"), auditoria + "\"respostas\": {\"M1\": \"CONFORME\"}}", 400);
        enviar(post(API + "auditoria-protocolo"), auditoria + "\"respostas\": {\"M1\": \"CONFORME\", \"M2\": \"CONFORME\", \"M3\": \"CONFORME\", \"M4\": \"CONFORME\", "
                + "\"M5\": \"CONFORME\", \"M9\": \"CONFORME\"}}", 400);
        enviar(post(API + "auditoria-protocolo"), auditoria + "\"respostas\": {\"M1\": \"NAO_SE_APLICA\", \"M2\": \"NAO_SE_APLICA\", \"M3\": \"NAO_SE_APLICA\", "
                + "\"M4\": \"NAO_SE_APLICA\", \"M5\": \"NAO_SE_APLICA\"}}", 400);
        enviar(post(API + "auditoria-protocolo"), auditoria.replace(mes.atDay(8).toString(), LocalDate.now(ZONA).plusDays(1).toString())
                + "\"respostas\": {\"M1\": \"CONFORME\", \"M2\": \"CONFORME\", \"M3\": \"CONFORME\", \"M4\": \"CONFORME\", \"M5\": \"CONFORME\"}}", 400);
        String r = enviar(post(API + "auditoria-protocolo"), auditoria + "\"respostas\": {\"m1\": \"CONFORME\", \"M2\": \"CONFORME\", \"M3\": \"NAO_CONFORME\", "
                + "\"M4\": \"CONFORME\", \"M5\": \"CONFORME\", \"M3\": \"NAO_SE_APLICA\"}}".replace(", \"M3\": \"NAO_SE_APLICA\"", ""), 201);
        assertThat((Double) JsonPath.read(r, "$.adesao")).isEqualTo(80.0);

        // Metas: a queda e a adesão fora; a reinternação dentro.
        String meta = "{\"unidadeId\": \"" + hospital.getUuid() + "\", \"vigenteDesde\": \"" + mes.atDay(1) + "\", ";
        enviar(post(API + "meta"), meta + "\"indicador\": \"ADESAO_PROTOCOLO\", \"valor\": 90}", 400);
        enviar(post(API + "meta"), meta + "\"indicador\": \"TAXA_QUEDAS\", \"checklistId\": \"" + higiene() + "\", \"valor\": 3}", 400);
        enviar(post(API + "meta"), meta + "\"indicador\": \"TAXA_QUEDAS\", \"valor\": 3}", 201);
        enviar(post(API + "meta"), meta + "\"indicador\": \"TAXA_QUEDAS\", \"valor\": 2}", 422);
        enviar(post(API + "meta"), meta + "\"indicador\": \"ADESAO_PROTOCOLO\", \"checklistId\": \"" + higiene() + "\", \"valor\": 90}", 201);
        enviar(post(API + "meta"), meta + "\"indicador\": \"REINTERNACAO_30_DIAS\", \"valor\": 50}", 201);

        String painel = ler(API + "indicadores?unidadeId=" + hospital.getUuid() + "&competencia=" + mes);
        assertThat(linha(painel, "TAXA_QUEDAS", "valor")).containsExactly(117.65);
        assertThat(linha(painel, "TAXA_QUEDAS", "denominador")).containsExactly(17);
        assertThat(linha(painel, "TAXA_QUEDAS", "situacao")).containsExactly("FORA");
        assertThat(linha(painel, "INCIDENCIA_LESAO_PRESSAO", "valor")).containsExactly(0.0);
        assertThat(linha(painel, "INCIDENCIA_LESAO_PRESSAO", "situacao")).containsExactly("SEM_META");
        assertThat(linha(painel, "EVENTOS_ADVERSOS_GRAVES", "valor")).containsExactly(58.82);
        assertThat(linha(painel, "REINTERNACAO_30_DIAS", "valor")).containsExactly(33.3);
        assertThat(linha(painel, "REINTERNACAO_30_DIAS", "situacao")).containsExactly("DENTRO");
        assertThat(linha(painel, "ABSENTEISMO", "situacao")).containsExactly("SEM_BASE");
        assertThat(linha(painel, "TEMPO_ATE_MEDICO", "valor")).containsExactly((Object) null);
        assertThat((List<Object>) JsonPath.read(painel, "$.linhas[?(@.checklistId == '" + higiene() + "')].valor")).containsExactly(80.0);
        assertThat((List<Object>) JsonPath.read(painel, "$.linhas[?(@.checklistId == '" + higiene() + "')].situacao")).containsExactly("FORA");
        assertThat((List<Object>) JsonPath.read(painel, "$.linhas[?(@.indicador == 'TAXA_QUEDAS')].serie.length()")).containsExactly(12);
        assertThat((List<Object>) JsonPath.read(painel, "$.linhas[?(@.indicador == 'TAXA_QUEDAS')].serie[11].valor")).containsExactly(117.65);

        // A meta nova do mês seguinte encerra a anterior na véspera.
        enviar(post(API + "meta"), "{\"unidadeId\": \"" + hospital.getUuid() + "\", \"vigenteDesde\": \"" + mes.plusMonths(1).atDay(1)
                + "\", \"indicador\": \"TAXA_QUEDAS\", \"valor\": 2.5}", 201);
        String metas = ler(API + "metas?unidadeId=" + hospital.getUuid());
        assertThat((List<Object>) JsonPath.read(metas, "$[?(@.indicador == 'TAXA_QUEDAS' && @.valor == 3.0)].vigenteAte")).containsExactly(mes.atEndOfMonth().toString());

        // A sugestão do fora da meta no mês fechado e a origem indicador na Q2.
        String s = ler(API + "nao-conformidades/sugestoes");
        assertThat((List<String>) JsonPath.read(s, "$[?(@.origem == 'INDICADOR' && @.unidadeId == '" + hospital.getUuid() + "')].indicador"))
                .contains("TAXA_QUEDAS", "ADESAO_PROTOCOLO");
        String nc = "{\"unidadeId\": \"" + hospital.getUuid() + "\", \"origem\": \"INDICADOR\", \"descricao\": \"" + PREFIXO + "Quedas acima da meta.\", "
                + "\"gravidade\": \"MAIOR\", \"responsavelMatricula\": \"IQT-MED\"";
        enviar(post(API + "nao-conformidade"), nc + ", \"indicador\": \"TAXA_QUEDAS\"}", 400);
        enviar(post(API + "nao-conformidade"), nc + ", \"indicador\": \"ADESAO_PROTOCOLO\", \"competencia\": \"" + mes.atDay(1) + "\"}", 400);
        enviar(post(API + "nao-conformidade"), nc.replace("INDICADOR", "OUTRA") + ", \"indicador\": \"TAXA_QUEDAS\", \"competencia\": \"" + mes.atDay(1) + "\"}", 400);
        r = enviar(post(API + "nao-conformidade"), nc + ", \"indicador\": \"TAXA_QUEDAS\", \"competencia\": \"" + mes.atDay(15) + "\"}", 201);
        assertThat((String) JsonPath.read(r, "$.competencia")).isEqualTo(mes.atDay(1).toString());
        enviar(post(API + "nao-conformidade"), nc + ", \"indicador\": \"TAXA_QUEDAS\", \"competencia\": \"" + mes.atDay(1) + "\"}", 409);
        s = ler(API + "nao-conformidades/sugestoes");
        assertThat((List<String>) JsonPath.read(s, "$[?(@.origem == 'INDICADOR' && @.unidadeId == '" + hospital.getUuid() + "')].indicador"))
                .containsExactly("ADESAO_PROTOCOLO");

        // O relatório no #19.
        String rel = ler("/api/v1/relatorios/QUALIDADE?unidadeId=" + hospital.getUuid() + "&de=" + mes.atDay(1) + "&ate=" + mes.atEndOfMonth());
        assertThat((String) JsonPath.read(rel, "$.categoria")).isEqualTo("Qualidade");
        assertThat((List<String>) JsonPath.read(rel, "$.secoes[?(@.titulo == 'Indicadores do período')].linhas[?(@[0] == 'Taxa de quedas')][4]"))
                .containsExactly("FORA");
    }

    @Test
    void checklistsVersionados() throws Exception {
        enviar(post(API + "checklist"), "{\"itens\": [{\"codigo\": \"A\", \"texto\": \"x\"}]}", 400);
        enviar(post(API + "checklist"), "{\"codigo\": \"TSTQ3_X\", \"nome\": \"x\", \"itens\": [{\"codigo\": \"A\", \"texto\": \"x\"}, {\"codigo\": \"a\", \"texto\": \"y\"}]}", 400);
        String r = enviar(post(API + "checklist"), "{\"codigo\": \"tstq3_pulseira\", \"nome\": \"Identificação: pulseira\", \"itens\": [{\"codigo\": \"P1\", "
                + "\"texto\": \"Pulseira com dois identificadores\"}]}", 201);
        assertThat((String) JsonPath.read(r, "$.codigo")).isEqualTo("TSTQ3_PULSEIRA");
        enviar(post(API + "checklist"), "{\"codigo\": \"TSTQ3_PULSEIRA\", \"nome\": \"x\", \"itens\": [{\"codigo\": \"A\", \"texto\": \"x\"}]}", 409);
        r = enviar(post(API + "checklist/" + JsonPath.read(r, "$.uuid") + "/versao"), "{\"itens\": [{\"codigo\": \"P1\", \"texto\": \"Pulseira com dois identificadores\"}, "
                + "{\"codigo\": \"P2\", \"texto\": \"Conferência antes do medicamento\"}]}", 201);
        assertThat((Integer) JsonPath.read(r, "$.versao")).isEqualTo(2);
        // O painel ganha a linha de adesão do checklist novo, sem base.
        String painel = ler(API + "indicadores?unidadeId=" + hospital.getUuid());
        assertThat((List<Object>) JsonPath.read(painel, "$.linhas[?(@.checklistId == '" + JsonPath.read(r, "$.uuid") + "')].situacao")).containsExactly("SEM_BASE");
    }
}
