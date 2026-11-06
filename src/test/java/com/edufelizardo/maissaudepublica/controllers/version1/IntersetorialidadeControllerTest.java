package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Intersetorialidade IS1 (ADR-0154) e Saúde Mental SM6 (ADR-0155): o catálogo de instituições externas, o ciclo do
 * encaminhamento intersetorial no caso (envio, recebimento, retorno, cancelamento, sem retorno no prazo) e os cinco
 * indicadores do programa Saúde Mental.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntersetorialidadeControllerTest {

    private static final String PREFIXO = "Interset Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private SetorRepository setorRepository;
    @Autowired private LeitoRepository leitoRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude caps;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        caps = new UnidadeDeSaude();
        caps.setNome(PREFIXO + "CAPS");
        caps.setTipo(TipoUnidadeDeSaude.CAPS);
        caps.setAtivo(true);
        caps = unidadeDeSaudeRepository.save(caps);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String casos = "(select uuid from tb_acompanhamento_saude_mental where unidade_id in " + unidades + ")";
        String crises = "(select uuid from tb_evento_crise where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_encaminhamento_intersetorial where unidade_id in " + unidades);
        jdbc.update("delete from tb_instituicao_externa where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_etapa_crise where crise_id in " + crises);
        jdbc.update("delete from tb_evento_crise where unidade_id in " + unidades);
        jdbc.update("delete from tb_comunicacao_ministerio_publico where internacao_id in (select uuid from tb_internacao where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_internacao where unidade_id in " + unidades);
        jdbc.update("delete from tb_leito where unidade_id in " + unidades);
        jdbc.update("delete from tb_setor where unidade_id in " + unidades);
        jdbc.update("delete from tb_profissional where matricula = 'IST-MED'");
        jdbc.update("delete from tb_projeto_terapeutico_singular where acompanhamento_id in " + casos);
        jdbc.update("delete from tb_avaliacao_saude_mental where acompanhamento_id in " + casos);
        jdbc.update("delete from tb_acompanhamento_saude_mental where unidade_id in " + unidades);
        jdbc.update("delete from tb_acompanhamento_programatico where unidade_id in " + unidades);
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private Paciente paciente() {
        pacientes++;
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente " + pacientes);
        p.setCpf(String.format("900333%05d", pacientes));
        p.setSexo(Sexo.FEMININO);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    /** Um caso de saúde mental vigente na unidade, aberto há {@code dias} dias. */
    private String caso(int dias) {
        Paciente p = paciente();
        String programa = jdbc.queryForObject("insert into tb_acompanhamento_programatico (uuid, paciente_id, programa_id, unidade_id, inicio, situacao) "
                + "select gen_random_uuid(), ?, uuid, ?, ?, 'ATIVO' from tb_programa where codigo = 'SAUDE_MENTAL' returning cast(uuid as varchar)", String.class,
                p.getUuid(), caps.getUuid(), hoje.minusDays(dias));
        return jdbc.queryForObject("insert into tb_acompanhamento_saude_mental (uuid, paciente_id, unidade_id, acompanhamento_programatico_id, inicio) "
                + "values (gen_random_uuid(), ?, ?, cast(? as uuid), ?) returning cast(uuid as varchar)", String.class, p.getUuid(), caps.getUuid(), programa,
                hoje.minusDays(dias));
    }

    private String pacienteDoCaso(String caso) {
        return jdbc.queryForObject("select cast(paciente_id as varchar) from tb_acompanhamento_saude_mental where uuid = cast(? as uuid)", String.class, caso);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String instituicao(String nome, String setor, String tipo) throws Exception {
        String r = enviar(post("/api/v1/instituicao-externa"), "{\"nome\": \"" + PREFIXO + nome + "\", \"setor\": \"" + setor + "\", \"tipo\": \"" + tipo
                + "\", \"telefone\": \"(11) 3333-0000\"}", 201);
        return JsonPath.read(r, "$.uuid");
    }

    @Test
    void catalogoDeInstituicoes() throws Exception {
        String cras = instituicao("CRAS Centro", "ASSISTENCIA_SOCIAL", "CRAS");
        instituicao("EMEF Jardim", "EDUCACAO", "ESCOLA");
        instituicao("ONG Abrigo", "OUTRO", "OUTRO");
        // Nome repetido (sem diferença de caixa) e tipo de outro setor.
        enviar(post("/api/v1/instituicao-externa"), "{\"nome\": \"" + PREFIXO.toLowerCase() + "cras centro\", \"setor\": \"ASSISTENCIA_SOCIAL\", "
                + "\"tipo\": \"CRAS\"}", 409);
        enviar(post("/api/v1/instituicao-externa"), "{\"nome\": \"" + PREFIXO + "Escola X\", \"setor\": \"ASSISTENCIA_SOCIAL\", \"tipo\": \"ESCOLA\"}", 422);
        enviar(post("/api/v1/instituicao-externa"), "{\"nome\": \"\", \"setor\": \"EDUCACAO\", \"tipo\": \"ESCOLA\"}", 400);

        List<String> social = JsonPath.read(ler("/api/v1/instituicoes-externas?setor=ASSISTENCIA_SOCIAL"), "$[?(@.nome =~ /" + PREFIXO + ".*/)].nome");
        assertThat(social).containsExactly(PREFIXO + "CRAS Centro");

        // Inativar mantém o registro e sai da lista das ativas.
        String r = enviar(put("/api/v1/instituicao-externa/" + cras), "{\"nome\": \"" + PREFIXO + "CRAS Centro\", \"setor\": \"ASSISTENCIA_SOCIAL\", "
                + "\"tipo\": \"CRAS\", \"responsavel\": \"Coordenação\", \"ativa\": false}", 200);
        assertThat((Boolean) JsonPath.read(r, "$.ativa")).isFalse();
        assertThat((String) JsonPath.read(r, "$.responsavel")).isEqualTo("Coordenação");
        List<String> ativas = JsonPath.read(ler("/api/v1/instituicoes-externas?ativas=true"), "$[?(@.nome =~ /" + PREFIXO + ".*/)].nome");
        assertThat(ativas).containsExactly(PREFIXO + "EMEF Jardim", PREFIXO + "ONG Abrigo");
    }

    @Test
    void cicloDoEncaminhamentoNoCaso() throws Exception {
        String caso = caso(20);
        String cras = instituicao("CRAS Norte", "ASSISTENCIA_SOCIAL", "CRAS");
        String base = "/api/v1/saude-mental/acompanhamento/" + caso + "/encaminhamento-intersetorial";
        enviar(post(base), "{\"instituicaoId\": \"" + cras + "\", \"motivo\": \"Benefício eventual\", \"retornoEsperadoEm\": \"" + hoje.minusDays(1) + "\"}", 400);
        enviar(post(base), "{\"instituicaoId\": \"" + cras + "\", \"motivo\": \"Benefício\", \"enviadoEm\": \"" + hoje.plusDays(1) + "\", "
                + "\"retornoEsperadoEm\": \"" + hoje.plusDays(9) + "\"}", 400);
        String r = enviar(post(base), "{\"instituicaoId\": \"" + cras + "\", \"motivo\": \"Inclusão no CadÚnico e benefício eventual\", "
                + "\"retornoEsperadoEm\": \"" + hoje.plusDays(15) + "\"}", 201);
        assertThat((String) JsonPath.read(r, "$.encaminhamentosIntersetoriais[0].situacao")).isEqualTo("ENVIADO");
        assertThat((String) JsonPath.read(r, "$.encaminhamentosIntersetoriais[0].instituicaoNome")).isEqualTo(PREFIXO + "CRAS Norte");
        assertThat((Boolean) JsonPath.read(r, "$.retornoIntersetorialVencido")).isFalse();
        String id = JsonPath.read(r, "$.encaminhamentosIntersetoriais[0].uuid");
        // Um em aberto por instituição.
        enviar(post(base), "{\"instituicaoId\": \"" + cras + "\", \"motivo\": \"De novo\", \"retornoEsperadoEm\": \"" + hoje.plusDays(15) + "\"}", 409);

        r = enviar(post(base + "/" + id + "/recebimento"), "{}", 200);
        assertThat((String) JsonPath.read(r, "$.encaminhamentosIntersetoriais[0].situacao")).isEqualTo("RECEBIDO");
        enviar(post(base + "/" + id + "/recebimento"), "{}", 422);
        enviar(post(base + "/" + id + "/retorno"), "{\"desfecho\": \"CONCLUIDO\"}", 400);
        enviar(post(base + "/" + id + "/retorno"), "{\"desfecho\": \"CANCELADO\", \"retorno\": \"x\"}", 400);
        r = enviar(post(base + "/" + id + "/retorno"), "{\"desfecho\": \"CONCLUIDO\", \"retorno\": \"Família inscrita; benefício concedido.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.encaminhamentosIntersetoriais[0].situacao")).isEqualTo("CONCLUIDO");
        assertThat((String) JsonPath.read(r, "$.encaminhamentosIntersetoriais[0].retorno")).isEqualTo("Família inscrita; benefício concedido.");
        enviar(post(base + "/" + id + "/cancelamento"), "{\"motivo\": \"Engano\"}", 422);

        // Instituição inativa não recebe encaminhamento novo; o encaminhamento de outro caso não é deste.
        enviar(put("/api/v1/instituicao-externa/" + cras), "{\"nome\": \"" + PREFIXO + "CRAS Norte\", \"setor\": \"ASSISTENCIA_SOCIAL\", \"tipo\": \"CRAS\", "
                + "\"ativa\": false}", 200);
        enviar(post(base), "{\"instituicaoId\": \"" + cras + "\", \"motivo\": \"Outro\", \"retornoEsperadoEm\": \"" + hoje.plusDays(5) + "\"}", 422);
        enviar(post("/api/v1/saude-mental/acompanhamento/" + caso(5) + "/encaminhamento-intersetorial/" + id + "/recebimento"), "{}", 404);
    }

    @Test
    void semRetornoNoPrazoViraPendenciaDoCaso() throws Exception {
        String caso = caso(40);
        String ct = instituicao("Conselho Tutelar Sul", "GARANTIA_DE_DIREITOS", "CONSELHO_TUTELAR");
        String base = "/api/v1/saude-mental/acompanhamento/" + caso + "/encaminhamento-intersetorial";
        String r = enviar(post(base), "{\"instituicaoId\": \"" + ct + "\", \"motivo\": \"Situação de violência\", \"enviadoEm\": \"" + hoje.minusDays(20)
                + "\", \"retornoEsperadoEm\": \"" + hoje.minusDays(5) + "\"}", 201);
        assertThat((Boolean) JsonPath.read(r, "$.retornoIntersetorialVencido")).isTrue();
        assertThat((Boolean) JsonPath.read(r, "$.encaminhamentosIntersetoriais[0].semRetorno")).isTrue();
        String lista = ler("/api/v1/saude-mental/acompanhamentos?unidadeId=" + caps.getUuid() + "&pendencia=RETORNO_INTERSETORIAL");
        assertThat((List<String>) JsonPath.read(lista, "$.lista[*].uuid")).containsExactly(caso);
        assertThat((Integer) JsonPath.read(lista, "$.retornoIntersetorialVencido")).isEqualTo(1);

        // Cancelado não é mais pendência.
        String id = JsonPath.read(r, "$.encaminhamentosIntersetoriais[0].uuid");
        r = enviar(post(base + "/" + id + "/cancelamento"), "{\"motivo\": \"Registrado no caso errado\"}", 200);
        assertThat((Boolean) JsonPath.read(r, "$.retornoIntersetorialVencido")).isFalse();
        assertThat((String) JsonPath.read(r, "$.encaminhamentosIntersetoriais[0].motivoCancelamento")).isEqualTo("Registrado no caso errado");
    }

    private Map<String, Object> indicador(String json, String codigo) {
        List<Map<String, Object>> l = JsonPath.read(json, "$[?(@.indicador == '" + codigo + "')]");
        assertThat(l).hasSize(1);
        return l.get(0);
    }

    @Test
    void indicadoresDoProgramaSaudeMental() throws Exception {
        // PTS em dia: um caso com PTS dentro da revisão e um sem PTS.
        String c1 = caso(40);
        String c2 = caso(35);
        caso(5);
        jdbc.update("insert into tb_projeto_terapeutico_singular (uuid, acompanhamento_id, versao, necessidades, periodicidade_revisao, revisar_em, "
                + "pactuado_com_pessoa, elaborado_em) values (gen_random_uuid(), cast(? as uuid), 1, 'Vínculo', 'MENSAL', ?, true, now() - interval '30 days')",
                c1, hoje.plusDays(10));
        // Avaliação em 30 dias: o caso 1 avaliado no 5º dia; o 2 sem avaliação passou do prazo; o 3 (5 dias) ainda não conta.
        jdbc.update("insert into tb_avaliacao_saude_mental (uuid, acompanhamento_id, realizada_em, contexto_historia, necessidades) values "
                + "(gen_random_uuid(), cast(? as uuid), now() - interval '35 days', 'História', 'Necessidades')", c1);
        // Pós-crise em 7 dias: uma crise com pós-crise no 3º dia; a de 2 dias sem pós-crise não conta.
        String crise = jdbc.queryForObject("insert into tb_evento_crise (uuid, paciente_id, unidade_id, identificada_em, tipo, descricao, situacao, "
                + "notificacao_sugerida) values (gen_random_uuid(), cast(? as uuid), ?, now() - interval '10 days', 'AGITACAO_PSICOMOTORA', 'Agitação', 'ENCERRADA', false) "
                + "returning cast(uuid as varchar)", String.class, pacienteDoCaso(c1), caps.getUuid());
        jdbc.update("insert into tb_etapa_crise (uuid, crise_id, etapa, descricao, ocorrida_em) values (gen_random_uuid(), cast(? as uuid), 'POS_CRISE', "
                + "'Visita', now() - interval '7 days')", crise);
        jdbc.update("insert into tb_evento_crise (uuid, paciente_id, unidade_id, identificada_em, tipo, descricao, situacao, notificacao_sugerida) values "
                + "(gen_random_uuid(), cast(? as uuid), ?, now() - interval '2 days', 'AGITACAO_PSICOMOTORA', 'Agitação', 'EM_CURSO', false)", pacienteDoCaso(c2), caps.getUuid());
        // Comunicação ao MP: uma involuntária comunicada em 1 dia, outra sem comunicação há 10 dias; a voluntária não entra.
        String i1 = internacao("INVOLUNTARIA", 5, "PQ 01");
        internacao("INVOLUNTARIA", 10, "PQ 02");
        internacao("VOLUNTARIA", 10, "PQ 03");
        jdbc.update("insert into tb_comunicacao_ministerio_publico (uuid, internacao_id, momento, comunicada_em) values (gen_random_uuid(), cast(? as uuid), "
                + "'ADMISSAO', now() - interval '4 days')", i1);
        // Retorno intersetorial: um com retorno, um em aberto, um cancelado (fora).
        String cras = instituicao("CRAS Leste", "ASSISTENCIA_SOCIAL", "CRAS");
        String escola = instituicao("Escola Leste", "EDUCACAO", "ESCOLA");
        String ct = instituicao("CT Leste", "GARANTIA_DE_DIREITOS", "CONSELHO_TUTELAR");
        String base = "/api/v1/saude-mental/acompanhamento/" + c1 + "/encaminhamento-intersetorial";
        String corpo = "\", \"motivo\": \"M\", \"enviadoEm\": \"" + hoje.minusDays(3) + "\", \"retornoEsperadoEm\": \"" + hoje.plusDays(10) + "\"}";
        String r = enviar(post(base), "{\"instituicaoId\": \"" + cras + corpo, 201);
        enviar(post(base + "/" + JsonPath.read(r, "$.encaminhamentosIntersetoriais[0].uuid") + "/retorno"), "{\"desfecho\": \"RECUSADO\", \"retorno\": "
                + "\"Fora da área de abrangência.\"}", 200);
        enviar(post(base), "{\"instituicaoId\": \"" + escola + corpo, 201);
        r = enviar(post(base), "{\"instituicaoId\": \"" + ct + corpo, 201);
        enviar(post(base + "/" + JsonPath.read(r, "$.encaminhamentosIntersetoriais[0].uuid") + "/cancelamento"), "{\"motivo\": \"Engano\"}", 200);

        String programa = jdbc.queryForObject("select cast(uuid as varchar) from tb_programa where codigo = 'SAUDE_MENTAL'", String.class);
        String json = ler("/api/v1/programa/" + programa + "/indicadores?unidadeId=" + caps.getUuid() + "&de=" + hoje.minusDays(60) + "&ate=" + hoje);
        assertThat(indicador(json, "PTS_EM_DIA")).containsEntry("numerador", 1).containsEntry("denominador", 3);
        assertThat(indicador(json, "AVALIACAO_EM_30_DIAS")).containsEntry("numerador", 1).containsEntry("denominador", 2);
        assertThat(indicador(json, "POS_CRISE_EM_7_DIAS")).containsEntry("numerador", 1).containsEntry("denominador", 1);
        assertThat(indicador(json, "COMUNICACAO_MP_NO_PRAZO")).containsEntry("numerador", 1).containsEntry("denominador", 2);
        assertThat(indicador(json, "RETORNO_INTERSETORIAL")).containsEntry("numerador", 1).containsEntry("denominador", 2);
        assertThat(indicador(json, "RETORNO_INTERSETORIAL").get("valor")).isEqualTo(50.0);
        // Os indicadores da Saúde Mental não aparecem em outro programa.
        String hiperdia = jdbc.queryForObject("select cast(uuid as varchar) from tb_programa where codigo = 'HIPERDIA'", String.class);
        assertThat((List<String>) JsonPath.read(ler("/api/v1/programa/" + hiperdia + "/indicadores"), "$[*].indicador")).doesNotContain("PTS_EM_DIA");
        // A meta vale para eles como para os demais.
        enviar(post("/api/v1/programa/" + programa + "/meta"), "{\"indicador\": \"RETORNO_INTERSETORIAL\", \"unidadeId\": \"" + caps.getUuid()
                + "\", \"alvo\": 80, \"inicio\": \"" + hoje.minusDays(60) + "\", \"fim\": \"" + hoje.plusDays(30) + "\"}", 201);
        json = ler("/api/v1/programa/" + programa + "/indicadores?unidadeId=" + caps.getUuid() + "&de=" + hoje.minusDays(60) + "&ate=" + hoje);
        assertThat(indicador(json, "RETORNO_INTERSETORIAL")).containsEntry("situacao", "ABAIXO");
        jdbc.update("delete from tb_meta_programatica where unidade_id = ?", caps.getUuid());
    }

    private String internacao(String modalidade, int diasAtras, String leitoNome) {
        Profissional medico = profissionalRepository.findByMatricula("IST-MED").orElseGet(() -> {
            Profissional m = new Profissional();
            m.setMatricula("IST-MED");
            m.setCpf("90033300099");
            m.setNome(PREFIXO + "Médico");
            m.setAtivo(true);
            return profissionalRepository.save(m);
        });
        Setor setor = setorRepository.findAll().stream().filter(s -> s.getUnidade().getUuid().equals(caps.getUuid())).findFirst()
                .orElseGet(() -> setorRepository.save(new Setor(caps, PREFIXO + "Leitos", "IST-LT", TipoSetor.ASSISTENCIAL, true, null)));
        Leito leito = new Leito();
        leito.setUnidade(caps);
        leito.setSetor(setor);
        leito.setIdentificacao(leitoNome);
        leito.setTipo(TipoLeito.CLINICO);
        leito.setSexo(SexoLeito.MISTO);
        leito.setSituacao(SituacaoLeito.OCUPADO);
        leito.setAtivo(true);
        leito = leitoRepository.save(leito);
        UUID paciente = paciente().getUuid();
        return jdbc.queryForObject("insert into tb_internacao (uuid, paciente_id, unidade_id, leito_id, medico_responsavel_id, cid, motivo, carater, "
                + "admitida_em, status, modalidade) values (gen_random_uuid(), ?, ?, ?, ?, 'F20', 'Crise', 'URGENCIA', now() - make_interval(days => ?), "
                + "'INTERNADO', ?) returning cast(uuid as varchar)", String.class, paciente, caps.getUuid(), leito.getUuid(), medico.getUuid(), diasAtras, modalidade);
    }
}
